package dev.marzban.admin.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided. Wrap composition in SnackbarHostScaffold.")
}

@Composable
fun SnackbarHostScaffold(content: @Composable () -> Unit) {
    val state = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalSnackbarHostState provides state) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            SnackbarHost(
                hostState = state,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** Convenience helper: launches a snackbar from any coroutine scope. */
fun CoroutineScope.snack(
    host: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
) {
    launch { host.showSnackbar(message, actionLabel, withDismissAction = withDismissAction) }
}
