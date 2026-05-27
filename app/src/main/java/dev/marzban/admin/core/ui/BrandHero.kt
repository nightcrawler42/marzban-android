package dev.marzban.admin.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import dev.marzban.admin.core.ui.theme.BrandColors
import dev.marzban.admin.core.ui.theme.BrandGradients

/**
 * Hero header card with the brand gradient. Content is always rendered with
 * [BrandColors.onGradient]; the gradient never uses theme colors so contrast
 * is constant regardless of the dynamic palette.
 */
@Composable
fun BrandHero(
    modifier: Modifier = Modifier,
    brush: Brush = BrandGradients.hero,
    refreshing: Boolean = false,
    rightSlot: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .padding(20.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (rightSlot != null || refreshing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(visible = refreshing, enter = fadeIn(), exit = fadeOut()) {
                        Row {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = BrandColors.onGradient,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    Spacer(Modifier.width(0.dp))
                    Spacer(Modifier.weight(1f))
                    rightSlot?.invoke()
                }
                Spacer(Modifier.height(8.dp))
            }
            content()
        }
    }
}

@Composable
fun HeroPill(text: String, leading: @Composable (() -> Unit)? = null) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.let {
                it()
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                color = BrandColors.onGradient,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
