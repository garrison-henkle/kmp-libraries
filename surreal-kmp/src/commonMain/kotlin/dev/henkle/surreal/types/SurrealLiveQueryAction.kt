package dev.henkle.surreal.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SurrealLiveQueryAction {
    @SerialName(value = "CREATE")
    Create,

    @SerialName(value = "UPDATE")
    Update,

    @SerialName(value = "DELETE")
    Delete,
}
