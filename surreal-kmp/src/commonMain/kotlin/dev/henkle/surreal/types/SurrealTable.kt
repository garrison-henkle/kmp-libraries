package dev.henkle.surreal.types

/**
 * A table in a SurrealDB database.
 *
 * See [DEFINE TABLE documentation](https://surrealdb.com/docs/surrealql/statements/define/table) for more information.
 */
interface SurrealTable<R: SurrealRecord<R>>  {
    /**
     * The name of the table
     */
    val tableName: String
}
