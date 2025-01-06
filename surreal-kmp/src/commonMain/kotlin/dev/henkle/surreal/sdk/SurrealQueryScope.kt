package dev.henkle.surreal.sdk

import dev.henkle.surreal.ext.QueryPromises3
import dev.henkle.surreal.ext.QueryPromises4
import dev.henkle.surreal.ext.QueryPromises5
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KType

interface SurrealQueryScope {
    fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> get(id: I, type: KType): SurrealResultPromise<R?>

    fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> get(ids: Collection<I>, type: KType): SurrealResultPromise<List<R>>

    fun <R: SurrealRecord<R>, T: SurrealTable<R>> getAll(table: T, type: KType): SurrealResultPromise<List<R>>

    fun <R: SurrealRecord<R>> insert(record: R, type: KType): SurrealResultPromise<R>

    fun <R: SurrealRecord<R>> insert(records: Collection<R>, type: KType): SurrealResultPromise<List<R>>

    // aka upsert
    fun <R: SurrealRecord<R>> put(record: R, type: KType): SurrealResultPromise<R>

    fun <R: SurrealRecord<R>> update(record: R, type: KType): SurrealResultPromise<R?>

    fun <R: SurrealRecord<R>, T: SurrealTable<R>, U: Any> updateAll(
        table: T,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResultPromise<List<R>>

    fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>, U: Any> merge(
        id: I,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResultPromise<R?>

    fun <R: SurrealRecord<R>, T: SurrealTable<R>, U: Any> mergeAll(
        table: T,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResultPromise<List<R>>

    fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> delete(id: I, type: KType): SurrealResultPromise<R?>

    fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> delete(ids: Collection<I>, type: KType): SurrealResultPromise<List<R>>

    fun <R: SurrealRecord<R>, T: SurrealTable<R>> deleteAll(table: T, type: KType): SurrealResultPromise<List<R>>

    fun <T: Any> queryOne(
        query: String,
        parameters: Map<String, Any> = emptyMap(),
        type: KType,
    ): SurrealResultPromise<T>

    fun <T: Any> queryMany(
        query: String,
        parameters: Map<String, Any> = emptyMap(),
        type: KType
    ): SurrealResultPromise<List<T>>

    fun statement(
        statement: String,
        parameters: Map<String, Any> = emptyMap(),
        hasResult: Boolean = true
    ): SurrealResultPromise<RawSurrealStatementResult>

    fun <I, II, O, OI, E, ET> relate(
        table: ET,
        `in`: II,
        out: OI,
        edgeType: KType
    ): SurrealResultPromise<E> where I: SurrealRecord<I>,
            II: SurrealIdentifiable<I>,
            O: SurrealRecord<O>,
            OI: SurrealIdentifiable<O>,
            E: SurrealEdge<E, I, O>,
            ET: SurrealEdgeTable<E, I, O>

    fun <I: SurrealRecord<I>, O: SurrealRecord<O>, E: SurrealEdge<E, I, O>> relate(edge: E, edgeType: KType): SurrealResultPromise<E>

    fun <R: SurrealRecord<R>, T: SurrealTable<R>> live(
        table: T,
        type: KType,
    ): SurrealResultPromise<SurrealLiveQueryResponse<R>>

    fun kill(handle: SurrealLiveQueryHandle): SurrealResultPromise<Unit>

    fun beginTransaction()

    fun commitTransaction()

    operator fun String.unaryPlus(): SurrealResultPromise<RawSurrealStatementResult> =
        statement(statement = this)

    infix fun String.params(parameters: Map<String, Any>): SurrealResultPromise<RawSurrealStatementResult> =
        statement(statement = this, parameters = parameters)

    @Suppress("PropertyName")
    val ALL get() = Companion.ALL

    @Suppress("PropertyName")
    val NONE get() = Companion.NONE

    fun <A, B, C> keep(
        p1: SurrealResultPromise<A>,
        p2: SurrealResultPromise<B>,
        p3: SurrealResultPromise<C>,
    ): QueryPromises3<A, B, C> =
        QueryPromises3(
            p1 = p1,
            p2 = p2,
            p3 = p3,
        )

    fun <A, B, C, D> keep(
        p1: SurrealResultPromise<A>,
        p2: SurrealResultPromise<B>,
        p3: SurrealResultPromise<C>,
        p4: SurrealResultPromise<D>,
    ): QueryPromises4<A, B, C, D> =
        QueryPromises4(
            p1 = p1,
            p2 = p2,
            p3 = p3,
            p4 = p4,
        )

    fun <A, B, C, D, E> keep(
        p1: SurrealResultPromise<A>,
        p2: SurrealResultPromise<B>,
        p3: SurrealResultPromise<C>,
        p4: SurrealResultPromise<D>,
        p5: SurrealResultPromise<E>
    ): QueryPromises5<A, B, C, D, E> =
        QueryPromises5(
            p1 = p1,
            p2 = p2,
            p3 = p3,
            p4 = p4,
            p5 = p5,
        )

    companion object {
        val ALL = emptyList<SurrealResultPromise<*>>()
        val NONE = emptyList<SurrealResultPromise<*>>()
    }
}
