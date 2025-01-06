package dev.henkle.surreal.internal.utils

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.types.Thing.ID
import dev.henkle.surreal.types.Thing.Record
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

// this can't live in Thing b/c it would have to be public (since it would be a member of an interface)
@OptIn(ExperimentalSerializationApi::class)
internal class ThingSerializer<R: SurrealRecord<R>>(
    private val tSerializer: KSerializer<R>,
) : KSerializer<Thing<R>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = "${tSerializer.descriptor.serialName}Thing",
    ) {
        element("id", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: Thing<R>) {
        encoder.encodeString(value = value.id)
    }

    override fun deserialize(decoder: Decoder): Thing<R> =
        try {
            val record = decoder.decodeSerializableValue(deserializer = tSerializer)
            val id = decoder.decodeStructure(descriptor = descriptor) {
                decodeStringElement(descriptor = descriptor, index = 0)
            }
            Record(id = id, record = record)
        } catch (ex: Exception) {
            ID(id = decoder.decodeString())
        }
}
