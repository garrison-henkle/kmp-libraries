package dev.henkle.korvus

interface KorvusDatabaseAdmin {
    /**
     * Creates a database with the specified [name] in the RavenDB instance
     *
     * @param name the name of the database to create
     * @param replicationFactor The number of replicas that will be maintained in the cluster for the new database
     */
    suspend fun create(
        name: String,
        replicationFactor: Int = 1,
    ): KorvusResult<KorvusDatabase>

    /**
     * Deletes the specified databases from the RavenDB instance
     *
     * @param dbNames the names of the databases to delete
     * @param hardDelete determines whether or not the data associated with the database should be immediately deleted
     */
    suspend fun delete(
        vararg dbNames: String,
        hardDelete: Boolean = true,
    ): KorvusResult<Unit>

    /**
     * Retrieves an instance of a database that can be used for database operations
     *
     * @param name the name of the database to retrieve
     * @param replicationFactor the number of replicas that will be maintained in the cluster for the new database if
     * it needs to be created
     * @param createIfNeeded determines whether the database will be created if it does not exist
     */
    suspend fun get(
        name: String,
        replicationFactor: Int = 1,
        createIfNeeded: Boolean = true,
    ): KorvusResult<KorvusDatabase>

    /**
     * Retrieves the list of database names in this RavenDB instance
     */
    suspend fun getNames(): KorvusResult<List<String>>
}
