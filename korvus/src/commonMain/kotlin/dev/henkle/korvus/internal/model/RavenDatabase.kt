package dev.henkle.korvus.internal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RavenDatabase(
    @SerialName(value = "DatabaseName")
    val name: String,
    @SerialName(value = "Disabled")
    val disabled: Boolean = false,
    @SerialName(value = "Encrypted")
    val encrypted: Boolean = false,
    @SerialName(value = "Settings")
    val settings: Settings = Settings,
    @SerialName(value = "Sharding")
    val sharding: Sharding? = null,
    @SerialName(value = "Topology")
    val topology: Topology = Topology(),
) {
    @Serializable
    object Settings

    @Serializable
    object Sharding


    @Serializable
    data class Topology(
        @SerialName(value = "DynamicNodesDistribution")
        val dynamicNodesDistribution: Boolean = false,
        @SerialName(value = "Members")
        val members: Members? = null,
    ) {
        @Serializable
        object Members
    }
}
