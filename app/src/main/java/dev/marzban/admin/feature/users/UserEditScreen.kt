package dev.marzban.admin.feature.users

import dev.marzban.admin.core.ui.LocalSnackbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.data.dto.DataLimitResetStrategy
import dev.marzban.admin.data.dto.UserStatusCreate
import dev.marzban.admin.data.dto.UserStatusModify

@Composable
fun UserEditScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: UserEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is UserEditEvent.Saved -> onSaved(ev.username)
                is UserEditEvent.Failure -> snackbar.showSnackbar(ev.message.ifBlank { "Failed" })
            }
        }
    }

    TitleScaffold(
        title = if (state.isNew) "New user" else "Edit user",
        onBack = onBack,
        actions = {
            TextButton(onClick = viewModel::submit, enabled = !state.submitting && !state.loading) {
                Text("Save")
            }
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@TitleScaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isNew) {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::setUsername,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = state.dataLimitGb,
                onValueChange = viewModel::setDataLimitGb,
                label = { Text("Data limit (GB, empty = unlimited)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.expireEpoch?.toString().orEmpty(),
                onValueChange = { v -> viewModel.setExpireEpoch(v.toLongOrNull()) },
                label = { Text("Expire (unix seconds, empty = never)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.onHoldDurationDays,
                onValueChange = viewModel::setOnHoldDurationDays,
                label = { Text("On-hold duration (days)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.autoDeleteInDays,
                onValueChange = viewModel::setAutoDeleteInDays,
                label = { Text("Auto-delete in (days)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
            )

            SectionLabel("Reset strategy")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataLimitResetStrategy.entries.forEach { strategy ->
                    FilterChip(
                        selected = state.resetStrategy == strategy,
                        onClick = { viewModel.setResetStrategy(strategy) },
                        label = { Text(strategy.name.lowercase()) }
                    )
                }
            }

            SectionLabel("Status")
            if (state.isNew) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserStatusCreate.entries.forEach { s ->
                        FilterChip(
                            selected = state.statusCreate == s,
                            onClick = { viewModel.setStatusCreate(s) },
                            label = { Text(s.name.lowercase()) }
                        )
                    }
                }
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserStatusModify.entries.forEach { s ->
                        FilterChip(
                            selected = state.statusModify == s,
                            onClick = { viewModel.setStatusModify(s) },
                            label = { Text(s.name.lowercase()) }
                        )
                    }
                }
            }

            SectionLabel("Inbounds")
            state.availableInbounds.forEach { (protocol, list) ->
                Text(
                    protocol,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    list.forEach { inbound ->
                        val selected = state.inbounds[protocol].orEmpty().contains(inbound.tag)
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleInbound(protocol, inbound.tag) },
                            label = { Text(inbound.tag) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::submit,
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp).padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(if (state.isNew) "Create user" else "Save changes")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
}
