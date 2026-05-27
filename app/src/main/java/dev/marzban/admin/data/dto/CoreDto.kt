package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoreStatsDto(
    @SerialName("version") val version: String,
    @SerialName("started") val started: Boolean,
    @SerialName("logs_websocket") val logsWebsocket: String,
)
