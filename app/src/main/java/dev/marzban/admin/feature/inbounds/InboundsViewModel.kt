package dev.marzban.admin.feature.inbounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.InboundDto
import dev.marzban.admin.data.repository.SystemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboundsViewModel @Inject constructor(
    private val repository: SystemRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Map<String, List<InboundDto>>>>(UiState.Loading)
    val state: StateFlow<UiState<Map<String, List<InboundDto>>>> = _state

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repository.inbounds()) {
                is ApiResult.Success -> _state.value = UiState.Success(r.value)
                is ApiResult.Failure -> _state.value = UiState.Error(r.error.message ?: "Error")
            }
        }
    }
}
