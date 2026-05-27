package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class HostSecurity {
    @SerialName("inbound_default") InboundDefault,
    @SerialName("none") None,
    @SerialName("tls") Tls,
}

@Serializable
enum class HostAlpn {
    @SerialName("") Empty,
    @SerialName("h3") H3,
    @SerialName("h2") H2,
    @SerialName("http/1.1") Http11,
    @SerialName("h3,h2,http/1.1") H3H2Http11,
    @SerialName("h3,h2") H3H2,
    @SerialName("h2,http/1.1") H2Http11,
}

@Serializable
enum class HostFingerprint {
    @SerialName("") Empty,
    @SerialName("chrome") Chrome,
    @SerialName("firefox") Firefox,
    @SerialName("safari") Safari,
    @SerialName("ios") IOs,
    @SerialName("android") Android,
    @SerialName("edge") Edge,
    @SerialName("360") F360,
    @SerialName("qq") Qq,
    @SerialName("random") Random,
    @SerialName("randomized") Randomized,
}

@Serializable
data class HostDto(
    @SerialName("remark") val remark: String,
    @SerialName("address") val address: String,
    @SerialName("port") val port: Int? = null,
    @SerialName("sni") val sni: String? = null,
    @SerialName("host") val host: String? = null,
    @SerialName("path") val path: String? = null,
    @SerialName("security") val security: HostSecurity = HostSecurity.InboundDefault,
    @SerialName("alpn") val alpn: HostAlpn = HostAlpn.Empty,
    @SerialName("fingerprint") val fingerprint: HostFingerprint = HostFingerprint.Empty,
    @SerialName("allowinsecure") val allowInsecure: Boolean? = null,
    @SerialName("is_disabled") val isDisabled: Boolean? = null,
    @SerialName("mux_enable") val muxEnable: Boolean? = null,
    @SerialName("fragment_setting") val fragmentSetting: String? = null,
    @SerialName("noise_setting") val noiseSetting: String? = null,
    @SerialName("random_user_agent") val randomUserAgent: Boolean? = null,
    @SerialName("use_sni_as_host") val useSniAsHost: Boolean? = null,
)
