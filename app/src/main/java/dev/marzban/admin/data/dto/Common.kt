package dev.marzban.admin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetailResponse(
    @SerialName("detail") val detail: String? = null
)

enum class ProxyType(val wire: String) {
    @SerialName("vmess") VMess("vmess"),
    @SerialName("vless") VLess("vless"),
    @SerialName("trojan") Trojan("trojan"),
    @SerialName("shadowsocks") Shadowsocks("shadowsocks");

    companion object {
        fun fromWire(value: String): ProxyType? = entries.firstOrNull { it.wire == value }
    }
}
