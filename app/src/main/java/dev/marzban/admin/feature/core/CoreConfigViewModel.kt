package dev.marzban.admin.feature.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.data.repository.CoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

data class CoreConfigUi(
    val text: String = "",
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null,
    val pretty: Boolean = true,
)

@HiltViewModel
class CoreConfigViewModel @Inject constructor(
    private val repository: CoreRepository,
    private val json: Json,
) : ViewModel() {
    private val pretty = Json(json) { prettyPrint = true; prettyPrintIndent = "  " }

    private val _state = MutableStateFlow(CoreConfigUi())
    val state: StateFlow<CoreConfigUi> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = repository.getConfig()) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, text = pretty.encodeToString(JsonObject.serializer(), r.value))
                }
                is ApiResult.Failure -> _state.update { it.copy(loading = false, error = r.error.message) }
            }
        }
    }

    fun setText(value: String) = _state.update { it.copy(text = value, error = null) }

    fun save() {
        val parsed: JsonObject = try {
            json.parseToJsonElement(_state.value.text) as? JsonObject
                ?: throw IllegalArgumentException("Top-level JSON must be an object")
        } catch (e: Exception) {
            _state.update { it.copy(error = "Invalid JSON: ${e.message}") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val r = repository.updateConfig(parsed)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(submitting = false, text = pretty.encodeToString(JsonObject.serializer(), r.value))
                    }
                    _events.emit("Config saved & core restarted")
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(submitting = false, error = r.error.message)
                }
            }
        }
    }
}
