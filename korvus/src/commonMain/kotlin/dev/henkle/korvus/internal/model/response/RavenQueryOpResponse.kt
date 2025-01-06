package dev.henkle.korvus.internal.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RavenQueryOpResponse(
    @SerialName(value = "OperationId")
    val operationId: Long,
    @SerialName(value = "OperationNodeTag")
    val operationNodeTag: String,
)
