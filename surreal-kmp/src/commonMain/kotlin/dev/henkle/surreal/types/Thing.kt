package dev.henkle.surreal.types

import dev.henkle.surreal.internal.utils.ThingSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * An [Thing] is a container that either references a record or wraps a complete record instance.
 *
 * Used with the
 * ["fetch" keyword in SurrealQL](https://surrealdb.com/docs/surrealql/statements/select#connect-targets-using-the-fetch-clause)
 *
 * @param R the type of the record referred to by this [Thing]
 * @property id the String id of the record referred to by this [Thing]
 */
@Serializable(with = ThingSerializer::class)
sealed interface Thing<R: SurrealRecord<R>> : SurrealIdentifiable<R> {
    val id: String
    override val idString: String get() = id

    interface RecordID {
        val idWithoutTable: String
    }

    /**
     * A reference to a document in the database
     */
    @Serializable(with = ID.Serializer::class)
    open class ID<R: SurrealRecord<R>>(override val id: String) : Thing<R>, RecordID {
        override val idWithoutTable: String get() = id.substringAfter(delimiter = ':')
        class Serializer<R: SurrealRecord<R>> : KSerializer<ID<R>> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName = "Thing.ID", kind = PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): ID<R> = ID(id = decoder.decodeString())
            override fun serialize(encoder: Encoder, value: ID<R>) {
                value.id.takeIf { it.isNotEmpty() }?.let { id -> encoder.encodeString(value = id) }
            }
        }
        override fun toString(): String = "Thing.ID(id=$id)"
        override fun equals(other: Any?): Boolean = other is ID<*> && other.id == id
        override fun hashCode(): Int = this::class.hashCode()
    }

    @Serializable(with = EdgeID.Serializer::class)
    class EdgeID<E: SurrealEdge<E, I, O>, I: SurrealRecord<I>, O: SurrealRecord<O>>(override val id: String): ID<E>(id = id) {
        val inID: String get() = id.substringAfter(delimiter = ':').substringBefore(delimiter = ':')
        val outID: String get() = id.substringAfterLast(delimiter = ':')
        class Serializer<E: SurrealEdge<E, I, O>, I: SurrealRecord<I>, O: SurrealRecord<O>> : KSerializer<EdgeID<E, I, O>> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName = "Thing.EdgeID", kind = PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): EdgeID<E, I, O> = EdgeID(id = decoder.decodeString())
            override fun serialize(encoder: Encoder, value: EdgeID<E, I, O>) {
                value.id.takeIf { it.isNotEmpty() }?.let { id -> encoder.encodeString(value = id) }
            }
        }
        override fun toString(): String = "Thing.EdgeID(id=$id)"
        override fun equals(other: Any?): Boolean = other is EdgeID<*, *, *> && other.id == id
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * A database record instance
     */
    @Serializable
    data class Record<R: SurrealRecord<R>>(override val id: String, val record: R) : Thing<R>, RecordID {
        override val idWithoutTable: String get() = id.substringAfter(delimiter = ':')
        override fun toString(): String = "Thing.Record(id=$id, record=$record)"
    }
}
