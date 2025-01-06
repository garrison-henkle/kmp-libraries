package dev.henkle.korvus.internal.model.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class RavenQueryRequest(
    @SerialName(value = "Query")
    val query: String,
    @SerialName(value = "QueryParameters")
    val queryParameters: Map<String, JsonElement> = emptyMap(),
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "Start")
    val start: Int? = null,
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "PageSize")
    val pageSize: Int? = null,
    @SerialName(value = "ProjectionBehavior")
    val projectionBehavior: String = "Default",
)
