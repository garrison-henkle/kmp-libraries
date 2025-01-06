package dev.henkle.surreal.utils

import dev.henkle.nanoid.nanoId
import dev.henkle.surreal.internal.utils.idGenerator
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing

/**
 * Creates a [Thing.ID] for type [R] with the provided [id] string.
 *
 * @param id the id string in the form "table_name:id"
 */
fun <R: SurrealRecord<R>> id(id: String): Thing.ID<R> = Thing.ID(id = id)

/**
 * Creates a [Thing.ID] for the specified [table] with the provided [id].
 *
 * @param table a SurrealDB table that will be used for the id prefix (before the ':')
 * @param id an identifier that will be used for the id suffix (after the ':')
 */
fun <R: SurrealRecord<R>, T: SurrealTable<R>> id(table: T, id: String): Thing.ID<R> = Thing.ID(id = "${table.tableName}:$id")

/**
 * Creates a [Thing.ID] for the specified [table] with the provided [id].
 *
 * @param table a SurrealDB table that will be used for the id prefix (before the ':')
 * @param `in` the record whose id suffix will make up the middle portion of id (between the two ':'s)
 * @param `out` the record whose id suffix will make up the last portiion of the id (after the last ':')
 */
fun <E, ET, I, II, O, OI> id(
    table: ET,
    `in`: II,
    out: OI
): Thing.EdgeID<E, I, O> where
    E: SurrealEdge<E, I, O>,
    ET: SurrealEdgeTable<E, I, O>,
    I: SurrealRecord<I>,
    II: SurrealIdentifiable<I>,
    O: SurrealRecord<O>,
    OI: SurrealIdentifiable<O> =
        Thing.EdgeID(id = "${table.tableName}:${`in`.idWithoutTable}:${out.idWithoutTable}")

/**
 * Creates a [Thing.EdgeID] for the specified edge [table] with the provided [id].
 *
 * @param table a SurrealDB edge table that will be used for the id prefix (before the ':')
 * @param id an identifier that will be used for the id suffix (after the ':')
 */
fun <E: SurrealEdge<E, I, O>, T: SurrealEdgeTable<E, I, O>, I: SurrealRecord<I>, O: SurrealRecord<O>> id(
    table: T,
    id: String,
): Thing.EdgeID<E, I, O> = Thing.EdgeID(id = "${table.tableName}:$id")

/**
 * Creates a [Thing.ID] for the specified [table] with a random nano id.
 *
 * @param table a SurrealDB table that will be used for the id prefix (before the ':')
 */
fun <R: SurrealRecord<R>, T: SurrealTable<R>> nanoId(table: T): Thing.ID<R> = id(table = table, id = idGenerator.generate())

/**
 * Creates a [Thing.EdgeID] for the specified edge [table] with a random nano id.
 *
 * @param table a SurrealDB edge table that will be used for the id prefix (before the ':')
 */
fun <E: SurrealEdge<E, I, O>, T: SurrealEdgeTable<E, I, O>, I: SurrealRecord<I>, O: SurrealRecord<O>> nanoId(
    table: T,
): Thing.EdgeID<E, I, O> = id(table = table, id = idGenerator.generate())

/**
 * Creates a [Thing.ID] that instructs the database to autogenerate the id for this record
 */
fun <R: SurrealRecord<R>> generateId(): Thing.ID<R> = Thing.ID(id = "")
