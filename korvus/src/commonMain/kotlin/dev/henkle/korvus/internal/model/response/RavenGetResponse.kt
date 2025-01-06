package dev.henkle.korvus.internal.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RavenGetResponse<T: Any>(
    @SerialName(value = "Results")
    val results: List<T>,
)
