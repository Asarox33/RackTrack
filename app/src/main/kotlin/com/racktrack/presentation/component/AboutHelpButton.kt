@file:Suppress("MagicNumber")

package com.racktrack.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racktrack.i18n.StringKey
import com.racktrack.i18n.Strings
import com.racktrack.presentation.theme.LocalAppTheme

/**
 * Top-chrome info control (filled disc + “i”), same weight as [SettingsGearButton].
 * Setup only — not shown on the live board.
 */
@Composable
fun AboutHelpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val theme = LocalAppTheme.current
    val fill = theme.textPrimary.copy(alpha = if (theme.isDark) FILL_ALPHA_DARK else FILL_ALPHA_LIGHT)
    val shadow = theme.textPrimary.copy(alpha = SHADOW_ALPHA)
    val label = Strings.get(StringKey.SECTION_ABOUT)
    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = label }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * DRAW_FRACTION)) {
            val dim = this.size.minDimension
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val path = infoGlyphPath(center = c, radius = dim * DISC_R)

            translate(left = 0f, top = dim * SHADOW_Y) {
                drawPath(path = path, color = shadow)
            }
            drawPath(path = path, color = fill)
        }
    }
}

/** Filled circle with punched “i” (dot + stem), EvenOdd like the settings cog hub. */
private fun infoGlyphPath(center: Offset, radius: Float): Path {
    val path = Path().apply { fillType = PathFillType.EvenOdd }
    path.addOval(Rect(center = center, radius = radius))

    val dotR = radius * DOT_R_FRAC
    val dotCy = center.y - radius * DOT_Y_FRAC
    path.addOval(Rect(center = Offset(center.x, dotCy), radius = dotR))

    val stemHalfW = radius * STEM_W_FRAC
    val stemTop = center.y - radius * STEM_TOP_FRAC
    val stemBottom = center.y + radius * STEM_BOT_FRAC
    path.addRect(
        Rect(
            left = center.x - stemHalfW,
            top = stemTop,
            right = center.x + stemHalfW,
            bottom = stemBottom,
        ),
    )
    return path
}

private const val DRAW_FRACTION = 0.80f
private const val DISC_R = 0.48f
private const val DOT_R_FRAC = 0.14f
private const val DOT_Y_FRAC = 0.38f
private const val STEM_W_FRAC = 0.11f
private const val STEM_TOP_FRAC = 0.08f
private const val STEM_BOT_FRAC = 0.42f
private const val FILL_ALPHA_DARK = 0.92f
private const val FILL_ALPHA_LIGHT = 0.88f
private const val SHADOW_ALPHA = 0.18f
private const val SHADOW_Y = 0.035f
