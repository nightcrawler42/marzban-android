package dev.marzban.admin.core.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Brand gradients used for heroes, login background, splash. These always use the
 * fixed brand palette so the contrast against `BrandColors.onGradient` is constant.
 */
object BrandGradients {

    val hero: Brush = Brush.linearGradient(
        colors = listOf(
            BrandColors.gradientStart,
            BrandColors.gradientMid,
            BrandColors.gradientEnd,
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val heroSoft: Brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF263775),
            Color(0xFF1A2557),
        )
    )

    val accentSweep: Brush = Brush.sweepGradient(
        listOf(
            Color(0xFF6F8BFF),
            Color(0xFF21D4FD),
            Color(0xFF8AE36F),
            Color(0xFFF6C453),
            Color(0xFFFF7E7E),
            Color(0xFF6F8BFF),
        )
    )

    val danger: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFB22222), Color(0xFFFF4D6D))
    )
}
