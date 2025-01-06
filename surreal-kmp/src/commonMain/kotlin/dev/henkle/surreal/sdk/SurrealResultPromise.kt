package dev.henkle.surreal.sdk

import dev.henkle.surreal.internal.model.Statement
import dev.henkle.surreal.internal.utils.ext.toSurrealResult
import kotlin.reflect.KType

class SurrealResultPromise<T>(private val statement: Statement, internal val index: Int) {
    internal suspend fun complete(
        response: SurrealQueryResultValue<RawSurrealStatementResult>,
        registerForLiveUpdates: suspend (handle: SurrealLiveQueryHandle, type: KType) -> SurrealLiveQueryResponse<*>,
        unregisterFromLiveUpdates: suspend (handle: SurrealLiveQueryHandle) -> Unit
    ): SurrealResult<T> =
        response.toSurrealResult(
            statement = statement,
            registerForLiveUpdates = registerForLiveUpdates,
            unregisterFromLiveUpdates = unregisterFromLiveUpdates,
        )
}
