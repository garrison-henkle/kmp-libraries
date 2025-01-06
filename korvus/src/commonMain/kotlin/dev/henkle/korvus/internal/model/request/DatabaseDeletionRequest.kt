package dev.henkle.korvus.internal.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseDeletionRequest(
    @SerialName(value = "DatabaseNames")
    val dbNames: List<String>,
    @SerialName(value = "HardDelete")
    val hardDelete: Boolean = true,
)
