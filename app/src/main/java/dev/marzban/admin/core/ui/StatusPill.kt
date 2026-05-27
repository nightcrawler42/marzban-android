package dev.marzban.admin.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import dev.marzban.admin.core.ui.theme.StatusPalette
import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.dto.NodeStatus
import dev.marzban.admin.data.dto.UserStatus

data class PillTone(val bg: Color, val fg: Color, val label: String)

@Composable
fun StatusPill(tone: PillTone, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(tone.bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            tone.label,
            color = tone.fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun UserStatus.pill(): PillTone {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return remember(this, dark) {
        when (this) {
            UserStatus.Active -> if (dark) PillTone(StatusPalette.activeBg, StatusPalette.activeFg, "active")
                else PillTone(StatusPalette.activeBgLight, StatusPalette.activeFgLight, "active")
            UserStatus.Disabled -> if (dark) PillTone(StatusPalette.disabledBg, StatusPalette.disabledFg, "disabled")
                else PillTone(StatusPalette.disabledBgLight, StatusPalette.disabledFgLight, "disabled")
            UserStatus.Limited -> if (dark) PillTone(StatusPalette.limitedBg, StatusPalette.limitedFg, "limited")
                else PillTone(StatusPalette.limitedBgLight, StatusPalette.limitedFgLight, "limited")
            UserStatus.Expired -> if (dark) PillTone(StatusPalette.expiredBg, StatusPalette.expiredFg, "expired")
                else PillTone(StatusPalette.expiredBgLight, StatusPalette.expiredFgLight, "expired")
            UserStatus.OnHold -> if (dark) PillTone(StatusPalette.onHoldBg, StatusPalette.onHoldFg, "on hold")
                else PillTone(StatusPalette.onHoldBgLight, StatusPalette.onHoldFgLight, "on hold")
        }
    }
}

@Composable
fun NodeStatus.pill(): PillTone {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return remember(this, dark) {
        when (this) {
            NodeStatus.Connected -> if (dark) PillTone(StatusPalette.activeBg, StatusPalette.activeFg, "connected")
                else PillTone(StatusPalette.activeBgLight, StatusPalette.activeFgLight, "connected")
            NodeStatus.Connecting -> if (dark) PillTone(StatusPalette.warningBg, StatusPalette.warningFg, "connecting")
                else PillTone(StatusPalette.warningBgLight, StatusPalette.warningFgLight, "connecting")
            NodeStatus.Error -> if (dark) PillTone(StatusPalette.expiredBg, StatusPalette.expiredFg, "error")
                else PillTone(StatusPalette.expiredBgLight, StatusPalette.expiredFgLight, "error")
            NodeStatus.Disabled -> if (dark) PillTone(StatusPalette.disabledBg, StatusPalette.disabledFg, "disabled")
                else PillTone(StatusPalette.disabledBgLight, StatusPalette.disabledFgLight, "disabled")
        }
    }
}

@Composable
fun hostPill(host: HostDto): PillTone {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val disabled = host.isDisabled == true
    return when {
        disabled && dark -> PillTone(StatusPalette.disabledBg, StatusPalette.disabledFg, "disabled")
        disabled -> PillTone(StatusPalette.disabledBgLight, StatusPalette.disabledFgLight, "disabled")
        dark -> PillTone(StatusPalette.activeBg, StatusPalette.activeFg, "enabled")
        else -> PillTone(StatusPalette.activeBgLight, StatusPalette.activeFgLight, "enabled")
    }
}

private fun Color.luminance(): Float =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue)
