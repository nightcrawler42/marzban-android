package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NodeStatus {
    @SerialName("connected") Connected,
    @SerialName("connecting") Connecting,
    @SerialName("error") Error,
    @SerialName("disabled") Disabled,
}

@Serializable
data class NodeResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String,
    @SerialName("port") val port: Int = 62050,
    @SerialName("api_port") val apiPort: Int = 62051,
    @SerialName("usage_coefficient") val usageCoefficient: Double = 1.0,
    @SerialName("xray_version") val xrayVersion: String? = null,
    @SerialName("status") val status: NodeStatus,
    @SerialName("message") val message: String? = null,
)

@Serializable
data class NodeCreateRequest(
    @SerialName("name") val name: String,
    @SerialName("address") val address: String,
    @SerialName("port") val port: Int = 62050,
    @SerialName("api_port") val apiPort: Int = 62051,
    @SerialName("usage_coefficient") val usageCoefficient: Double = 1.0,
    @SerialName("add_as_new_host") val addAsNewHost: Boolean = true,
)

@Serializable
data class NodeModifyRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("port") val port: Int? = null,
    @SerialName("api_port") val apiPort: Int? = null,
    @SerialName("status") val status: NodeStatus? = null,
    @SerialName("usage_coefficient") val usageCoefficient: Double? = null,
)

@Serializable
data class NodeSettingsResponse(
    @SerialName("min_node_version") val minNodeVersion: String = "v0.2.0",
    @SerialName("certificate") val certificate: String,
)

@Serializable
data class NodeUsageDto(
    @SerialName("node_id") val nodeId: Int? = null,
    @SerialName("node_name") val nodeName: String,
    @SerialName("uplink") val uplink: Long,
    @SerialName("downlink") val downlink: Long,
)

@Serializable
data class NodesUsageResponse(
    @SerialName("usages") val usages: List<NodeUsageDto>,
)
