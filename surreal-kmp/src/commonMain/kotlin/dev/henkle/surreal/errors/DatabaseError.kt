package dev.henkle.surreal.errors

import kotlinx.serialization.Serializable

/**
 * An error returned by the SurrealDB API
 */
@Serializable
data class DatabaseError(
    val code: Long = CODE_UNKNOWN,
    override val message: String,
) : Exception(message) {
    val isCollision: Boolean get() = message.endsWith(suffix = "` already exists")
    val isRolledBack: Boolean get() = message == "The query was not executed due to a failed transaction"

    companion object {
        const val CODE_UNKNOWN = -1L
    }
}
