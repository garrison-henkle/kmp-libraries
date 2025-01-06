package dev.henkle.surreal.internal.utils.ext

import dev.henkle.surreal.errors.NonSingleRecordResultException
import dev.henkle.surreal.errors.SurrealError
import dev.henkle.surreal.internal.model.Statement
import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.sdk.RawSurrealStatementResult
import dev.henkle.surreal.sdk.SurrealLiveQueryHandle
import dev.henkle.surreal.sdk.SurrealLiveQueryResponse
import dev.henkle.surreal.sdk.SurrealQueryResultValue
import dev.henkle.surreal.sdk.SurrealResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
internal suspend fun <T> SurrealQueryResultValue<RawSurrealStatementResult>.toSurrealResult(
    statement: Statement,
    registerForLiveUpdates: suspend (handle: SurrealLiveQueryHandle, type: KType) -> SurrealLiveQueryResponse<*>,
    unregisterFromLiveUpdates: suspend (handle: SurrealLiveQueryHandle) -> Unit
): SurrealResult<T> =
    try {
        when (this) {
            is SurrealQueryResultValue.Data -> {
                when (statement) {
                    is Statement.Typed -> {
                        data?.singleOrNull()
                            ?.let { jsonElement ->
                                SurrealResult.Success(
                                    value = nullSerializer.decodeFromJsonElement(
                                        deserializer = serializer(type = statement.type),
                                        element = jsonElement,
                                    ),
                                ) as SurrealResult.Success<T>
                            } ?: throw NonSingleRecordResultException(recordCount = data?.size ?: 0)
                    }
                    is Statement.TypedNullable -> {
                        data?.size?.takeIf { it != 0 }?.let { size ->
                            if (size == 1) {
                                SurrealResult.Success(
                                    value = nullSerializer.decodeFromJsonElement(
                                        deserializer =
                                            (serializer(type = statement.type) as KSerializer<Any>).nullable,
                                        element = data!!.first(),
                                    ),
                                ) as SurrealResult.Success<T>
                            } else {
                                throw NonSingleRecordResultException(recordCount = data?.size ?: 0)
                            }
                        } ?: SurrealResult.Success(null) as SurrealResult.Success<T>
                    }
                    is Statement.TypedList -> {
                        SurrealResult.Success(
                            value = nullSerializer.decodeFromJsonElement(
                                deserializer = ListSerializer(elementSerializer = serializer(type = statement.type)),
                                element = JsonArray(content = data ?: emptyList()),
                            ),
                        ) as SurrealResult.Success<T>
                    }
                    is Statement.RawStatement -> SurrealResult.Success(value = this.data) as SurrealResult.Success<T>
                    is Statement.Live -> {
                        val handle = data?.singleOrNull()?.let { json ->
                            nullSerializer.decodeFromJsonElement<SurrealLiveQueryHandle>(json = json)
                        } ?: throw NonSingleRecordResultException(recordCount = data?.size ?: 0)
                        val response = registerForLiveUpdates(handle, statement.type)
                        SurrealResult.Success(value = response) as SurrealResult.Success<T>
                    }
                    is Statement.Kill -> {
                        val response = if (data == null) {
                            Unit
                        } else {
                            throw NonSingleRecordResultException(recordCount = data?.size ?: 0)
                        }
                        unregisterFromLiveUpdates(statement.handle)
                        SurrealResult.Success(value = response) as SurrealResult.Success<T>
                    }

                }
            }
            is SurrealQueryResultValue.Error -> SurrealResult.Failure(error = SurrealError.DB(error = error))
        }

    } catch (ex: Exception) {
        SurrealResult.Failure<Unit>(error = SurrealError.SDK(ex = ex)) as SurrealResult.Failure<T>
    }
