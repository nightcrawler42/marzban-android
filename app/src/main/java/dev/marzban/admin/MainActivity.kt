package dev.marzban.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.marzban.admin.core.ui.LocalSnackbarHostState
import dev.marzban.admin.core.ui.SnackbarHostScaffold
import dev.marzban.admin.core.ui.theme.MarzbanTheme
import dev.marzban.admin.feature.settings.ThemeViewModel
import dev.marzban.admin.nav.MarzbanNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeVm: ThemeViewModel = hiltViewModel()
            val themeState by themeVm.state.collectAsStateWithLifecycle()
            MarzbanTheme(mode = themeState.mode, source = themeState.source) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SnackbarHostScaffold {
                        MarzbanNavHost()
                    }
                }
            }
        }
    }
}
