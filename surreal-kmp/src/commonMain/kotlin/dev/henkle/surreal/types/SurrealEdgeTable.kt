package dev.henkle.surreal.types

/**
 * An edge table in a SurrealDB database.
 *
 * Edge tables are tables for storing relations. They have two special properties:
 * - edge tables are deleted when there are no relationships in them
 * - edge tables have two required fields: `in` ([SurrealEdge.from]) and `out` ([SurrealEdge.to])
 *
 * See [RELATE statement documentation](https://surrealdb.com/docs/surrealql/statements/relate#relate-statement)  for more on edge tables.
 */
interface SurrealEdgeTable<E: SurrealEdge<E, I, O>, I: SurrealRecord<I>, O: SurrealRecord<O>> : SurrealTable<E>
