package dev.henkle.surreal.types

import dev.henkle.nanoid.nanoId

/**
 * A record that is stored in a SurrealDB database
 */
interface SurrealRecord<R> : SurrealIdentifiable<R> where R : SurrealRecord<R>, R : Any {
    val id: Thing.ID<R>

    override val idString: String get() = id.id

    fun generateId(): Thing.ID<R> = Thing.ID(id = "$tableName:${nanoId()}")

    @Suppress("UNCHECKED_CAST")
    fun asThing(): Thing.Record<R> = Thing.Record(id = id.id, record = this as R)
}
