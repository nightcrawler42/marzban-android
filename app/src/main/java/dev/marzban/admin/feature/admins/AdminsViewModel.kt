package dev.marzban.admin.feature.admins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.AdminDto
import dev.marzban.admin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminsViewModel @Inject constructor(
    private val repository: AdminRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<AdminDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AdminDto>>> = _state

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repository.list(limit = 200)) {
                is ApiResult.Success -> _state.value = UiState.Success(r.value)
                is ApiResult.Failure -> _state.value = UiState.Error(r.error.message ?: "Error")
            }
        }
    }
}
