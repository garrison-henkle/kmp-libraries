package dev.henkle.surreal.sdk

import dev.henkle.surreal.errors.DatabaseError
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

@Serializable(with = SurrealQueryResultValue.Serializer::class)
sealed interface SurrealQueryResultValue<T> {
    val data: T? get() = if (this is Data) data else null
    val error: DatabaseError? get() = if (this is Error) error else null

    @Serializable
    data class Data<T>(override val data: T) : SurrealQueryResultValue<T>

    @Serializable
    data class Error<T>(
        override val error: DatabaseError,
        internal val raw: JsonElement,
    ) : SurrealQueryResultValue<T>

    @OptIn(ExperimentalSerializationApi::class)
    class Serializer<T>(
        private val tSerializer: KSerializer<T>,
    ) : KSerializer<SurrealQueryResultValue<T>> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
            serialName = "SurrealQueryResultValue<${tSerializer.descriptor.serialName}>"
        )

        override fun serialize(encoder: Encoder, value: SurrealQueryResultValue<T>) {
            when (value) {
                is Data -> encoder.encodeSerializableValue(serializer = tSerializer, value = value.data)
                is Error -> encoder.encodeString(value = value.error.message)
            }
        }

        override fun deserialize(decoder: Decoder): SurrealQueryResultValue<T> =
            try {
                val data = decoder.decodeSerializableValue(deserializer = tSerializer)
                Data(data = data)
            } catch(ex: Exception) {
                val raw = decoder.decodeSerializableValue(deserializer = serializer<JsonElement>())
                val message = try {
                    raw.jsonPrimitive.content
                } catch (ex: Exception) {
                    ex.toString()
                }
                Error(error = DatabaseError(message = message), raw = raw)
            }
    }

    companion object {
        private const val KEY_RESULT = "result"
    }
}
