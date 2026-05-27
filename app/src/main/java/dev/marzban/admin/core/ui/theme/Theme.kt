package dev.marzban.admin.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode { System, Light, Dark }
enum class ThemeSource { Brand, Dynamic }

@Composable
fun MarzbanTheme(
    mode: ThemeMode = ThemeMode.System,
    source: ThemeSource = ThemeSource.Brand,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (mode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme = when (source) {
        ThemeSource.Dynamic -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> BrandDark
            else -> BrandLight
        }
        ThemeSource.Brand -> if (darkTheme) BrandDark else BrandLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MarzbanTypography,
        content = content,
    )
}
