package dev.henkle.korvus.internal.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * @property cappedMaxResults if using limit, it is <= the max limit
 */
@Serializable
data class RavenQueryResponse<T>(
    @SerialName(value = "Results")
    val results: List<T>,
    @SerialName(value = "TotalResults")
    val totalResults: Int,
    @SerialName(value = "CappedMaxResults")
    val cappedMaxResults: Int = -1,
    @SerialName(value = "ScannedResults")
    val scannedResults: Int = -1,
    @SerialName(value = "SkippedResults")
    val skippedResults: Int,
    @SerialName(value = "DurationInMs")
    val durationMs: Long,
    @SerialName(value = "IndexName")
    val indexName: String,
    @SerialName(value = "Includes")
    val includes: Map<String, JsonObject> = emptyMap(),
    @SerialName(value = "IncludedPaths")
    val includedPaths: List<String>? = null,
    @SerialName(value = "IndexTimestamp")
    val indexTimestamp: String,
    @SerialName(value = "LastQueryTime")
    val lastQueryTime: String,
    @SerialName(value = "IsStale")
    val isStale: Boolean,
    @SerialName(value = "ResultEtag")
    val resultETag: Long,
    @SerialName(value = "NodeTag")
    val nodeTag: String,
)
