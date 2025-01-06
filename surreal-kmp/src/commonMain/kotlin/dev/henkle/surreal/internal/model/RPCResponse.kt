package dev.henkle.surreal.internal.model

import dev.henkle.surreal.errors.DatabaseError
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class RPCResponse(
    val id: RequestID = LIVE_QUERY_UPDATE,
    val result: JsonElement? = null,
    val error: DatabaseError? = null,
) {
    val isLiveQueryUpdate: Boolean = id == LIVE_QUERY_UPDATE
    companion object {
        const val LIVE_QUERY_UPDATE = -1L
    }
}
