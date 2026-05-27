package dev.marzban.admin.feature.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.TitleScaffold

@Composable
fun NodeEditScreen(
    onBack: () -> Unit,
    onSaved: (Int) -> Unit,
    viewModel: NodeEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { ev -> if (ev is NodeEditEvent.Saved) onSaved(ev.id) }
    }
    TitleScaffold(
        title = if (state.isNew) "New node" else "Edit node",
        onBack = onBack,
        actions = {
            TextButton(onClick = viewModel::submit, enabled = !state.submitting && !state.loading) { Text("Save") }
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
            OutlinedTextField(value = state.name, onValueChange = viewModel::setName, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.address, onValueChange = viewModel::setAddress, label = { Text("Address (host or IP)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.port, onValueChange = viewModel::setPort, label = { Text("Port") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.apiPort, onValueChange = viewModel::setApiPort, label = { Text("API port") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.usageCoefficient, onValueChange = viewModel::setUsageCoefficient,
                label = { Text("Usage coefficient") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isNew) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = state.addAsNewHost, onCheckedChange = viewModel::setAddAsNewHost)
                    Spacer(Modifier.padding(start = 12.dp))
                    Text("Add as a new host for every inbound")
                }
            }
            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            Button(onClick = viewModel::submit, enabled = !state.submitting, modifier = Modifier.fillMaxWidth()) {
                if (state.submitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(18.dp).padding(end = 8.dp))
                }
                Text(if (state.isNew) "Create" else "Save changes")
            }
        }
    }
}
