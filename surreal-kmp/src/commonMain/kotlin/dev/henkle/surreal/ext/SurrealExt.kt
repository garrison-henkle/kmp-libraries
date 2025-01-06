package dev.henkle.surreal.ext

import dev.henkle.surreal.Surreal
import dev.henkle.surreal.errors.QueryResultCountMismatchException
import dev.henkle.surreal.errors.SurrealError
import dev.henkle.surreal.errors.SurrealSDKException
import dev.henkle.surreal.sdk.RawSurrealQueryResult
import dev.henkle.surreal.sdk.SurrealLiveQueryResponse
import dev.henkle.surreal.sdk.SurrealQueryScope
import dev.henkle.surreal.sdk.SurrealResult
import dev.henkle.surreal.sdk.SurrealResultPromise
import dev.henkle.surreal.sdk.map
import dev.henkle.surreal.sdk.mapError
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.utils.printlnToStdErr
import kotlin.jvm.JvmName
import kotlin.reflect.typeOf

suspend inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> Surreal.get(id: I): SurrealResult<R?> =
    get(id = id, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> Surreal.get(ids: Collection<I>): SurrealResult<List<R>> =
    get(ids = ids, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>> Surreal.getAll(table: T): SurrealResult<List<R>> =
    getAll(table = table, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>> Surreal.insert(record: R): SurrealResult<R> =
    insert(record = record, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>> Surreal.insert(records: Collection<R>): SurrealResult<List<R>> =
    insert(records = records, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>> Surreal.put(record: R): SurrealResult<R> =
    put(record = record, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>> Surreal.update(record: R): SurrealResult<R?> =
    update(record = record, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>, reified U: Any> Surreal.updateAll(
    table: T,
    update: U,
): SurrealResult<List<R>> =
    updateAll(table = table, update = update, tableType = typeOf<R>(), updateType = typeOf<U>())

suspend inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>, reified U: Any> Surreal.merge(
    id: I,
    update: U,
): SurrealResult<R?> =
    merge(id = id, update = update, recordType = typeOf<R>(), updateType = typeOf<U>())

suspend inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>, reified U: Any> Surreal.mergeAll(
    table: T,
    update: U,
): SurrealResult<List<R>> =
    mergeAll(table = table, update = update, tableType = typeOf<R>(), updateType = typeOf<U>())

suspend inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> Surreal.delete(id: I): SurrealResult<R?> =
    delete(id = id, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> Surreal.delete(ids: Collection<I>): SurrealResult<List<R>> =
    delete(ids = ids, type = typeOf<R>())

suspend inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>> Surreal.deleteAll(table: T): SurrealResult<List<R>> =
    deleteAll(table = table, type = typeOf<R>())

suspend inline fun <reified T> Surreal.queryOne(query: String, parameters: Map<String, Any?> = emptyMap()): SurrealResult<T> =
    queryOne(query = query, parameters = parameters, type = typeOf<T>())

suspend inline fun <reified T> Surreal.queryMany(query: String, parameters: Map<String, Any?> = emptyMap()): SurrealResult<List<T>> =
    queryMany(query = query, parameters = parameters, type = typeOf<T>())

data class QueryResult1<A>(val r1: SurrealResult<A>, val results: RawSurrealQueryResult)
data class QueryResult2<A, B>(val r1: SurrealResult<A>, val r2: SurrealResult<B>, val results: RawSurrealQueryResult)
data class QueryResult3<A, B, C>(
    val r1: SurrealResult<A>,
    val r2: SurrealResult<B>,
    val r3: SurrealResult<C>,
    val results: RawSurrealQueryResult,
)
data class QueryResult4<A, B, C, D>(
    val r1: SurrealResult<A>,
    val r2: SurrealResult<B>,
    val r3: SurrealResult<C>,
    val r4: SurrealResult<D>,
    val results: RawSurrealQueryResult,
)
data class QueryResult5<A, B, C, D, E>(
    val r1: SurrealResult<A>,
    val r2: SurrealResult<B>,
    val r3: SurrealResult<C>,
    val r4: SurrealResult<D>,
    val r5: SurrealResult<E>,
    val results: RawSurrealQueryResult,
)

data class QueryPromises3<A, B, C>(
    val p1: SurrealResultPromise<A>,
    val p2: SurrealResultPromise<B>,
    val p3: SurrealResultPromise<C>,
)
data class QueryPromises4<A, B, C, D>(
    val p1: SurrealResultPromise<A>,
    val p2: SurrealResultPromise<B>,
    val p3: SurrealResultPromise<C>,
    val p4: SurrealResultPromise<D>,
)
data class QueryPromises5<A, B, C, D, E>(
    val p1: SurrealResultPromise<A>,
    val p2: SurrealResultPromise<B>,
    val p3: SurrealResultPromise<C>,
    val p4: SurrealResultPromise<D>,
    val p5: SurrealResultPromise<E>,
)

inline fun countMismatch(expected: Int, actual: Int): QueryResultCountMismatchException =
    QueryResultCountMismatchException(expected = expected, actual = actual)

@JvmName(name = "query1")
suspend inline fun <A> Surreal.query(
    crossinline builder: SurrealQueryScope.() -> SurrealResultPromise<A>,
): QueryResult1<A> =
    query {
        listOf(builder())
    }.let(::toQueryResult1)

@JvmName(name = "query2")
suspend inline fun <A, B> Surreal.query(
    crossinline builder: SurrealQueryScope.() -> Pair<SurrealResultPromise<A>, SurrealResultPromise<B>>,
): QueryResult2<A, B> =
    query {
        val promises = builder()
        listOf(promises.first, promises.second)
    }.let(::toQueryResult2)

@JvmName(name = "query3")
suspend inline fun <A, B, C> Surreal.query(
    crossinline builder: SurrealQueryScope.() -> QueryPromises3<A, B, C>,
): QueryResult3<A, B, C> =
    query {
        val promises = builder()
        listOf(promises.p1, promises.p2, promises.p3)
    }.let(::toQueryResult3)

@JvmName(name = "query4")
suspend inline fun <A, B, C, D> Surreal.query(
    crossinline builder: SurrealQueryScope.() -> QueryPromises4<A, B, C, D>,
): QueryResult4<A, B, C, D> =
    query {
        val promises = builder()
        listOf(promises.p1, promises.p2, promises.p3, promises.p4)
    }.let(::toQueryResult4)

@JvmName(name = "query5")
suspend inline fun <A, B, C, D, E> Surreal.query(
    crossinline builder: SurrealQueryScope.() -> QueryPromises5<A, B, C, D, E>,
): QueryResult5<A, B, C, D, E> =
    query {
        val promises = builder()
        listOf(promises.p1, promises.p2, promises.p3, promises.p4, promises.p5)
    }.let(::toQueryResult5)

@JvmName(name = "transaction1")
suspend inline fun <A> Surreal.transaction(
    crossinline builder: SurrealQueryScope.() -> SurrealResultPromise<A>,
): QueryResult1<A> =
    transaction {
        listOf(builder())
    }.let(::toQueryResult1)

@JvmName(name = "transaction2")
suspend inline fun <A, B> Surreal.transaction(
    crossinline builder: SurrealQueryScope.() -> Pair<SurrealResultPromise<A>, SurrealResultPromise<B>>,
): QueryResult2<A, B> =
    transaction {
        val promises = builder()
        listOf(promises.first, promises.second)
    }.let(::toQueryResult2)

@JvmName(name = "transaction3")
suspend inline fun <A, B, C> Surreal.transaction(
    crossinline builder: SurrealQueryScope.() -> QueryPromises3<A, B, C>,
): QueryResult3<A, B, C> =
    transaction {
        val promises = builder()
        listOf(promises.p1, promises.p2, promises.p3)
    }.let(::toQueryResult3)

@JvmName(name = "transaction4")
suspend inline fun <A, B, C, D> Surreal.transaction(
    crossinline builder: SurrealQueryScope.() -> QueryPromises4<A, B, C, D>,
): QueryResult4<A, B, C, D> =
    transaction {
        val promises = builder()
        listOf(promises.p1, promises.p2, promises.p3, promises.p4)
    }.let(::toQueryResult4)

@JvmName(name = "transaction5")
suspend inline fun <A, B, C, D, E> Surreal.transaction(
    crossinline builder: SurrealQueryScope.() -> QueryPromises5<A, B, C, D, E>,
): QueryResult5<A, B, C, D, E> =
    transaction {
        val promises = builder()
        listOf(promises.p1, promises.p2, promises.p3, promises.p4, promises.p5)
    }.let(::toQueryResult5)

@Suppress("UNCHECKED_CAST")
inline fun <A> toQueryResult1(results: List<SurrealResult<*>>): QueryResult1<A> {
    val expectedCount = 2
    val firstResult = results.firstOrNull()
    return when {
        results.size == 1 && firstResult is SurrealResult.Failure && firstResult.error is SurrealError.SDK -> {
            QueryResult1(
                r1 = firstResult.mapError(),
                results = firstResult.mapError(),
            )
        }
        results.size == expectedCount -> {
            QueryResult1(
                r1 = results[0] as SurrealResult<A>,
                results = results[1] as RawSurrealQueryResult,
            )
        }
        else -> {
            QueryResult1(
                results = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r1 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <A, B> toQueryResult2(results: List<SurrealResult<*>>): QueryResult2<A, B> {
    val expectedCount = 3
    val firstResult = results.firstOrNull()
    return when {
        results.size == 1 && firstResult is SurrealResult.Failure && firstResult.error is SurrealError.SDK -> {
            QueryResult2(
                r1 = firstResult.mapError(),
                r2 = firstResult.mapError(),
                results = firstResult.mapError(),
            )
        }
        results.size == expectedCount -> {
            QueryResult2(
                r1 = results[0] as SurrealResult<A>,
                r2 = results[1] as SurrealResult<B>,
                results = results[2] as RawSurrealQueryResult,
            )
        }
        else -> {
            QueryResult2(
                results = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r1 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r2 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <A, B, C> toQueryResult3(results: List<SurrealResult<*>>): QueryResult3<A, B, C> {
    val expectedCount = 4
    val firstResult = results.firstOrNull()
    return when {
        results.size == 1 && firstResult is SurrealResult.Failure && firstResult.error is SurrealError.SDK -> {
            QueryResult3(
                r1 = firstResult.mapError(),
                r2 = firstResult.mapError(),
                r3 = firstResult.mapError(),
                results = firstResult.mapError(),
            )
        }
        results.size == expectedCount -> {
            QueryResult3(
                r1 = results[0] as SurrealResult<A>,
                r2 = results[1] as SurrealResult<B>,
                r3 = results[2] as SurrealResult<C>,
                results = results[3] as RawSurrealQueryResult,
            )
        }
        else -> {
            QueryResult3(
                results = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r1 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r2 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r3 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <A, B, C, D> toQueryResult4(results: List<SurrealResult<*>>): QueryResult4<A, B, C, D> {
    val expectedCount = 5
    val firstResult = results.firstOrNull()
    return when {
        results.size == 1 && firstResult is SurrealResult.Failure && firstResult.error is SurrealError.SDK -> {
            QueryResult4(
                r1 = firstResult.mapError(),
                r2 = firstResult.mapError(),
                r3 = firstResult.mapError(),
                r4 = firstResult.mapError(),
                results = firstResult.mapError(),
            )
        }
        results.size == expectedCount -> {
            QueryResult4(
                r1 = results[0] as SurrealResult<A>,
                r2 = results[1] as SurrealResult<B>,
                r3 = results[2] as SurrealResult<C>,
                r4 = results[3] as SurrealResult<D>,
                results = results[4] as RawSurrealQueryResult,
            )
        }
        else -> {
            QueryResult4(
                results = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r1 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r2 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r3 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r4 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <A, B, C, D, E> toQueryResult5(results: List<SurrealResult<*>>): QueryResult5<A, B, C, D, E> {
    val expectedCount = 6
    val firstResult = results.firstOrNull()
    return when {
        results.size == 1 && firstResult is SurrealResult.Failure && firstResult.error is SurrealError.SDK -> {
            QueryResult5(
                r1 = firstResult.mapError(),
                r2 = firstResult.mapError(),
                r3 = firstResult.mapError(),
                r4 = firstResult.mapError(),
                r5 = firstResult.mapError(),
                results = firstResult.mapError(),
            )
        }
        results.size == expectedCount -> {
            QueryResult5(
                r1 = results[0] as SurrealResult<A>,
                r2 = results[1] as SurrealResult<B>,
                r3 = results[2] as SurrealResult<C>,
                r4 = results[3] as SurrealResult<D>,
                r5 = results[4] as SurrealResult<E>,
                results = results[5] as RawSurrealQueryResult,
            )
        }
        else -> {
            QueryResult5(
                results = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r1 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r2 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r3 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r4 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
                r5 = SurrealResult.Failure(
                    error = SurrealError.SDK(ex = countMismatch(expected = expectedCount, actual = results.size)),
                ),
            )
        }
    }
}

suspend inline fun <reified E, I, O> Surreal.relate(
    edge: E,
): SurrealResult<E> where
    I: SurrealRecord<I>,
    O: SurrealRecord<O>,
    E: SurrealEdge<E, I, O> =
        relate(edge = edge, edgeType = typeOf<E>())

suspend inline fun <reified E, ET, I, II, O, OI> Surreal.relate(
    table: ET,
    `in`: II,
    `out`: OI,
): SurrealResult<E> where
    I: SurrealRecord<I>,
    II: SurrealIdentifiable<I>,
    O: SurrealRecord<O>,
    OI: SurrealIdentifiable<O>,
    E: SurrealEdge<E, I, O>,
    ET: SurrealEdgeTable<E, I, O> =
        relate(table = table, `in` = `in`, out = out, edgeType = typeOf<E>())

suspend inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>> Surreal.live(
    table: T,
): SurrealResult<SurrealLiveQueryResponse<R>> = live(table = table, type = typeOf<R>())
