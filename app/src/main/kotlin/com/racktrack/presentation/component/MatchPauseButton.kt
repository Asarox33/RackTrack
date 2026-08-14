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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite

/** Top-bar pause / resume control (club break — not shot clock or FFB timeout). */
@Composable
fun MatchPauseButton(
    paused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    solo: Boolean = false,
) {
    val timing = if (solo) "training timing" else "match timing"
    val label = if (paused) "Resume $timing" else "Pause $timing"
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
            val fill = if (paused) OutlineWarm else ScoreWhite.copy(alpha = FILL_ALPHA)
            val shadow = ScoreWhite.copy(alpha = SHADOW_ALPHA)
            val w = this.size.width
            val h = this.size.height
            if (paused) {
                // Play triangle
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.22f)
                    lineTo(w * 0.78f, h * 0.50f)
                    lineTo(w * 0.32f, h * 0.78f)
                    close()
                }
                drawPath(path, color = shadow)
                drawPath(
                    path,
                    color = fill,
                )
            } else {
                val barW = w * BAR_WIDTH
                val barH = h * BAR_HEIGHT
                val top = h * (1f - BAR_HEIGHT) / 2f
                val gap = w * BAR_GAP
                val leftX = (w - (2f * barW + gap)) / 2f
                val radius = CornerRadius(barW * 0.35f, barW * 0.35f)
                drawRoundRect(
                    color = shadow,
                    topLeft = Offset(leftX, top + h * SHADOW_Y),
                    size = Size(barW, barH),
                    cornerRadius = radius,
                )
                drawRoundRect(
                    color = shadow,
                    topLeft = Offset(leftX + barW + gap, top + h * SHADOW_Y),
                    size = Size(barW, barH),
                    cornerRadius = radius,
                )
                drawRoundRect(
                    color = fill,
                    topLeft = Offset(leftX, top),
                    size = Size(barW, barH),
                    cornerRadius = radius,
                )
                drawRoundRect(
                    color = fill,
                    topLeft = Offset(leftX + barW + gap, top),
                    size = Size(barW, barH),
                    cornerRadius = radius,
                )
            }
        }
    }
}

private const val DRAW_FRACTION = 0.78f
private const val FILL_ALPHA = 0.95f
private const val SHADOW_ALPHA = 0.20f
private const val SHADOW_Y = 0.04f
private const val BAR_WIDTH = 0.22f
private const val BAR_HEIGHT = 0.58f
private const val BAR_GAP = 0.16f
