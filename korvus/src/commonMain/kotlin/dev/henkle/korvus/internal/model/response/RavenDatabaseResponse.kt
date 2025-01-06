package dev.henkle.korvus.internal.model.response

import dev.henkle.korvus.internal.model.RavenDatabaseDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RavenDatabaseResponse(
    @SerialName(value = "Databases")
    val databases: List<RavenDatabaseDetails>,
)
