package dev.marzban.admin.feature.logs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.data.ws.LogEvent
import dev.marzban.admin.data.ws.LogsClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsUi(
    val title: String,
    val lines: List<String> = emptyList(),
    val paused: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logsClient: LogsClient,
) : ViewModel() {

    /** -1 = core logs; >=0 = node id */
    private val nodeId: Int = savedStateHandle.get<Int>("nodeId") ?: -1

    private val _state = MutableStateFlow(
        LogsUi(title = if (nodeId < 0) "Core logs" else "Node $nodeId logs")
    )
    val state: StateFlow<LogsUi> = _state

    private var job: Job? = null

    init { start() }

    fun start() {
        job?.cancel()
        job = viewModelScope.launch {
            val flow = if (nodeId < 0) logsClient.coreLogs() else logsClient.nodeLogs(nodeId)
            flow.collect { event ->
                if (_state.value.paused) return@collect
                when (event) {
                    is LogEvent.Line -> _state.update {
                        val lines = (it.lines + event.text).takeLast(MAX_LINES)
                        it.copy(lines = lines, error = null)
                    }
                    is LogEvent.Closed -> _state.update { it.copy(error = "Disconnected: ${event.reason ?: "closed"}") }
                    is LogEvent.Error -> _state.update { it.copy(error = event.cause.message ?: "error") }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun togglePause() = _state.update { it.copy(paused = !it.paused) }
    fun clear() = _state.update { it.copy(lines = emptyList()) }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    private companion object { const val MAX_LINES = 1000 }
}
