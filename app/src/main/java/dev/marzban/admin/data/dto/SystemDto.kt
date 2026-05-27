package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemStatsDto(
    @SerialName("version") val version: String,
    @SerialName("mem_total") val memTotal: Long,
    @SerialName("mem_used") val memUsed: Long,
    @SerialName("cpu_cores") val cpuCores: Int,
    @SerialName("cpu_usage") val cpuUsage: Double,
    @SerialName("total_user") val totalUser: Int,
    @SerialName("online_users") val onlineUsers: Int,
    @SerialName("users_active") val usersActive: Int,
    @SerialName("users_on_hold") val usersOnHold: Int = 0,
    @SerialName("users_disabled") val usersDisabled: Int = 0,
    @SerialName("users_expired") val usersExpired: Int = 0,
    @SerialName("users_limited") val usersLimited: Int = 0,
    @SerialName("incoming_bandwidth") val incomingBandwidth: Long,
    @SerialName("outgoing_bandwidth") val outgoingBandwidth: Long,
    @SerialName("incoming_bandwidth_speed") val incomingBandwidthSpeed: Long,
    @SerialName("outgoing_bandwidth_speed") val outgoingBandwidthSpeed: Long,
)

@Serializable
data class InboundDto(
    @SerialName("tag") val tag: String,
    @SerialName("protocol") val protocol: String,
    @SerialName("network") val network: String,
    @SerialName("tls") val tls: String,
    @SerialName("port") val port: Int? = null,
)
