package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminDto(
    @SerialName("username") val username: String,
    @SerialName("is_sudo") val isSudo: Boolean,
    @SerialName("telegram_id") val telegramId: Long? = null,
    @SerialName("discord_webhook") val discordWebhook: String? = null,
    @SerialName("users_usage") val usersUsage: Long? = null,
)

@Serializable
data class AdminCreateRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("is_sudo") val isSudo: Boolean = false,
    @SerialName("telegram_id") val telegramId: Long? = null,
    @SerialName("discord_webhook") val discordWebhook: String? = null,
)

@Serializable
data class AdminModifyRequest(
    @SerialName("password") val password: String? = null,
    @SerialName("is_sudo") val isSudo: Boolean,
    @SerialName("telegram_id") val telegramId: Long? = null,
    @SerialName("discord_webhook") val discordWebhook: String? = null,
)
