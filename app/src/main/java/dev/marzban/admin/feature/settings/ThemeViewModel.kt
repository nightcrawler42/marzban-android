package dev.marzban.admin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.storage.ServerConfigStore
import dev.marzban.admin.core.ui.theme.ThemeMode
import dev.marzban.admin.core.ui.theme.ThemeSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeState(
    val mode: ThemeMode = ThemeMode.System,
    val source: ThemeSource = ThemeSource.Brand,
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val configStore: ServerConfigStore,
) : ViewModel() {

    val state: StateFlow<ThemeState> = combine(
        configStore.themeMode,
        configStore.themeSource,
    ) { modeStr, sourceStr ->
        ThemeState(
            mode = runCatching { ThemeMode.valueOf(modeStr) }.getOrDefault(ThemeMode.System),
            source = runCatching { ThemeSource.valueOf(sourceStr) }.getOrDefault(ThemeSource.Brand),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeState())

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch { configStore.setThemeMode(mode.name) }
    }

    fun setSource(source: ThemeSource) {
        viewModelScope.launch { configStore.setThemeSource(source.name) }
    }
}
