package dev.henkle.surreal.internal.model

import dev.henkle.surreal.internal.model.RPCParams.DataList
import dev.henkle.surreal.internal.model.RPCParams.None
import dev.henkle.surreal.internal.model.RPCParams.Relation
import dev.henkle.surreal.internal.model.RPCParams.StringWithData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement

@Serializable(with = RPCRequest.Serializer::class)
internal data class RPCRequest<T>(
    val id: RequestID,
    val method: String,
    val params: RPCParams<T> = None(),
) {
    class Serializer<T>(private val genericSerializer: KSerializer<T>) : KSerializer<RPCRequest<T>> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName = "RPCRequest") {
            element(elementName = "id", descriptor = serialDescriptor<RequestID>())
            element(elementName = "method", descriptor = serialDescriptor<String>())
            element(elementName = "params", descriptor = serialDescriptor<JsonElement>(), isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: RPCRequest<T>) {
            encoder.encodeStructure(descriptor = descriptor) {
                encodeLongElement(descriptor = descriptor, index = 0, value = value.id)
                encodeStringElement(descriptor = descriptor, index = 1, value = value.method)

                when (value.params) {
                    is DataList ->
                        encodeParams(value = value.params, serializer = DataList.serializer(typeSerial0 = genericSerializer))

                    is StringWithData ->
                        encodeParams(value = value.params, serializer = StringWithData.serializer(typeSerial0 = genericSerializer))

                    is Relation ->
                        encodeParams(value = value.params, serializer = Relation.serializer(typeSerial0 = genericSerializer))

                    is None -> { /* no-op */ }
                }
            }
        }

        private fun <T, P: RPCParams<T>> CompositeEncoder.encodeParams(value: P, serializer: KSerializer<P>) {
            encodeSerializableElement(
                descriptor = descriptor,
                index = 2,
                serializer = serializer,
                value = value,
            )
        }

        override fun deserialize(decoder: Decoder): RPCRequest<T> {
            throw NotImplementedError(message = "Deserialization of RPCRequests is not allowed!")
        }
    }
}
