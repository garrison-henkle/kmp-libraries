package dev.henkle.surreal.sdk

import dev.henkle.surreal.types.SurrealLiveQueryUpdate
import kotlinx.coroutines.channels.ReceiveChannel

data class SurrealLiveQueryResponse<T>(
    val handle: SurrealLiveQueryHandle,
    val updates: ReceiveChannel<SurrealLiveQueryUpdate<T>>,
)
