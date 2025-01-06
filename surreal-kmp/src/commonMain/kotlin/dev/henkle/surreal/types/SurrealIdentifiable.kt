package dev.henkle.surreal.types

/**
 * An object that can be located in a SurrealDB database by its [idString] (after converting it to a
 * [record link](https://surrealdb.com/docs/surrealql/datamodel/records#record-links)
 */
interface SurrealIdentifiable<R: SurrealRecord<R>> : SurrealTable<R> {
    /**
     * The string form of a record link
     */
    val idString: String

    /**
     * The table of this record
     */
    override val tableName: String get() = idString.substringBefore(delimiter = ':')

    /**
     * The id of this record stripped of its table prefix
     */
    val idWithoutTable: String get() = idString.substringAfter(delimiter = ':')
}
