package dev.marzban.admin.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.CoreStatsDto
import dev.marzban.admin.data.dto.SystemStatsDto
import dev.marzban.admin.data.repository.CoreRepository
import dev.marzban.admin.data.repository.SystemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val system: SystemStatsDto?,
    val core: CoreStatsDto?,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    private val coreRepository: CoreRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardData>> = _state

    init { refresh() }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val (sys, core) = awaitAll(
                async { systemRepository.stats() },
                async { coreRepository.stats() },
            )
            @Suppress("UNCHECKED_CAST")
            val sysRes = sys as ApiResult<SystemStatsDto>
            @Suppress("UNCHECKED_CAST")
            val coreRes = core as ApiResult<CoreStatsDto>
            val firstError = (sysRes as? ApiResult.Failure)?.error ?: (coreRes as? ApiResult.Failure)?.error
            if (firstError != null) {
                _state.value = UiState.Error(firstError.message ?: "Error")
                return@launch
            }
            _state.value = UiState.Success(
                DashboardData(
                    system = (sysRes as ApiResult.Success).value,
                    core = (coreRes as ApiResult.Success).value,
                )
            )
        }
    }
}
