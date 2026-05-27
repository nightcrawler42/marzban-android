package dev.marzban.admin.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.BuildConfig
import dev.marzban.admin.core.ui.LocalSnackbarHostState
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.theme.BrandColors
import dev.marzban.admin.core.ui.theme.BrandGradients
import dev.marzban.admin.core.ui.theme.ThemeMode
import dev.marzban.admin.core.ui.theme.ThemeSource
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val theme by themeViewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var serverUrlInput by remember(ui.serverUrl) { mutableStateOf(ui.serverUrl) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is SettingsEvent.LoggedOut -> onLoggedOut()
                is SettingsEvent.Toast -> scope.launch { snackbar.showSnackbar(ev.message) }
            }
        }
    }

    TitleScaffold(title = "Settings", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SectionLabel("Appearance") }
            item {
                ThemeCard(
                    mode = theme.mode,
                    source = theme.source,
                    onModeChange = themeViewModel::setMode,
                    onSourceChange = themeViewModel::setSource,
                )
            }
            item { SectionLabel("Server") }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("Server URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { viewModel.setServerUrl(serverUrlInput) }) {
                                Text("Save server URL")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = ui.trustAllCerts, onCheckedChange = viewModel::setTrustAllCerts)
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Trust all TLS certificates", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Disables verification. Reduces security; only for self-signed panels you control.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
            item { SectionLabel("Account") }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    ui.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Signed in as ${ui.username.ifBlank { "—" }}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(ui.serverUrl, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth()) {
                            Text("Sign out")
                        }
                    }
                }
            }
            item { SectionLabel("About") }
            item {
                Text(
                    "Marzban Admin v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ThemeCard(
    mode: ThemeMode,
    source: ThemeSource,
    onModeChange: (ThemeMode) -> Unit,
    onSourceChange: (ThemeSource) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Live preview swatch — uses the brand gradient so the user can see
            // exactly what the dashboard hero will look like.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(BrandGradients.hero, RoundedCornerShape(16.dp))
                    .padding(12.dp),
            ) {
                Text(
                    "Brand preview",
                    color = BrandColors.onGradient,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text("Theme mode", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { i, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { onModeChange(m) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = ThemeMode.entries.size),
                    ) { Text(m.name) }
                }
            }

            Text("Palette", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeSource.entries.forEachIndexed { i, s ->
                    SegmentedButton(
                        selected = source == s,
                        onClick = { onSourceChange(s) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = ThemeSource.entries.size),
                    ) {
                        Text(if (s == ThemeSource.Brand) "Brand" else "Dynamic")
                    }
                }
            }
            Text(
                "Dynamic uses your wallpaper colors (Android 12+). Brand always uses the app's indigo/cyan palette.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
