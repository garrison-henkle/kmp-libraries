package dev.henkle.korvus.internal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RavenSize(
    @SerialName(value = "HumaneSize")
    val humanReadable: String,
    @SerialName(value = "SizeInBytes")
    val bytes: Long,
)
