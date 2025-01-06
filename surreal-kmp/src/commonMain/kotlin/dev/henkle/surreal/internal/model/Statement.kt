package dev.henkle.surreal.internal.model

import dev.henkle.surreal.sdk.SurrealLiveQueryHandle
import kotlin.reflect.KType

sealed interface Statement {
    interface HasType {
        val type: KType
    }

    interface Typed : Statement, HasType
    interface TypedNullable : Statement, HasType
    interface TypedList : Statement, HasType

    data class GetById(override val type: KType): TypedNullable
    data class GetByIds(override val type: KType): TypedList
    data class GetAll(override val type: KType): TypedList
    data class Insert(override val type: KType): Typed
    data class BulkInsert(override val type: KType): TypedList
    data class Put(override val type: KType): Typed
    data class Update(override val type: KType): TypedNullable
    data class UpdateAll(override val type: KType): TypedList
    data class Merge(override val type: KType): TypedNullable
    data class MergeAll(override val type: KType): TypedList
    data class DeleteById(override val type: KType): TypedNullable
    data class DeleteByIds(override val type: KType): TypedList
    data class DeleteAll(override val type: KType): TypedList
    data class QueryOne(override val type: KType): Typed
    data class QueryMany(override val type: KType): TypedList
    data object RawStatement: Statement
    data class Relate(override val type: KType): Typed
    data class Live(val type: KType): Statement
    data class Kill(val handle: SurrealLiveQueryHandle) : Statement
}
