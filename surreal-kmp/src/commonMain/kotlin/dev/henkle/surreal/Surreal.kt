package dev.henkle.surreal

import dev.henkle.surreal.internal.impl.SurrealImpl
import dev.henkle.surreal.sdk.RawSurrealQueryResult
import dev.henkle.surreal.sdk.SurrealConnection
import dev.henkle.surreal.sdk.SurrealLiveQueryHandle
import dev.henkle.surreal.sdk.SurrealLiveQueryResponse
import dev.henkle.surreal.sdk.SurrealQueryScope
import dev.henkle.surreal.sdk.SurrealResult
import dev.henkle.surreal.sdk.SurrealResultPromise
import dev.henkle.surreal.sdk.SurrealToken
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealTable
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KType

interface Surreal {
    val connectionStatus: StateFlow<ConnectionStatus>

    /**
     * Starts or restarts the connection to the SurrealDB database instance.
     */
    suspend fun start()

    /**
     * Shuts down the connection to the SurrealDB database instance and stops all current jobs.
     */
    suspend fun shutdown()

    /**
     * Allows for signing into the database, optionally into a specific namespace or database.
     *
     * @param namespace the name of the namespace to log into or null to not log into a namespace.
     * @param database the name of the database to log into or null to not log into a database. [namespace] must be
     * non-null in order to use this parameter.
     * @param user the username to use for the login.
     * @param password the password of the [user] account.
     *
     * @return a result that returns a session token on success.
     */
    suspend fun signIn(
        namespace: String? = null,
        database: String? = null,
        user: String? = null,
        password: String? = null,
    ): SurrealResult<SurrealToken>

    /**
     * Sets or clears the namespace and database of this connection.
     *
     * @param namespace the name of the namespace to use.
     * @param database the name of the database to use. [namespace] must be non-null if this parameter is non-null.
     *
     * @return a result that is successful if the namespace and database were set to the requested values.
     */
    suspend fun use(
        namespace: String? = null,
        database: String? = null,
    ): SurrealResult<Unit>

    /**
     * Retrieves a single record from the database.
     *
     * @param id the id of the record to retrieve.
     * @param type the type of the record to retrieve.
     *
     * @return a successful result containing either the record or null if the record did not exist in the
     * database or an error result if the get failed.
     */
    suspend fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> get(id: I, type: KType): SurrealResult<R?>

    /**
     * Retrieves one or more records from the database.
     *
     * @param ids the ids of the records to retrieve.
     * @param type the type of the records to retrieve.
     *
     * @return a successful result containing the records or an error result if the get failed. The returned
     * list has no guarantees with regard to the number or ordering of the returned records.
     */
    suspend fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> get(ids: Collection<I>, type: KType): SurrealResult<List<R>>

    /**
     * Retrieves all the records from a table.
     *
     * @param table the table to retrieve.
     * @param type the type of the records in the table.
     *
     * @return a successful result containing all the records contained in the table or an error result if the get
     * failed. The returned list has no guarantees with regard to the number or ordering of the returned records.
     */
    suspend fun <R: SurrealRecord<R>, T: SurrealTable<R>> getAll(table: T, type: KType): SurrealResult<List<R>>

    /**
     * Inserts a single record into the database.
     *
     * @param record the record to insert.
     * @param type the type of the record to insert.
     *
     * @return a successful result containing the record that was inserted with any id updates made by the database
     * or an error result if the insertion failed. Collision errors can be identified via the
     * [dev.henkle.surreal.errors.DatabaseError.isCollision] property of the error contained inside of the error
     * result's [dev.henkle.surreal.errors.SurrealError.DB].
     */
    suspend fun <R: SurrealRecord<R>> insert(record: R, type: KType): SurrealResult<R>

    /**
     * Inserts a collection of records into the database.
     *
     * @param records the records to insert.
     * @param type the type of the records to insert.
     *
     * @return a succeesful result containing the record that was inserted with any id updates made by the database
     * or an error result if the insertions failed. The returned list has no guarantees with regard to the number or
     * ordering of the returned records. Collision errors can be identified via the
     * [dev.henkle.surreal.errors.DatabaseError.isCollision] property of the error contained inside of the error
     * result's [dev.henkle.surreal.errors.SurrealError.DB].
     */
    suspend fun <R: SurrealRecord<R>> insert(records: Collection<R>, type: KType): SurrealResult<List<R>>

    /**
     * Puts (upserts) a record into the database.
     *
     * @param record the record to put in the database.
     * @param type the type of the record to put in the database.
     *
     * @return a successful result containing the record that was put in the database with any id updates made by the
     * database or an error result if the put failed.
     */
    suspend fun <R: SurrealRecord<R>> put(record: R, type: KType): SurrealResult<R>

    /**
     * Updates an existing record in the database.
     *
     * @param record the record to update.
     * @param type the type of the record to update.
     *
     * @return a successful result containing either the updated record or null if no record with a matching id was
     * found in the database or an error result if the update failed.
     */
    suspend fun <R: SurrealRecord<R>> update(record: R, type: KType): SurrealResult<R?>

    /**
     * Updates all the records in the specified table with the provided update.
     *
     * @param table the table whose records will be updated.
     * @param update the update that will be applied to the table. The update should be an instance of [R] with the
     * [SurrealRecord.id] field removed.
     * @param tableType the type of the records in the [table].
     * @param updateType the type of the update.
     *
     * @return a successful result containing a list with the updated records or an error result if the update failed.
     */
    suspend fun <R : SurrealRecord<R>, T: SurrealTable<R>, U: Any> updateAll(
        table: T,
        update: U,
        tableType: KType,
        updateType: KType,
    ): SurrealResult<List<R>>

    suspend fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>, U: Any> merge(
        id: I,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResult<R?>

    suspend fun <R: SurrealRecord<R>, T: SurrealTable<R>, U: Any> mergeAll(
        table: T,
        update: U,
        tableType: KType,
        updateType: KType,
    ): SurrealResult<List<R>>

    suspend fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> delete(id: I, type: KType): SurrealResult<R?>

    suspend fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> delete(ids: Collection<I>, type: KType): SurrealResult<List<R>>

    suspend fun <R: SurrealRecord<R>, T: SurrealTable<R>> deleteAll(table: T, type: KType): SurrealResult<List<R>>

    suspend fun <T> queryOne(
        query: String,
        parameters: Map<String, Any?> = emptyMap(),
        type: KType,
    ): SurrealResult<T>

    suspend fun <T> queryMany(
        query: String,
        parameters: Map<String, Any?> = emptyMap(),
        type: KType,
    ): SurrealResult<List<T>>

    suspend fun queryRaw(
        query: String,
        parameters: Map<String, Any?> = emptyMap(),
    ): RawSurrealQueryResult

    suspend fun query(
        builder: SurrealQueryScope.() -> List<SurrealResultPromise<*>>,
    ): List<SurrealResult<*>>

    suspend fun transaction(builder: SurrealQueryScope.() -> List<SurrealResultPromise<*>>): List<SurrealResult<*>>

    suspend fun <I, O, E> relate(
        edge: E,
        edgeType: KType,
    ): SurrealResult<E> where
        I: SurrealRecord<I>,
        O: SurrealRecord<O>,
        E: SurrealEdge<E, I, O>

    suspend fun <I, II, O, OI, E, ET> relate(
        table: ET,
        `in`: II,
        out: OI,
        edgeType: KType,
    ): SurrealResult<E> where
        I: SurrealRecord<I>,
        II: SurrealIdentifiable<I>,
        O: SurrealRecord<O>,
        OI: SurrealIdentifiable<O>,
        E: SurrealEdge<E, I, O>,
        ET: SurrealEdgeTable<E, I, O>

    suspend fun <R: SurrealRecord<R>, T: SurrealTable<R>> live(
        table: T,
        type: KType,
    ): SurrealResult<SurrealLiveQueryResponse<R>>

    suspend fun kill(handle: SurrealLiveQueryHandle, force: Boolean = true): SurrealResult<Unit>

    suspend fun <R: SurrealRecord<R>> registerForLiveUpdates(
        handle: SurrealLiveQueryHandle,
        type: KType,
    ): SurrealLiveQueryResponse<R>

    suspend fun unregisterFromLiveUpdates(handle: SurrealLiveQueryHandle)

    sealed interface ConnectionStatus {
        data class Connected(internal val session: DefaultClientWebSocketSession) : ConnectionStatus
        data class Reconnecting(val cause: Throwable) : ConnectionStatus
        data object NotConnected : ConnectionStatus
    }

    companion object {
        fun create(
            url: String,
            port: Int = 8000,
            requestBuilder: HttpRequestBuilder.() -> Unit = {},
            onConnect: suspend Surreal.() -> Unit,
        ): Surreal = SurrealImpl.create(
            url = url,
            port = port,
            onConnect = onConnect,
        )

        suspend fun create(
            url: String,
            port: Int = 8000,
            requestBuilder: HttpRequestBuilder.() -> Unit = {},
            connection: SurrealConnection,
        ): Surreal = SurrealImpl.create(
            url = url,
            port = port,
            connection = connection,
        )
    }
}
