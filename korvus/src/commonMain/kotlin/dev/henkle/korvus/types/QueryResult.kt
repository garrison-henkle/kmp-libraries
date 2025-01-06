package dev.henkle.korvus.types

import kotlinx.serialization.json.JsonObject

data class QueryResult<T: Any>(
    val results: List<T>,
    val includes: Map<String, JsonObject>,
    val totalResultCount: Int,
    val cappedMaxResultCount: Int,
    val skippedResultCount: Int,
    val scannedResultCount: Int,
    val durationMs: Long,
)
