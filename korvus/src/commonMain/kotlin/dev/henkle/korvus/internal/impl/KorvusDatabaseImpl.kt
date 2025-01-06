package dev.henkle.korvus.internal.impl

import dev.henkle.korvus.KorvusConfig
import dev.henkle.korvus.KorvusDatabase
import dev.henkle.korvus.KorvusDatabaseAdmin
import dev.henkle.korvus.KorvusResult
import dev.henkle.korvus.error.KorvusError
import dev.henkle.korvus.internal.ext.parseFailure
import dev.henkle.korvus.internal.ext.toDBResults
import dev.henkle.korvus.internal.ext.toJsonSafe
import dev.henkle.korvus.internal.ext.url
import dev.henkle.korvus.internal.model.request.RavenBatchRequest
import dev.henkle.korvus.internal.model.request.RavenQueryOpRequest
import dev.henkle.korvus.internal.model.request.RavenQueryRequest
import dev.henkle.korvus.internal.model.response.RavenBatchResponse
import dev.henkle.korvus.internal.model.response.RavenGetResponse
import dev.henkle.korvus.internal.model.response.RavenQueryOpResponse
import dev.henkle.korvus.internal.model.response.RavenQueryResponse
import dev.henkle.korvus.internal.utils.client
import dev.henkle.korvus.internal.utils.nullSerializer
import dev.henkle.korvus.map
import dev.henkle.korvus.types.BatchCommandScope
import dev.henkle.korvus.types.DBResult
import dev.henkle.korvus.types.QueryOneWithIncludesResult
import dev.henkle.korvus.types.QueryResult
import dev.henkle.korvus.types.TypedBatchCommandScope
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.reflect.KType

/**
 * An implementation of [KorvusDatabase] that delegates all calls to batch calls to match Raven.Studio.
 */
internal class KorvusDatabaseImpl(
    override val name: String,
    override val replicationFactor: Int = 1,
    private val baseUrl: String,
    private val dbAdmin: KorvusDatabaseAdmin,
    private val config: KorvusConfig,
) : KorvusDatabase {
    override suspend fun createDatabase(): KorvusResult<Unit> =
        dbAdmin.create(name = name, replicationFactor = replicationFactor).map {}

    override suspend fun deleteDatabase(hardDelete: Boolean): KorvusResult<Unit> = dbAdmin.delete(name, hardDelete = hardDelete)

    override suspend fun <T : Any> put(
        document: T,
        id: String,
        changeVector: String?,
        type: KType
    ): KorvusResult<DBResult.Put> = typedBatch(type = type) {
        put(document = document, id = id, changeVector = changeVector)
    }.map { it.first() as DBResult.Put }

    override suspend fun <T : Any> put(
        documents: Collection<T>,
        ids: Collection<String>,
        changeVectors: Collection<String?>,
        type: KType
    ): KorvusResult<List<DBResult.Put>> = typedBatch(type = type) {
        put(documents = documents, ids = ids, changeVectors = changeVectors)
    }.map { results -> results.map { result -> result as DBResult.Put } }

    override suspend fun <T : Any> delete(
        id: String,
        changeVector: String?,
        type: KType,
    ): KorvusResult<DBResult.Delete> = typedBatch<T>(type = type) {
        delete(id = id, changeVector = changeVector)
    }.map { it.first() as DBResult.Delete }

    override suspend fun <T : Any> delete(
        ids: Collection<String>,
        changeVectors: Collection<String?>,
        type: KType
    ): KorvusResult<List<DBResult.Delete>> = typedBatch<T>(type = type) {
        delete(ids = ids, changeVectors = changeVectors)
    }.map { results -> results.map { result -> result as DBResult.Delete } }

    override suspend fun <T : Any> deleteByIDPrefix(
        prefix: String,
        type: KType,
    ): KorvusResult<DBResult.Delete> = typedBatch<T>(type = type) {
        deleteByIDPrefix(prefix = prefix)
    }.map { it.first() as DBResult.Delete }

    override suspend fun <T : Any> patch(
        id: String,
        patchScript: String,
        arguments: Map<String, Any?>,
        changeVector: String?,
        type: KType
    ): KorvusResult<DBResult.Patch> = typedBatch<T>(type = type) {
        patch<T>(
            id = id,
            patchScript = patchScript,
            arguments = arguments,
            changeVector = changeVector,
            type = type,
        )
    }.map { it.first() as DBResult.Patch }

    override suspend fun <T : Any> patch(
        ids: Collection<String>,
        patchScripts: Collection<String>,
        arguments: Collection<Map<String, Any?>>,
        changeVectors: Collection<String?>,
        type: KType
    ): KorvusResult<List<DBResult.Patch>> = typedBatch<T>(type = type) {
        patch<T>(
            ids = ids,
            patchScripts = patchScripts,
            arguments = arguments,
            changeVectors = changeVectors,
            type = type,
        )
    }.map { results -> results.map { it as DBResult.Patch } }

    override suspend fun <T : Any> patch(
        ids: Collection<String>,
        patchScript: String,
        arguments: Collection<Map<String, Any?>>,
        changeVectors: Collection<String?>,
        type: KType
    ): KorvusResult<List<DBResult.Patch>> = typedBatch<T>(type = type) {
        patch<T>(
            ids = ids,
            patchScript = patchScript,
            arguments = arguments,
            changeVectors = changeVectors,
            type = type,
        )
    }.map { results -> results.map { it as DBResult.Patch } }

    override suspend fun batch(
        commandsBlock: suspend BatchCommandScope.() -> Unit,
    ): KorvusResult<List<DBResult>> = try {
        val request = BatchCommandScopeImpl()
            .apply { commandsBlock() }
            .request
        batch(request = request)
    } catch(ex: Exception) {
        KorvusResult.Failure(error = KorvusError.SDK(ex = ex))
    }

    override suspend fun <T: Any> typedBatch(
        type: KType,
        commandsBlock: suspend TypedBatchCommandScope<T>.() -> Unit,
    ): KorvusResult<List<DBResult>> = try {
        val request = TypedBatchCommandScopeImpl<T>(type = type)
            .apply { commandsBlock() }
            .request
        batch(request = request)
    } catch(ex: Exception) {
        KorvusResult.Failure(error = KorvusError.SDK(ex = ex))
    }

    @JvmName(name = "executeRavenBatchRequest")
    private suspend fun batch(request: RavenBatchRequest): KorvusResult<List<DBResult>> {
        val response = client.request {
            method = HttpMethod.Post
            url(baseUrl, "databases", name, "bulk_docs")
            contentType(type = ContentType.Application.Json)
            setBody(body = request)
        }
        return when(response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.Created -> {
                val results = response.body<RavenBatchResponse>().results.toDBResults()
                KorvusResult.Success(result = results)
            }
            else -> response.parseFailure()
        }
    }

    override suspend fun <T: Any> get(id: String, type: KType): KorvusResult<T?> =
        getByID<T>(ids = listOf(id), type = type).map {
            if (it.size > 1) {
                throw IllegalStateException("getByID returned more than one result!")
            } else {
                it.firstOrNull()
            }
        }

    override suspend fun <T: Any> get(ids: Collection<String>, type: KType): KorvusResult<List<T>> =
        getByID(ids = ids, type = type)

    override suspend fun <T: Any> getAll(collection: String, type: KType): KorvusResult<List<T>> =
        getCollection(collection = collection, type = type)

    private suspend fun <T: Any> getCollection(
        collection: String,
        type: KType,
    ): KorvusResult<List<T>> {
        val results = queryMany<T>(
            query = "from $collection",
            type = type,
        )

        return results.map { queryResult -> queryResult.results }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T: Any> getByID(
        ids: Collection<String>,
        type: KType,
        ): KorvusResult<List<T>> {
        if (ids.isEmpty()) {
            return KorvusResult.Failure(
                error = KorvusError.SDK(
                    ex = IllegalArgumentException("At least one id must be passed to getByID!"),
                ),
            )
        }
        val response = client.request {
            method = HttpMethod.Get
            url(baseUrl, "databases", name, "docs")
            for (id in ids) {
                parameter(key = "id", value = id)
            }
            accept(contentType = ContentType.Application.Json)
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val jsonString = response.bodyAsText()
                println(jsonString)
                val serializer = RavenGetResponse.serializer(typeSerial0 = serializer(type = type))
                val results = nullSerializer.decodeFromString(
                    deserializer = serializer,
                    string = jsonString,
                ).results as List<T>
                KorvusResult.Success(result = results)
            }
            else -> response.parseFailure()
        }
    }

    override suspend fun <T: Any> queryOne(
        query: String,
        parameters: Map<String, Any>,
        type: KType,
    ): KorvusResult<T> = queryMany<T>(
        query = query,
        parameters = parameters,
        type = type,
    ).map { result ->
        if (result.cappedMaxResultCount != 1 && result.totalResultCount != 1) {
            throw IllegalStateException("queryOne of '$query' returned more than one result!")
        }
        result.results.first()
    }

    override suspend fun <T: Any> queryOneWithIncludes(
        query: String,
        parameters: Map<String, Any>,
        type: KType,
    ): KorvusResult<QueryOneWithIncludesResult<T>> = queryMany<T>(
        query = query,
        parameters = parameters,
        type = type,
    ).map { result ->
        if (result.cappedMaxResultCount != 1 && result.totalResultCount != 1) {
            throw IllegalStateException("queryOneWithIncludes of '$query' returned more than one result!")
        }
        QueryOneWithIncludesResult(
            result = result.results.first(),
            includes = result.includes,
        )
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T: Any> queryMany(
        query: String,
        parameters: Map<String, Any?>,
        type: KType,
        start: Int?,
        pageSize: Int?,
    ): KorvusResult<QueryResult<T>> {
        val request = RavenQueryRequest(
            query = query,
            queryParameters = parameters.toJsonSafe(),
            start = start,
            pageSize = pageSize,
        )
        val response = client.request {
            method = HttpMethod.Post
            url(baseUrl, "databases", name, "queries")
            contentType(type = ContentType.Application.Json)
            setBody(request)
        }
        return when (response.status) {
            HttpStatusCode.OK -> {
                val jsonString = response.bodyAsText()
                val serializer = RavenQueryResponse.serializer(
                    typeSerial0 = serializer(type = type),
                )
                val queryResult = nullSerializer.decodeFromString(
                    deserializer = serializer,
                    string = jsonString,
                )
                val results = queryResult.results as List<T>
                KorvusResult.Success(
                    result = QueryResult(
                        results = results,
                        includes = queryResult.includes,
                        durationMs = queryResult.durationMs,
                        totalResultCount = queryResult.totalResults,
                        skippedResultCount = queryResult.skippedResults,
                        cappedMaxResultCount = queryResult.cappedMaxResults,
                        scannedResultCount = queryResult.scannedResults,
                    ),
                )
            }
            else -> response.parseFailure()
        }
    }

    override suspend fun deleteByQuery(
        query: String,
        parameters: Map<String, Any?>,
    ): KorvusResult<Unit> = queryOperation(
        query = query,
        parameters = parameters,
        method = HttpMethod.Delete,
    )

    override suspend fun patchByQuery(
        query: String,
        parameters: Map<String, Any?>,
    ): KorvusResult<Unit> = queryOperation(
        query = query,
        parameters = parameters,
        method = HttpMethod.Patch,
    )

    private suspend fun queryOperation(
        query: String,
        parameters: Map<String, Any?>,
        method: HttpMethod,
    ): KorvusResult<Unit> {
        val request = RavenQueryOpRequest(
            query = query,
            queryParameters = parameters.toJsonSafe(),
        )
        val response = client.request {
            this.method = method
            url(baseUrl, "databases", name, "queries")
            contentType(type = ContentType.Application.Json)
            setBody(request)
        }
        return when (response.status) {
            HttpStatusCode.OK ->
                KorvusResult.Success(result = response.body<RavenQueryOpResponse>()).map {}
            else -> response.parseFailure()
        }
    }
}
