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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racktrack.presentation.theme.ScoreWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SettingsGearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * DRAW_FRACTION)) {
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val path = cogPath(
                center = c,
                tipRadius = this.size.minDimension * TIP_R,
                rootRadius = this.size.minDimension * ROOT_R,
                holeRadius = this.size.minDimension * HOLE_R,
                teeth = TEETH,
            )

            translate(left = 0f, top = this.size.minDimension * SHADOW_Y) {
                drawPath(path = path, color = ScoreWhite.copy(alpha = SHADOW_ALPHA))
            }
            drawPath(path = path, color = ScoreWhite.copy(alpha = FILL_ALPHA))
        }
    }
}

/** Material-style settings cog: even trapezoid teeth + punched hub. */
private fun cogPath(
    center: Offset,
    tipRadius: Float,
    rootRadius: Float,
    holeRadius: Float,
    teeth: Int,
): Path {
    val path = Path().apply { fillType = PathFillType.EvenOdd }
    val step = (2.0 * PI / teeth).toFloat()
    val tipHalf = step * TIP_SPAN / 2f
    val rootHalf = step * ROOT_SPAN / 2f

    for (i in 0 until teeth) {
        val mid = i * step - PI.toFloat() / 2f
        val rootIn = polar(center, rootRadius, mid - rootHalf)
        val tipIn = polar(center, tipRadius, mid - tipHalf)
        val tipOut = polar(center, tipRadius, mid + tipHalf)
        val rootOut = polar(center, rootRadius, mid + rootHalf)

        if (i == 0) path.moveTo(rootIn.x, rootIn.y) else path.lineTo(rootIn.x, rootIn.y)
        path.lineTo(tipIn.x, tipIn.y)
        path.lineTo(tipOut.x, tipOut.y)
        path.lineTo(rootOut.x, rootOut.y)
    }
    path.close()
    path.addOval(Rect(center = center, radius = holeRadius))
    return path
}

private fun polar(center: Offset, radius: Float, angle: Float): Offset =
    Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)

private const val DRAW_FRACTION = 0.80f
private const val TIP_R = 0.48f
private const val ROOT_R = 0.30f
private const val HOLE_R = 0.145f
private const val TEETH = 8
/** Fraction of tooth sector occupied at the tip / at the root. */
private const val TIP_SPAN = 0.36f
private const val ROOT_SPAN = 0.72f
private const val FILL_ALPHA = 0.95f
private const val SHADOW_ALPHA = 0.20f
private const val SHADOW_Y = 0.035f
