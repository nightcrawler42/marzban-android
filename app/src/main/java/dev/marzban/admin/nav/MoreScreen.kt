package dev.marzban.admin.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.marzban.admin.core.ui.TitleScaffold

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    onOpenAdmins: () -> Unit,
    onOpenExpiredUsers: () -> Unit,
    onOpenHosts: () -> Unit,
    onOpenInbounds: () -> Unit,
    onOpenCore: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    TitleScaffold(title = "More") { padding ->
        LazyColumn(
            modifier = modifier.padding(padding).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { MoreEntry("Admins", "Manage panel admins", Icons.Default.AdminPanelSettings, onOpenAdmins) }
            item { MoreEntry("Expired users", "Browse and bulk-clean expired accounts", Icons.Default.AssignmentLate, onOpenExpiredUsers) }
            item { MoreEntry("Hosts", "Edit proxy hosts per inbound", Icons.Default.Dns, onOpenHosts) }
            item { MoreEntry("Inbounds", "Inspect configured inbounds", Icons.AutoMirrored.Filled.List, onOpenInbounds) }
            item { MoreEntry("Core", "Status, config editor, live logs", Icons.Default.Memory, onOpenCore) }
            item { MoreEntry("Settings", "Server, security, account", Icons.Default.Settings, onOpenSettings) }
        }
    }
}

@Composable
private fun MoreEntry(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
