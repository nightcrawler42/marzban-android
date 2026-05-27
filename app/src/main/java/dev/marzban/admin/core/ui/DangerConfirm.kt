package dev.marzban.admin.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.marzban.admin.core.ui.theme.BrandGradients
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Long-press destructive confirm. Holding for [holdSeconds] fills the bar and
 * invokes [onConfirm]; releasing early aborts.
 */
@Composable
fun HoldToConfirmButton(
    label: String,
    onConfirm: () -> Unit,
    holdSeconds: Float = 2.5f,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var runJob by remember { mutableStateOf<Job?>(null) }

    val color = MaterialTheme.colorScheme.errorContainer
    val onColor = MaterialTheme.colorScheme.onErrorContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(color)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        runJob?.cancel()
                        runJob = scope.launch {
                            progress.snapTo(0f)
                            progress.animateTo(1f, tween(durationMillis = (holdSeconds * 1000).toInt()))
                            onConfirm()
                        }
                        val released = tryAwaitRelease()
                        if (!released || progress.value < 1f) {
                            runJob?.cancel()
                            scope.launch { progress.animateTo(0f, tween(180)) }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // Fill bar
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.value)
                .height(56.dp)
                .background(BrandGradients.danger),
            contentAlignment = Alignment.Center,
        ) {}
        Text(
            text = if (progress.value > 0f) "Keep holding…" else label,
            color = onColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Confirm dialog where the user has to type [expected] into a text field before
 * the destructive button enables. Useful for "delete all" flows.
 */
@Composable
fun TypedConfirmDialog(
    title: String,
    body: String,
    expected: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(body, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Type \"$expected\" to confirm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(); onDismiss() },
                enabled = text.trim() == expected,
            ) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
