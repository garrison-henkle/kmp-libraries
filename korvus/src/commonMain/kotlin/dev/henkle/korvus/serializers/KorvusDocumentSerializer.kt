package dev.henkle.korvus.serializers

import dev.henkle.korvus.ext.update
import dev.henkle.korvus.types.KorvusDocument
import dev.henkle.korvus.types.KorvusMetadata
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class KorvusDocumentSerializer<T: KorvusDocument<T>>(
    private val tSerializer: KSerializer<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = "KorvusDocumentSerializer",
    ) {
        element(elementName = KorvusDocument.KEY, descriptor = tSerializer.descriptor)
        element(elementName = KorvusMetadata.KEY, descriptor = metadataSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor = descriptor) {
            encodeSerializableElement(
                descriptor = descriptor,
                index = 0,
                serializer = tSerializer,
                value = value,
            )
            encodeSerializableElement(
                descriptor = descriptor,
                index = 1,
                serializer = metadataSerializer,
                value = value.metadata,
            )
        }
    }

    override fun deserialize(decoder: Decoder): T {
        return decoder.decodeStructure(descriptor = descriptor) {
            var type: T? = null
            var metadata: KorvusMetadata? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor = descriptor)) {
                    0 -> type = this.decodeSerializableElement(
                        descriptor = descriptor,
                        index = 0,
                        deserializer = tSerializer,
                    )

                    1 -> metadata = decodeSerializableElement(
                        descriptor = descriptor,
                        index = 1,
                        deserializer = metadataSerializer,
                    )

                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index decoding KorvusDocument: $index")
                }
            }
            require(type != null && metadata != null) {
                "Encountered missing values while decoding KorvusDocument"
            }
            type.update(with = metadata)
        }
    }

    companion object {
        private val metadataSerializer = KorvusMetadata.serializer()
    }
}
