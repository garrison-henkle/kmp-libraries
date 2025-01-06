package dev.henkle.surreal.internal.model

import dev.henkle.surreal.sdk.SurrealLiveQueryHandle
import dev.henkle.surreal.types.SurrealLiveQueryAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class LiveQueryUpdate(
    @SerialName(value = "id")
    val handle: SurrealLiveQueryHandle,
    val action: SurrealLiveQueryAction,
    val result: JsonElement,
)
