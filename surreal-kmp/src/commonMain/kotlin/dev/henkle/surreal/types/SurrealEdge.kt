package dev.henkle.surreal.types

/**
 * A relation in a SurrealDB database.
 *
 * Relations create graph edges between pairs of records that can be traversed in a bi-directional manner.
 * Relations themselves are normal records but must have `in` and `out` fields.
 */
interface SurrealEdge<E: SurrealEdge<E, I, O>, I: SurrealRecord<I>, O: SurrealRecord<O>> : SurrealRecord<E> {
    override val id: Thing.EdgeID<E, I, O>
    val `in`: Thing<I>
    val out: Thing<O>
}
