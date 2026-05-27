package dev.marzban.admin.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Hand-tuned brand palette. Every `on*` pair is at least AA contrast (4.5:1) over
 * its surface. These colors are independent of the device's dynamic palette so the
 * hero gradients and accent surfaces always render legibly.
 */

// Indigo primary
private val IndigoLight = Color(0xFF3A4ADC)
private val IndigoLightContainer = Color(0xFFDFE2FF)
private val IndigoDark = Color(0xFF9FAEFF)
private val IndigoDarkContainer = Color(0xFF1F2D8F)

// Cyan accent (tertiary)
private val CyanLight = Color(0xFF0E7D8B)
private val CyanLightContainer = Color(0xFFB6F0FF)
private val CyanDark = Color(0xFF6BE6F0)
private val CyanDarkContainer = Color(0xFF114F58)

// Magenta secondary (sparingly used)
private val MagentaLight = Color(0xFF8A4ACB)
private val MagentaDark = Color(0xFFD0B0FF)

// Neutrals
private val SurfaceDarkBase = Color(0xFF0E1019)
private val SurfaceDarkElevated = Color(0xFF1A1E2D)
private val SurfaceDarkHighest = Color(0xFF22273A)
private val OutlineDark = Color(0xFF353A52)

private val SurfaceLightBase = Color(0xFFF6F7FB)
private val SurfaceLightElevated = Color(0xFFFFFFFF)
private val SurfaceLightHighest = Color(0xFFE9EBF3)
private val OutlineLight = Color(0xFFC9CCDA)

// Error
private val ErrorLight = Color(0xFFBA1A1A)
private val ErrorLightContainer = Color(0xFFFFDAD6)
private val ErrorDark = Color(0xFFFFB4AB)
private val ErrorDarkContainer = Color(0xFF93000A)

val BrandLight: ColorScheme = lightColorScheme(
    primary = IndigoLight,
    onPrimary = Color.White,
    primaryContainer = IndigoLightContainer,
    onPrimaryContainer = Color(0xFF000F5A),
    secondary = MagentaLight,
    onSecondary = Color.White,
    tertiary = CyanLight,
    onTertiary = Color.White,
    tertiaryContainer = CyanLightContainer,
    onTertiaryContainer = Color(0xFF002028),
    background = SurfaceLightBase,
    onBackground = Color(0xFF12131A),
    surface = SurfaceLightElevated,
    onSurface = Color(0xFF12131A),
    surfaceVariant = SurfaceLightHighest,
    onSurfaceVariant = Color(0xFF44475C),
    outline = OutlineLight,
    outlineVariant = Color(0xFFDDDFEA),
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorLightContainer,
    onErrorContainer = Color(0xFF410002),
)

val BrandDark: ColorScheme = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color(0xFF00115B),
    primaryContainer = IndigoDarkContainer,
    onPrimaryContainer = Color(0xFFE0E4FF),
    secondary = MagentaDark,
    onSecondary = Color(0xFF361756),
    tertiary = CyanDark,
    onTertiary = Color(0xFF003640),
    tertiaryContainer = CyanDarkContainer,
    onTertiaryContainer = Color(0xFFB6F0FF),
    background = SurfaceDarkBase,
    onBackground = Color(0xFFE7E8F3),
    surface = SurfaceDarkElevated,
    onSurface = Color(0xFFE7E8F3),
    surfaceVariant = SurfaceDarkHighest,
    onSurfaceVariant = Color(0xFFB7BACE),
    outline = OutlineDark,
    outlineVariant = Color(0xFF2A2F46),
    error = ErrorDark,
    onError = Color(0xFF690005),
    errorContainer = ErrorDarkContainer,
    onErrorContainer = Color(0xFFFFDAD6),
)

object StatusPalette {
    val activeBg = Color(0xFF1B5E20)
    val activeFg = Color(0xFFB6F1B9)
    val disabledBg = Color(0xFF424955)
    val disabledFg = Color(0xFFD7DAE3)
    val limitedBg = Color(0xFF8A4500)
    val limitedFg = Color(0xFFFFD3A3)
    val expiredBg = Color(0xFF8E1F1F)
    val expiredFg = Color(0xFFFFCDC8)
    val onHoldBg = Color(0xFF52247E)
    val onHoldFg = Color(0xFFE2C6FF)
    val infoBg = Color(0xFF1C3B7A)
    val infoFg = Color(0xFFC0D2FF)
    val warningBg = Color(0xFF7A5300)
    val warningFg = Color(0xFFFFE4A3)

    // Light variants for light theme — softer pastels with darker text
    val activeBgLight = Color(0xFFD4F4D6)
    val activeFgLight = Color(0xFF0F4314)
    val disabledBgLight = Color(0xFFDDE0E9)
    val disabledFgLight = Color(0xFF2E3340)
    val limitedBgLight = Color(0xFFFFE0BD)
    val limitedFgLight = Color(0xFF5A2D00)
    val expiredBgLight = Color(0xFFFFD8D5)
    val expiredFgLight = Color(0xFF5A0E0E)
    val onHoldBgLight = Color(0xFFEEDCFF)
    val onHoldFgLight = Color(0xFF3D1764)
    val infoBgLight = Color(0xFFD7E1FF)
    val infoFgLight = Color(0xFF132A5A)
    val warningBgLight = Color(0xFFFFE6B5)
    val warningFgLight = Color(0xFF553700)
}

object BrandColors {
    // Used for hero gradients — DO NOT swap with theme colors.
    val gradientStart = Color(0xFF3A4ADC)
    val gradientMid = Color(0xFF5B6CFF)
    val gradientEnd = Color(0xFF21D4FD)
    val gradientAccent = Color(0xFFFF6FD8)
    val onGradient = Color.White
    val onGradientMuted = Color(0xE6FFFFFF)
    val onGradientFaint = Color(0x99FFFFFF)
}
