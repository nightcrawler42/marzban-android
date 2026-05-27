package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
enum class UserStatus {
    @SerialName("active") Active,
    @SerialName("disabled") Disabled,
    @SerialName("limited") Limited,
    @SerialName("expired") Expired,
    @SerialName("on_hold") OnHold;

    fun wire(): String = when (this) {
        Active -> "active"
        Disabled -> "disabled"
        Limited -> "limited"
        Expired -> "expired"
        OnHold -> "on_hold"
    }
}

@Serializable
enum class UserStatusCreate {
    @SerialName("active") Active,
    @SerialName("on_hold") OnHold,
}

@Serializable
enum class UserStatusModify {
    @SerialName("active") Active,
    @SerialName("disabled") Disabled,
    @SerialName("on_hold") OnHold,
}

@Serializable
enum class DataLimitResetStrategy {
    @SerialName("no_reset") NoReset,
    @SerialName("day") Day,
    @SerialName("week") Week,
    @SerialName("month") Month,
    @SerialName("year") Year,
}

@Serializable
data class NextPlanDto(
    @SerialName("data_limit") val dataLimit: Long? = null,
    @SerialName("expire") val expire: Long? = null,
    @SerialName("add_remaining_traffic") val addRemainingTraffic: Boolean = false,
    @SerialName("fire_on_either") val fireOnEither: Boolean = true,
)

/** Proxy settings are polymorphic per protocol; keep as JSON. */
typealias ProxySettingsMap = Map<String, JsonObject>

/** inbounds is a dict of proxy type → list of inbound tags. */
typealias InboundsMap = Map<String, List<String>>

@Serializable
data class UserResponse(
    @SerialName("username") val username: String,
    @SerialName("status") val status: UserStatus,
    @SerialName("used_traffic") val usedTraffic: Long,
    @SerialName("lifetime_used_traffic") val lifetimeUsedTraffic: Long = 0,
    @SerialName("created_at") val createdAt: String,
    @SerialName("proxies") val proxies: ProxySettingsMap = emptyMap(),
    @SerialName("expire") val expire: Long? = null,
    @SerialName("data_limit") val dataLimit: Long? = null,
    @SerialName("data_limit_reset_strategy") val dataLimitResetStrategy: DataLimitResetStrategy = DataLimitResetStrategy.NoReset,
    @SerialName("inbounds") val inbounds: InboundsMap = emptyMap(),
    @SerialName("note") val note: String? = null,
    @SerialName("sub_updated_at") val subUpdatedAt: String? = null,
    @SerialName("sub_last_user_agent") val subLastUserAgent: String? = null,
    @SerialName("online_at") val onlineAt: String? = null,
    @SerialName("on_hold_expire_duration") val onHoldExpireDuration: Long? = null,
    @SerialName("on_hold_timeout") val onHoldTimeout: String? = null,
    @SerialName("auto_delete_in_days") val autoDeleteInDays: Int? = null,
    @SerialName("next_plan") val nextPlan: NextPlanDto? = null,
    @SerialName("links") val links: List<String> = emptyList(),
    @SerialName("subscription_url") val subscriptionUrl: String = "",
    @SerialName("excluded_inbounds") val excludedInbounds: InboundsMap = emptyMap(),
    @SerialName("admin") val admin: AdminDto? = null,
)

@Serializable
data class UserCreateRequest(
    @SerialName("username") val username: String,
    @SerialName("proxies") val proxies: ProxySettingsMap = emptyMap(),
    @SerialName("inbounds") val inbounds: InboundsMap = emptyMap(),
    @SerialName("expire") val expire: Long? = null,
    @SerialName("data_limit") val dataLimit: Long? = null,
    @SerialName("data_limit_reset_strategy") val dataLimitResetStrategy: DataLimitResetStrategy = DataLimitResetStrategy.NoReset,
    @SerialName("note") val note: String? = null,
    @SerialName("on_hold_expire_duration") val onHoldExpireDuration: Long? = null,
    @SerialName("on_hold_timeout") val onHoldTimeout: String? = null,
    @SerialName("auto_delete_in_days") val autoDeleteInDays: Int? = null,
    @SerialName("next_plan") val nextPlan: NextPlanDto? = null,
    @SerialName("status") val status: UserStatusCreate? = null,
)

@Serializable
data class UserModifyRequest(
    @SerialName("proxies") val proxies: ProxySettingsMap? = null,
    @SerialName("inbounds") val inbounds: InboundsMap? = null,
    @SerialName("expire") val expire: Long? = null,
    @SerialName("data_limit") val dataLimit: Long? = null,
    @SerialName("data_limit_reset_strategy") val dataLimitResetStrategy: DataLimitResetStrategy? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("on_hold_expire_duration") val onHoldExpireDuration: Long? = null,
    @SerialName("on_hold_timeout") val onHoldTimeout: String? = null,
    @SerialName("auto_delete_in_days") val autoDeleteInDays: Int? = null,
    @SerialName("next_plan") val nextPlan: NextPlanDto? = null,
    @SerialName("status") val status: UserStatusModify? = null,
)

@Serializable
data class UsersResponse(
    @SerialName("users") val users: List<UserResponse>,
    @SerialName("total") val total: Int,
)

@Serializable
data class UserUsageDto(
    @SerialName("node_id") val nodeId: Int? = null,
    @SerialName("node_name") val nodeName: String,
    @SerialName("used_traffic") val usedTraffic: Long,
)

@Serializable
data class UserUsagesResponse(
    @SerialName("username") val username: String,
    @SerialName("usages") val usages: List<UserUsageDto>,
)

@Serializable
data class UsersUsagesResponse(
    @SerialName("usages") val usages: List<UserUsageDto>,
)

@Suppress("unused")
private val ignoredHelper: JsonElement? = null
