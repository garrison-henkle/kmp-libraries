package dev.henkle.surreal.sdk

import kotlinx.serialization.Serializable

@Serializable
data class SurrealQueryResult<T>(
    val status: String,
    val time: String,
    val result: SurrealQueryResultValue<T>,
) {
    val isOk: Boolean get() = status == STATUS_OK
    val isError: Boolean get() = status == STATUS_ERROR

    companion object {
        private const val STATUS_OK = "OK"
        private const val STATUS_ERROR = "ERR"
    }
}
