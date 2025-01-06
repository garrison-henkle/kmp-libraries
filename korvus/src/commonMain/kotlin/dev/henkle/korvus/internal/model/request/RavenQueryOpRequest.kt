package dev.henkle.korvus.internal.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RavenQueryOpRequest(
    @SerialName(value = "Query")
    val query: String,
    @SerialName(value = "QueryParameters")
    val queryParameters: Map<String, JsonElement> = emptyMap(),
)
