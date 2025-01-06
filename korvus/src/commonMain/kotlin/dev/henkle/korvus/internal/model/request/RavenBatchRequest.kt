package dev.henkle.korvus.internal.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class RavenBatchRequest(
    @SerialName(value = "Commands")
    val commands: List<JsonElement>,
)
