package dev.henkle.korvus.types

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlin.jvm.JvmInline

/**
 * An [Include] is a container that either references a document or wraps a complete document
 * instance.
 *
 * Used with the ["include" keyword in RQL](https://ravendb.net/docs/article-page/5.0/python/client-api/rest-api/queries/query-the-database#include-related-documents)
 *
 * @param T the type of the document referred to by this [Include]
 * @property id the String id of the document referred to by this [Include]
 */
@Serializable(with = Include.Serializer::class)
sealed interface Include<T> {
    val id: String

    /**
     * A reference to a document in the database
     */
    @JvmInline
    @Serializable
    value class ID<T>(override val id: String) : Include<T>

    /**
     * A database record instance
     */
    @Serializable
    data class Document<T>(override val id: String, val record: T) : Include<T>

    @OptIn(ExperimentalSerializationApi::class)
    class Serializer<T: Any>(
        private val tSerializer: KSerializer<T>,
    ) : KSerializer<Include<T>> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
            serialName = "${tSerializer.descriptor.serialName}Include",
        ) {
            element("id", String.serializer().descriptor)
        }

        override fun serialize(encoder: Encoder, value: Include<T>) {
            when (value) {
                is ID -> encoder.encodeString(value = value.id)
                is Document -> encoder.encodeSerializableValue(
                    serializer = tSerializer,
                    value = value.record,
                )
            }
        }

        override fun deserialize(decoder: Decoder): Include<T> =
            try {
                val record = decoder.decodeSerializableValue(deserializer = tSerializer)
                val id = decoder.decodeStructure(descriptor = descriptor) {
                    decodeStringElement(descriptor = descriptor, index = 0)
                }
                Document(id = id, record = record)
            } catch (ex: Exception) {
                ID(id = decoder.decodeString())
            }
    }
}
