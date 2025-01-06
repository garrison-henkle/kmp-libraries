package dev.henkle.surreal.internal.model

import dev.henkle.surreal.internal.utils.ext.encodeToJsonObjWithoutId
import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

internal sealed interface RPCParams<T> {
    /**
     * An RPC request that requires no parameters
     */
    class None<T> : RPCParams<T>

    /**
     * An RPC request that requires one or more parameters of the same type
     */
    @Serializable(with = DataList.Serializer::class)
    data class DataList<T>(val data: List<T>) : RPCParams<T> {
        class Serializer<T>(genericTypeSerializer: KSerializer<T>) : KSerializer<DataList<T>> {
            private val serializer = ListSerializer(elementSerializer = genericTypeSerializer)
            override val descriptor: SerialDescriptor = serializer.descriptor

            override fun serialize(encoder: Encoder, value: DataList<T>) {
                encoder.encodeSerializableValue(
                    serializer = serializer,
                    value = value.data,
                )
            }

            override fun deserialize(decoder: Decoder): DataList<T> {
                throw NotImplementedError(message = "Deserialization of RPCParams is not allowed!")
            }
        }
    }

    /**
     * An RPC request that requires a [String] [string] parameter followed by some arbitrary [data] in the parameters list
     *
     * e.g.
     * ```json
     * [
     *     "person",
     *     { "name": "Mary Doe" }
     * ]
     * ```
     */
    @Serializable(with = StringWithData.Serializer::class)
    data class StringWithData<T>(val string: String, val data: T) : RPCParams<T> {
        class Serializer<T>(private val genericTypeSerializer: KSerializer<T>) : KSerializer<StringWithData<T>> {
            private val serializer = ListSerializer(elementSerializer = serializer<JsonElement>())
            override val descriptor: SerialDescriptor = serializer.descriptor

            override fun serialize(encoder: Encoder, value: StringWithData<T>) {
                encoder.encodeSerializableValue(
                    serializer = serializer,
                    value = listOf(
                        nullSerializer.encodeToJsonElement(value = value.string),
                        nullSerializer.encodeToJsonElement(serializer = genericTypeSerializer, value = value.data),
                    ),
                )
            }

            override fun deserialize(decoder: Decoder): StringWithData<T> {
                throw NotImplementedError(message = "Deserialization of RPCParams is not allowed!")
            }
        }
    }

    /**
     * The parameters for a relate RPC request
     *
     * e.g.
     * ```json
     * [
     *     "person:12s0j0bbm3ngrd5c9bx53",
     *     "knows",
     *     "person:8s0j0bbm3ngrd5c9bx53"
     * ]
     * ```
     * or
     * ```json
     * [
     *     "person:john_doe",
     *     "knows",
     *     "person:jane_smith",
     *     { "since": "2020-01-01" }
     * ]
     * ```
     */
    @Serializable(with = Relation.Serializer::class)
    data class Relation<R>(
        val inId: String,
        val outId: String,
        val edgeTable: String,
        val data: R? = null,
    ) : RPCParams<R> {
        class Serializer<R: SurrealRecord<R>>(private val genericTypeSerializer: KSerializer<R>) : KSerializer<Relation<R>> {
            private val serializer = ListSerializer(elementSerializer = serializer<JsonElement>())
            override val descriptor: SerialDescriptor = serializer.descriptor

            override fun serialize(encoder: Encoder, value: Relation<R>) {
                encoder.encodeSerializableValue(
                    serializer = serializer,
                    value = listOfNotNull(
                        nullSerializer.encodeToJsonElement(value = value.inId),
                        nullSerializer.encodeToJsonElement(value = value.edgeTable),
                        nullSerializer.encodeToJsonElement(value = value.outId),
                        value.data?.encodeToJsonObjWithoutId(serializer = genericTypeSerializer),
                    ),
                )
            }

            override fun deserialize(decoder: Decoder): Relation<R> {
                throw NotImplementedError(message = "Deserialization of RPCParams is not allowed!")
            }
        }
    }
}
