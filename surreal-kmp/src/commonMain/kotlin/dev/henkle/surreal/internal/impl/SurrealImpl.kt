// todo(garrison): maybe use that Jake Wharton plugin one day to perform an unsafe casts in the file to save on runtime checks
@file:Suppress("UNCHECKED_CAST")

package dev.henkle.surreal.internal.impl

import co.touchlab.kermit.Logger
import dev.henkle.surreal.Surreal
import dev.henkle.surreal.Surreal.ConnectionStatus
import dev.henkle.surreal.errors.DatabaseError
import dev.henkle.surreal.errors.EmptyArgListException
import dev.henkle.surreal.errors.NonSingleRecordResultException
import dev.henkle.surreal.errors.NonSingleStatementQueryException
import dev.henkle.surreal.errors.NotConnectedException
import dev.henkle.surreal.errors.QueryResultCountMismatchException
import dev.henkle.surreal.errors.SurrealError
import dev.henkle.surreal.errors.SurrealSDKException
import dev.henkle.surreal.internal.model.LiveQueryUpdate
import dev.henkle.surreal.internal.model.RPCParams
import dev.henkle.surreal.internal.model.RPCRequest
import dev.henkle.surreal.internal.model.RPCResponse
import dev.henkle.surreal.internal.model.RequestID
import dev.henkle.surreal.internal.model.functions.SignInRequest
import dev.henkle.surreal.internal.utils.IO
import dev.henkle.surreal.internal.utils.ext.toSerializableMap
import dev.henkle.surreal.internal.utils.ext.toSurrealResult
import dev.henkle.surreal.internal.utils.ext.tryParseDatabaseError
import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.internal.utils.obj
import dev.henkle.surreal.internal.utils.relate
import dev.henkle.surreal.internal.utils.retryWithExponentialBackoff
import dev.henkle.surreal.internal.utils.stringWithObj
import dev.henkle.surreal.internal.utils.strings
import dev.henkle.surreal.sdk.RawSurrealQueryResult
import dev.henkle.surreal.sdk.RawSurrealStatementResult
import dev.henkle.surreal.sdk.SurrealConnection
import dev.henkle.surreal.sdk.SurrealLiveQueryHandle
import dev.henkle.surreal.sdk.SurrealLiveQueryResponse
import dev.henkle.surreal.sdk.SurrealQueryResult
import dev.henkle.surreal.sdk.SurrealQueryResultValue
import dev.henkle.surreal.sdk.SurrealQueryScope
import dev.henkle.surreal.sdk.SurrealResult
import dev.henkle.surreal.sdk.SurrealResultPromise
import dev.henkle.surreal.sdk.SurrealToken
import dev.henkle.surreal.sdk.map
import dev.henkle.surreal.sdk.then
import dev.henkle.surreal.sdk.withResult
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealLiveQueryAction
import dev.henkle.surreal.types.SurrealLiveQueryUpdate
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmStatic
import kotlin.reflect.KType
import dev.henkle.surreal.internal.utils.client as clientInstance

private typealias RawQueryResult = SurrealQueryResult<RawSurrealStatementResult>

internal class SurrealImpl private constructor(
    private val url: String,
    private val port: Int = 8000,
    private val client: HttpClient = clientInstance,
    private val json: Json = nullSerializer,
    private val context: CoroutineContext = Dispatchers.IO + SupervisorJob(),
    private val requestBuilder: HttpRequestBuilder.() -> Unit = {},
    private val onConnect: suspend Surreal.() -> Unit,
): Surreal {
    private val scopeJob = Job()
    private val scope = CoroutineScope(context + scopeJob)
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.NotConnected)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    private val requestsMutex = Mutex()
    private val requests = mutableMapOf<RequestID, Request<Any?>>()
    private val liveRequestsMutex = Mutex()
    private val liveRequests = mutableMapOf<SurrealLiveQueryHandle, LiveRequest<Any?>>()

    override suspend fun start() {
        val job = scope.launch {
            val socketSession = retryWithExponentialBackoff {
                client.webSocketSession(
                    method = HttpMethod.Get,
                    host = url,
                    port = port,
                    path = SURREAL_JSON_RPC_PATH,
                ) {
                    header(key = HttpHeaders.SecWebSocketProtocol, value = "json")
                    requestBuilder()
                }
            }
            _connectionStatus.value = ConnectionStatus.Connected(session = socketSession)
            scope.launch {
                socketSession.incoming.consumeAsFlow().collect { frame ->
                    val textFrame = (frame as? Frame.Text) ?: return@collect
                    val text = textFrame.readText()
                    val response = try {
                        json.decodeFromString<RPCResponse>(string = text)
                    } catch(ex: Exception) {
                        try {
                            json.encodeToJsonElement(value = text).jsonObject["error"]?.also { obj ->
                                val error = json.decodeFromJsonElement<DatabaseError>(json = obj)
                                Logger.e("SurrealKMP", throwable = error) { "Database returned an error!" }
                            }
                        } catch(ex: Exception) {
                            Logger.e("SurrealKMP", throwable = ex) { "Unknown response frame from the database!" }
                            return@collect
                        }
                        return@collect
                    }
                    Logger.d("SurrealKMP") { "Received frame: $response" }
                    if (response.isLiveQueryUpdate) {
                        val update = try {
                            response.result?.let { result ->
                                json.decodeFromJsonElement<LiveQueryUpdate>(json = result)
                            } ?: run {
                                Logger.e("SurrealKMP") { "Expected live query update frame but found an empty frame!" }
                                return@collect
                            }
                        } catch (ex: Exception) {
                            Logger.e("SurrealKMP", throwable = ex) {
                                "Expected live query update frame but found an unknown frame!"
                            }
                            return@collect
                        }
                        try {
                            liveRequests[update.handle]?.also { request ->
                                when (update.action) {
                                    SurrealLiveQueryAction.Create -> {
                                        val record = json.decodeFromJsonElement(
                                            deserializer = request.serializer,
                                            element = update.result,
                                        )
                                        request.updates.send(element = SurrealLiveQueryUpdate.Create(record = record))
                                    }
                                    SurrealLiveQueryAction.Update -> {
                                        val record = json.decodeFromJsonElement(
                                            deserializer = request.serializer,
                                            element = update.result,
                                        )
                                        request.updates.send(element = SurrealLiveQueryUpdate.Update(record = record))
                                    }
                                    SurrealLiveQueryAction.Delete -> {
                                        update.result.jsonObject["id"]?.jsonPrimitive?.content?.also { id ->
                                            request.updates.send(
                                                element = SurrealLiveQueryUpdate.Delete(id = Thing.ID(id = id)),
                                            )
                                        } ?: Logger.e("SurrealKMP") {
                                            "Expected id for live query delete update but no id field was found!"
                                        }
                                    }
                                }
                            } ?: Logger.e("SurrealKMP") { "Received a live update with an unknown id!" }
                        } catch(ex: Exception) {
                            Logger.e("SurrealKMP", throwable = ex) { "Failed to deserialize contents of live update!" }
                        }
                    } else {
                        requestsMutex.withLock {
                            requests.remove(key = response.id)
                        }?.also { request ->
                            scope.launch {
                                if (response.error != null) {
                                    request.promise.completeExceptionally(exception = response.error)
                                } else {
                                    try {
                                        val parsedResponse = json.decodeFromJsonElement(
                                            deserializer = request.serializer,
                                            element = response.result ?: JsonNull,
                                            )
                                        request.promise.complete(value = parsedResponse)
                                    } catch (ex: Exception) {
                                        try {
                                            response.result?.jsonArray?.tryParseDatabaseError()?.also { dbError ->
                                                request.promise.completeExceptionally(exception = dbError)
                                            } ?: run {
                                                request.promise.completeExceptionally(exception = ex)
                                            }
                                        } catch(_: IllegalArgumentException) {
                                            request.promise.completeExceptionally(exception = ex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Logger.e("SurrealKMP") { "Socket collector unexpectedly stopped!" }
                _connectionStatus.value = ConnectionStatus.Reconnecting(
                    cause = SurrealSDKException(message = "Unknown error caused websocket to close!"),
                )
                start()
            }.invokeOnCompletion { ex ->
                if (ex != null && ex !is CancellationException) {
                    Logger.e("SurrealKMP", throwable = ex) { "Socket collector unexpectedly stopped!" }
                    _connectionStatus.value = ConnectionStatus.Reconnecting(cause = ex)
                    scope.launch {
                        start()
                    }
                }
            }
            onConnect()
        }
        job.join()
    }

    override suspend fun shutdown() {
        connectionStatus.value.also { connection ->
            if (connection is ConnectionStatus.Connected) {
                connection.session.close()
            }
            scopeJob.cancelChildren()
            _connectionStatus.value = ConnectionStatus.NotConnected
        }
    }

    override suspend fun signIn(
        namespace: String?,
        database: String?,
        user: String?,
        password: String?,
    ): SurrealResult<SurrealToken> =
        request(
            method = METHOD_SIGN_IN,
            params = obj(
                param = SignInRequest(
                    namespace = namespace,
                    database = database,
                    user = user,
                    password = password,
                ),
            ),
            returnSerializer = serializer<SurrealToken>(),
        )

    override suspend fun use(
        namespace: String?,
        database: String?,
    ): SurrealResult<Unit> =
        request<String?, Unit?>(
            method = METHOD_USE,
            params = strings(namespace, database),
            returnSerializer = serializer<Unit?>(),
        ).map { if (it != null) throw SurrealSDKException("Invalid 'use' response: did not receive expected null!") }

    override suspend fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> get(
        id: I,
        type: KType,
    ): SurrealResult<R?> {
        val returnSerializer = (serializer(type = type) as KSerializer<R>).nullable
        return request(
            method = METHOD_SELECT,
            params = strings(id.idString),
            returnSerializer = returnSerializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> get(
        ids: Collection<I>,
        type: KType,
    ): SurrealResult<List<R>> {
        if (ids.isEmpty()) {
            return SurrealResult.Failure(
                error = SurrealError.SDK(ex = EmptyArgListException(function = "get(Collection<I>, KType)")),
            )
        }
        return queryMany(
            query = getByIdsQuery(table = ids.first().tableName),
            parameters = mapOf("ids" to ids.map { it.idString }),
            type = type,
        )
    }

    override suspend fun <R : SurrealRecord<R>, T : SurrealTable<R>> getAll(
        table: T,
        type: KType,
    ): SurrealResult<List<R>> {
        val returnSerializer = ListSerializer(elementSerializer = serializer(type = type)).nullable as KSerializer<List<R>?>
        return request(
            method = METHOD_SELECT,
            params = strings(table.tableName),
            returnSerializer = returnSerializer,
        ).map { it ?: emptyList() }
    }


    override suspend fun <R : SurrealRecord<R>> insert(
        record: R,
        type: KType,
    ): SurrealResult<R> {
        val serializer = serializer(type = type)
        val returnSerializer = ListSerializer(elementSerializer = serializer) as KSerializer<List<R>>
        return request(
            method = METHOD_INSERT,
            params = stringWithObj(string = record.tableName, param = record),
            paramsGenericTypeSerializer = serializer,
            returnSerializer = returnSerializer,
        ).map { it.first() }
    }

    override suspend fun <R : SurrealRecord<R>> insert(
        records: Collection<R>,
        type: KType,
    ): SurrealResult<List<R>> {
        if (records.isEmpty()) {
            return SurrealResult.Failure(
                error = SurrealError.SDK(ex = EmptyArgListException(function = "insert(Collection<I>, KType)")),
            )
        }
        val serializer = ListSerializer(elementSerializer = serializer(type = type)) as KSerializer<List<R>>
        return request(
            method = METHOD_INSERT,
            params = stringWithObj(string = records.first().tableName, param = records.toList()),
            paramsGenericTypeSerializer = serializer,
            returnSerializer = serializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>> put(
        record: R,
        type: KType,
    ): SurrealResult<R> {
        val serializer = serializer(type = type) as KSerializer<R>
        return request(
            method = METHOD_UPSERT,
            params = stringWithObj(string = record.idString, param = record),
            paramsGenericTypeSerializer = serializer,
            returnSerializer = serializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>> update(
        record: R,
        type: KType,
    ): SurrealResult<R?> {
        val serializer = (serializer(type = type) as KSerializer<R>).nullable
        return request(
            method = METHOD_UPDATE,
            params = stringWithObj(string = record.idString, param = record),
            paramsGenericTypeSerializer = serializer,
            returnSerializer = serializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>, T: SurrealTable<R>, U: Any> updateAll(
        table: T,
        update: U,
        tableType: KType,
        updateType: KType,
    ): SurrealResult<List<R>> {
        val paramsGenericTypeSerializer = serializer(type = updateType)
        val serializer = serializer(type = tableType)
        val returnSerializer = ListSerializer(elementSerializer = serializer) as KSerializer<List<R>>
        return request(
            method = METHOD_UPDATE,
            params = stringWithObj(string = table.tableName, param = update),
            paramsGenericTypeSerializer = paramsGenericTypeSerializer,
            returnSerializer = returnSerializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>, U : Any> merge(
        id: I,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResult<R?> {
        val paramsGenericTypeSerializer = serializer(type = updateType)
        val returnSerializer = (serializer(type = recordType) as KSerializer<R>).nullable
        return request(
            method = METHOD_MERGE,
            params = stringWithObj(string = id.idString, param = update),
            paramsGenericTypeSerializer = paramsGenericTypeSerializer,
            returnSerializer = returnSerializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>, T : SurrealTable<R>, U : Any> mergeAll(
        table: T,
        update: U,
        tableType: KType,
        updateType: KType,
    ): SurrealResult<List<R>> {
        val paramsGenericTypeSerializer = serializer(type = updateType)
        val serializer = serializer(type = tableType) as KSerializer<R>
        val returnSerializer = ListSerializer(elementSerializer = serializer)
        return request(
            method = METHOD_MERGE,
            params = stringWithObj(string = table.tableName, param = update),
            paramsGenericTypeSerializer = paramsGenericTypeSerializer,
            returnSerializer = returnSerializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> delete(
        id: I,
        type: KType,
    ): SurrealResult<R?> {
        val serializer = (serializer(type = type) as KSerializer<R>).nullable
        return request(
            method = METHOD_DELETE,
            params = strings(id.idString),
            returnSerializer = serializer,
        )
    }

    override suspend fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> delete(
        ids: Collection<I>,
        type: KType,
    ): SurrealResult<List<R>> {
        if (ids.isEmpty()) {
            return SurrealResult.Failure(
                error = SurrealError.SDK(ex = EmptyArgListException(function = "delete(Collection<I>, KType)")),
            )
        }
        return queryMany(
            query = deleteByIdsQuery(table = ids.first().tableName),
            parameters = mapOf("ids" to ids.map { it.idString }),
            type = type,
        )
    }

    override suspend fun <R : SurrealRecord<R>, T : SurrealTable<R>> deleteAll(
        table: T,
        type: KType,
    ): SurrealResult<List<R>> {
        val serializer = serializer(type = type) as KSerializer<R>
        val returnSerializer = ListSerializer(elementSerializer = serializer)
        return request(
            method = METHOD_DELETE,
            params = strings(table.tableName),
            returnSerializer = returnSerializer,
        )
    }

    override suspend fun <T> queryOne(
        query: String,
        parameters: Map<String, Any?>,
        type: KType,
    ): SurrealResult<T> = queryMany<T>(
        query = query,
        parameters = parameters,
        type = type,
    ).map { it.singleOrNull() ?: throw NonSingleRecordResultException(recordCount = it.size) }

    override suspend fun <T> queryMany(
        query: String,
        parameters: Map<String, Any?>,
        type: KType,
    ): SurrealResult<List<T>> {
        val semicolonCount = query.count { it == ';' }
        if (query.isBlank() || (semicolonCount != 0 && !(semicolonCount == 1 && query.lastOrNull() == ';'))) {
            return SurrealResult.Failure(
                error = SurrealError.SDK(
                    ex = NonSingleStatementQueryException(statementCount = semicolonCount + 1),
                ),
            )
        }
        val returnSerializer = ListSerializer(
            elementSerializer = SurrealQueryResult.serializer(
                typeSerial0 = ListSerializer(
                    elementSerializer = serializer(type = type),
                ),
            ),
        ).nullable as KSerializer<List<SurrealQueryResult<List<T>>>?>
        return request(
            method = METHOD_QUERY,
            params = stringWithObj(string = query, param = parameters.toSerializableMap()),
            returnSerializer = returnSerializer,
        ).map {
            it?.singleOrNull()?.result?.let { value  ->
                when (value) {
                    is SurrealQueryResultValue.Data -> value.data
                    is SurrealQueryResultValue.Error -> throw value.error
                }
            } ?: throw QueryResultCountMismatchException(expected = 1, actual = it?.size ?: 0)
        }
    }

    override suspend fun query(builder: SurrealQueryScope.() -> List<SurrealResultPromise<*>>): List<SurrealResult<*>> =
        try {
            val scope = SurrealQueryScopeImpl()
            val promises = scope.builder()
            val (query, ops, params) = scope.query
            queryRaw(
                query = query,
                parameters = params,
            ).withResult(
                onSuccess = { queryResults ->
                    if (queryResults.size != ops.size) throw QueryResultCountMismatchException(expected = ops.size, actual = queryResults.size)
                    when {
                        promises === SurrealQueryScope.NONE -> emptyList()
                        promises === SurrealQueryScope.ALL -> {
                            queryResults.map(::correctSerializationErrors).zip(ops) { queryResult, statement ->
                                queryResult.result.toSurrealResult(
                                    statement = statement,
                                    registerForLiveUpdates = ::registerForLiveUpdates,
                                    unregisterFromLiveUpdates = ::unregisterFromLiveUpdates,
                                )
                            }
                        }
                        else -> {
                            promises.mapNotNull { promise ->
                                queryResults
                                    .getOrNull(index = promise.index)
                                    ?.let(::correctSerializationErrors)
                                    ?.let { queryResult ->
                                        promise.complete(
                                            response = queryResult.result,
                                            registerForLiveUpdates = ::registerForLiveUpdates,
                                            unregisterFromLiveUpdates = ::unregisterFromLiveUpdates,
                                        )
                                    }
                            }
                        }
                    } + SurrealResult.Success(value = queryResults)
                },
                onFailure = { error -> listOf(SurrealResult.Failure(error = error)) },
            )
        } catch(ex: Exception) {
            listOf<SurrealResult<*>>(SurrealResult.Failure<Any?>(error = SurrealError.SDK(ex = ex)))
        }

    override suspend fun transaction(builder: SurrealQueryScope.() -> List<SurrealResultPromise<*>>): List<SurrealResult<*>> =
        query {
            beginTransaction()
            val promises = builder()
            commitTransaction()
            promises
        }

    override suspend fun queryRaw(
        query: String,
        parameters: Map<String, Any?>,
    ): RawSurrealQueryResult =
        request(
            method = METHOD_QUERY,
            params = stringWithObj(string = query, param = parameters.toSerializableMap()),
            returnSerializer = serializer<List<SurrealQueryResult<List<JsonElement>?>>>()
        )

    override suspend fun <I, O, E> relate(
        edge: E,
        edgeType: KType,
    ): SurrealResult<E> where
        I: SurrealRecord<I>,
        O: SurrealRecord<O>,
        E: SurrealEdge<E, I, O> {
            val edgeSerializer = serializer(type = edgeType) as KSerializer<E>
            return request(
                method = METHOD_RELATE,
                params = relate(
                    inId = edge.`in`.idString,
                    edgeTable = edge.tableName,
                    outId = edge.out.idString,
                    data = edge,
                ),
                paramsGenericTypeSerializer = edgeSerializer,
                returnSerializer = edgeSerializer,
            )
        }

    override suspend fun <I, II, O, OI, E, ET> relate(
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
        ET: SurrealEdgeTable<E, I, O> =
            request(
                method = METHOD_RELATE,
                params = strings(`in`.idString, table.tableName, out.idString),
                returnSerializer = serializer(type = edgeType),
            ) as SurrealResult<E>

    override suspend fun <R : SurrealRecord<R>, T : SurrealTable<R>> live(
        table: T,
        type: KType,
    ): SurrealResult<SurrealLiveQueryResponse<R>> =
        request(
            method = METHOD_LIVE,
            params = strings(table.tableName),
            returnSerializer = serializer<SurrealLiveQueryHandle>(),
        ).map { handle -> registerForLiveUpdates(handle = handle, type = type) }

    override suspend fun kill(handle: SurrealLiveQueryHandle, force: Boolean): SurrealResult<Unit> =
        request(
            method = METHOD_KILL,
            params = strings(handle),
            returnSerializer = serializer<Unit?>(),
        ).withResult(
            onSuccess = {
                if (it != null) throw SurrealSDKException("Invalid 'kill' response: did not receive expected null!")
                unregisterFromLiveUpdates(handle = handle)
                SurrealResult.Success(value = Unit)
            },
            onFailure = { error ->
                if (force) {
                    unregisterFromLiveUpdates(handle = handle)
                }
                SurrealResult.Failure(error = error)
            }
        )

    override suspend fun <R: SurrealRecord<R>> registerForLiveUpdates(
        handle: SurrealLiveQueryHandle,
        type: KType,
    ): SurrealLiveQueryResponse<R> {
        val channel = Channel<SurrealLiveQueryUpdate<R>>()
        liveRequestsMutex.withLock {
            liveRequests[handle] = LiveRequest(
                serializer = serializer(type = type),
                updates = channel as SendChannel<SurrealLiveQueryUpdate<Any?>>,
            )
        }
        return SurrealLiveQueryResponse(
            handle = handle,
            updates = channel as ReceiveChannel<SurrealLiveQueryUpdate<R>>,
        )
    }

    override suspend fun unregisterFromLiveUpdates(handle: SurrealLiveQueryHandle) {
        liveRequestsMutex.withLock {
            liveRequests.remove(key = handle)?.also { liveRequest ->
                liveRequest.updates.close()
            }
        }
    }

    private suspend inline fun <reified T, R> request(
        method: String,
        params: RPCParams<T>,
        returnSerializer: KSerializer<R>,
    ): SurrealResult<R> {
        val (request, promise) = prepareRequest(
            method = method,
            params = params,
            returnSerializer = returnSerializer,
        )

        return try {
            sessionOrThrow
                .sendSerialized(data = request, typeInfo = typeInfo<RPCRequest<T>>())
                .handleResponse(responsePromise = promise)
        } catch (ex: Exception) {
            SurrealResult.Failure(error = SurrealError.SDK(ex = ex))
        }
    }

    private suspend inline fun <T, R> request(
        method: String,
        params: RPCParams<T>,
        paramsGenericTypeSerializer: KSerializer<T>,
        returnSerializer: KSerializer<R>,
    ): SurrealResult<R> {
        val (request, promise) = prepareRequest(
            method = method,
            params = params,
            returnSerializer = returnSerializer,
        )

        return try {
            val serializer = RPCRequest.serializer(typeSerial0 = paramsGenericTypeSerializer)
            val serializedParams = json.encodeToString(serializer = serializer, value = request)
            sessionOrThrow
                .send(content = serializedParams)
                .handleResponse(responsePromise = promise)
        } catch (ex: Exception) {
            SurrealResult.Failure(error = SurrealError.SDK(ex = ex))
        }
    }

    private suspend fun <T, R> prepareRequest(
        method: String,
        params: RPCParams<T>,
        returnSerializer: KSerializer<R>,
    ): Pair<RPCRequest<T>, CompletableDeferred<R>> {
        val id = NEXT_ID
        val request = RPCRequest(
            id = id,
            method = method,
            params = params,
        )
        val promise = CompletableDeferred<R>()
        requestsMutex.withLock {
            requests[id] = Request(
                serializer = returnSerializer,
                promise = promise,
            ) as Request<Any?>
        }

        return request to promise
    }

    private suspend fun <R> Any?.handleResponse(
        responsePromise: CompletableDeferred<R>,
    ): SurrealResult<R> =
        this?.run {
            try {
                SurrealResult.Success(value = responsePromise.await())
            } catch(ex: Throwable) {
                if (ex is DatabaseError) {
                    SurrealResult.Failure(error = SurrealError.DB(error = ex))
                } else {
                    SurrealResult.Failure(error = SurrealError.SDK(ex = ex))
                }
            }
        } ?: SurrealResult.Failure(error = SurrealError.SDK(ex = NotConnectedException()))

    private fun getByIdsQuery(table: String): String = "select * from $table where id in \$ids"

    private fun deleteByIdsQuery(table: String): String = "delete $table where id in \$ids return before"

    private val sessionOrThrow: DefaultClientWebSocketSession
        get() = ((connectionStatus.value as? ConnectionStatus.Connected) ?: throw NotConnectedException()).session

    private data class Request<T>(
        val serializer: KSerializer<T>,
        val promise: CompletableDeferred<T>,
    )

    private data class LiveRequest<T>(
        val serializer: KSerializer<T>,
        val updates: SendChannel<SurrealLiveQueryUpdate<T>>,
    )

    companion object {
        @Throws(SurrealSDKException::class, CancellationException::class)
        internal fun create(
            url: String,
            port: Int = 8000,
            onConnect: suspend Surreal.() -> Unit = {},
            requestBuilder: HttpRequestBuilder.() -> Unit = {},
            client: HttpClient = clientInstance,
            json: Json = nullSerializer,
            context: CoroutineContext = Dispatchers.IO + SupervisorJob(),
        ): SurrealImpl = SurrealImpl(
            url = url,
            port = port,
            client = client,
            json = json,
            context = context,
            onConnect = onConnect,
            requestBuilder = requestBuilder,
        )

        @Throws(SurrealSDKException::class, CancellationException::class)
        internal suspend fun create(
            url: String,
            port: Int = 8000,
            connection: SurrealConnection,
            requestBuilder: HttpRequestBuilder.() -> Unit = {},
            client: HttpClient = clientInstance,
            json: Json = nullSerializer,
            context: CoroutineContext = Dispatchers.IO + SupervisorJob(),
        ): SurrealImpl = SurrealImpl(
            url = url,
            port = port,
            client = client,
            json = json,
            context = context,
            onConnect = {
                signIn(user = connection.user, password = connection.password)
                    .then {
                        if (connection.namespace != null) {
                            query {
                                +"define namespace if not exists ${connection.namespace}"
                                +"use ns ${connection.namespace}"
                                if (connection.database != null) {
                                    +"define database if not exists ${connection.database}"
                                    +"use db ${connection.database}"
                                }
                                NONE
                            }.single().then {
                                use(namespace = connection.namespace, database = connection.database)
                            }
                        } else {
                            SurrealResult.Success(value = Unit)
                        }
                    }
            },
            requestBuilder = requestBuilder,
        ).apply {
            start()
        }

        // SurrealDB doesn't really give a good way to distinguish errors from non-errors, so we have to correct some of the incorrectly deserialized
        // query results. This correction is only possible after the fact because it involves checking the parent's status field.
        private fun correctSerializationErrors(query: RawQueryResult): RawQueryResult =
            if (query.isOk && query.result is SurrealQueryResultValue.Error) {
                query.copy(result = SurrealQueryResultValue.Data(data = listOf(query.result.raw)))
            } else {
                query
            }

        @JvmStatic
        private var NEXT_ID: RequestID = 0
            get() = field++

        private const val SURREAL_JSON_RPC_PATH = "/rpc"

        private const val METHOD_SIGN_IN = "signin"
        private const val METHOD_USE = "use"
        private const val METHOD_SELECT = "select"
        private const val METHOD_INSERT = "insert"
        private const val METHOD_UPSERT = "upsert"
        private const val METHOD_UPDATE = "update"
        private const val METHOD_MERGE = "merge"
        private const val METHOD_DELETE = "delete"
        private const val METHOD_QUERY = "query"
        private const val METHOD_RELATE = "relate"
        private const val METHOD_LIVE = "live"
        private const val METHOD_KILL = "kill"
    }
}
