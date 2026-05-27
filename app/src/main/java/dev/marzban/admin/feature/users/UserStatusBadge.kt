package dev.marzban.admin.feature.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.marzban.admin.core.ui.theme.StatusActive
import dev.marzban.admin.core.ui.theme.StatusDisabled
import dev.marzban.admin.core.ui.theme.StatusExpired
import dev.marzban.admin.core.ui.theme.StatusLimited
import dev.marzban.admin.core.ui.theme.StatusOnHold
import dev.marzban.admin.data.dto.UserStatus

@Composable
fun UserStatusBadge(status: UserStatus) {
    val color = colorFor(status)
    Text(
        text = status.label(),
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

fun UserStatus.label(): String = when (this) {
    UserStatus.Active -> "Active"
    UserStatus.Disabled -> "Disabled"
    UserStatus.Limited -> "Limited"
    UserStatus.Expired -> "Expired"
    UserStatus.OnHold -> "On hold"
}

private fun colorFor(status: UserStatus): Color = when (status) {
    UserStatus.Active -> StatusActive
    UserStatus.Disabled -> StatusDisabled
    UserStatus.Limited -> StatusLimited
    UserStatus.Expired -> StatusExpired
    UserStatus.OnHold -> StatusOnHold
}
