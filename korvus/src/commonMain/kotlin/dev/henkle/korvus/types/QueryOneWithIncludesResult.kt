package dev.henkle.korvus.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class QueryOneWithIncludesResult<T: Any>(
    val result: T,
    val includes: Map<String, JsonObject>,
)
