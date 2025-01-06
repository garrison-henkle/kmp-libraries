package dev.henkle.korvus.internal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RavenDatabaseDetails(
    @SerialName(value = "Name")
    val name: String,
    @SerialName(value = "TotalSize")
    val size: RavenSize,
    @SerialName(value = "UpTime")
    val uptime: String? = null,
    @SerialName(value = "DocumentsCount")
    val documentCount: Long,
    @SerialName(value = "IndexesCount")
    val indexCount: Long,
    @SerialName(value = "IndexingErrors")
    val indexingErrors: Long,
    @SerialName(value = "Alerts")
    val alertCount: Long,
    @SerialName(value = "IsEncrypted")
    val encrypted: Boolean,
    @SerialName(value = "Disabled")
    val disabled: Boolean,
    @SerialName(value = "NodesTopology")
    val topology: Topology,
) {
    @Serializable
    data class Topology(
        @SerialName(value = "Members")
        val nodes: List<Node>,
        @SerialName(value = "Status")
        val statuses: Map<String, NodeStatus>,
    )

    @Serializable
    data class Node(
        @SerialName(value = "NodeTag")
        val tag: String,
        @SerialName(value = "NodeUrl")
        val url: String,
        @SerialName(value = "ResponsibleNode")
        val responsibleNode: String? = null,
    )

    @Serializable
    data class NodeStatus(
        @SerialName(value = "LastStatus")
        val lastStatus: String,
    )
}
