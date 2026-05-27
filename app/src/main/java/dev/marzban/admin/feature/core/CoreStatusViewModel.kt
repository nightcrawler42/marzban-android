package dev.marzban.admin.feature.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.CoreStatsDto
import dev.marzban.admin.data.repository.CoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoreStatusViewModel @Inject constructor(
    private val repository: CoreRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<CoreStatsDto>>(UiState.Loading)
    val state: StateFlow<UiState<CoreStatsDto>> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repository.stats()) {
                is ApiResult.Success -> _state.value = UiState.Success(r.value)
                is ApiResult.Failure -> _state.value = UiState.Error(r.error.message ?: "Error")
            }
        }
    }

    fun restart() {
        viewModelScope.launch {
            when (val r = repository.restart()) {
                is ApiResult.Success -> { _events.emit("Core restart requested"); load() }
                is ApiResult.Failure -> _events.emit(r.error.message ?: "Restart failed")
            }
        }
    }
}
