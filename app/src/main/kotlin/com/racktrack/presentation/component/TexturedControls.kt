package com.racktrack.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite
import kotlin.math.floor

@Composable
fun TexturedActionButton(
    label: String,
    base: Color,
    light: Color,
    dark: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    corner: Dp = (height.value * CORNER_FRAC_OF_HEIGHT).dp,
) {
    val performHaptic = rememberClickHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(corner)
    val alpha = if (enabled) 1f else 0.38f
    val top = if (pressed && enabled) dark else light
    val bottom = if (pressed && enabled) base.copy(alpha = 0.85f) else dark
    val borderW = (height.value * BORDER_FRAC_OF_HEIGHT).coerceIn(1f, 2f).dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(top.copy(alpha = alpha), bottom.copy(alpha = alpha))),
            )
            .drawWithContent {
                drawContent()
                drawFeltGrain(alpha = GRAIN_OVERLAY_ALPHA * alpha)
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = HIGHLIGHT_ALPHA * alpha),
                        HIGHLIGHT_FADE_STOP to Color.Transparent,
                    ),
                )
            }
            .border(
                width = borderW,
                color = OutlineWarm.copy(alpha = if (enabled) 0.55f else 0.22f),
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = {
                    performHaptic()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = if (height.value < 40f) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.labelLarge
            },
            color = ScoreWhite.copy(alpha = if (enabled) 1f else 0.45f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun TexturedChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedLight: Color,
    selectedDark: Color,
    idleLight: Color,
    idleDark: Color,
    height: Dp = 52.dp,
) {
    val performHaptic = rememberClickHaptic()
    val shape = RoundedCornerShape((height.value * CORNER_FRAC_OF_HEIGHT).dp)
    val top = if (selected) selectedLight else idleLight
    val bottom = if (selected) selectedDark else idleDark

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .drawWithContent {
                drawContent()
                drawFeltGrain(alpha = CHIP_GRAIN_ALPHA)
            }
            .border(
                width = (height.value * BORDER_FRAC_OF_HEIGHT).coerceIn(1f, 2f).dp,
                color = OutlineWarm.copy(alpha = if (selected) 0.85f else 0.40f),
                shape = shape,
            )
            .clickable {
                performHaptic()
                onClick()
            }
            .padding(horizontal = (height.value * CHIP_PAD_H_FRAC).dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

/**
 * Full-width setting affordance: label + value + chevron (e.g. race distance).
 * Reads as a tappable control, not a lone selected chip.
 */
@Composable
fun TexturedSettingButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    light: Color,
    dark: Color,
) {
    val performHaptic = rememberClickHaptic()
    val shape = RoundedCornerShape((height.value * CORNER_FRAC_OF_HEIGHT).dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(light, dark)))
            .drawWithContent {
                drawContent()
                drawFeltGrain(alpha = CHIP_GRAIN_ALPHA)
            }
            .border(
                width = (height.value * BORDER_FRAC_OF_HEIGHT).coerceIn(1f, 2f).dp,
                color = OutlineWarm.copy(alpha = 0.75f),
                shape = shape,
            )
            .clickable {
                performHaptic()
                onClick()
            }
            .padding(horizontal = (height.value * CHIP_PAD_H_FRAC).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = ScoreWhite.copy(alpha = 0.85f),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = ScoreWhite,
                maxLines = 1,
            )
            Text(
                text = "  ›",
                style = MaterialTheme.typography.titleLarge,
                color = ScoreWhite.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
fun TexturedOutlineAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
) {
    val performHaptic = rememberClickHaptic()
    val shape = RoundedCornerShape((height.value * OUTLINE_CORNER_FRAC).dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.10f else 0.04f),
                        Color.Black.copy(alpha = if (enabled) 0.18f else 0.08f),
                    ),
                ),
            )
            .border(
                (height.value * OUTLINE_BORDER_FRAC).coerceIn(1f, 2f).dp,
                OutlineWarm.copy(alpha = if (enabled) 0.55f else 0.22f),
                shape,
            )
            .clickable(enabled = enabled) {
                performHaptic()
                onClick()
            }
            .padding(horizontal = (height.value * OUTLINE_PAD_H_FRAC).dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ScoreWhite.copy(alpha = if (enabled) 1f else 0.4f),
            maxLines = 1,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFeltGrain(alpha: Float) {
    val step = GRAIN_STEP_PX
    val cols = floor(size.width / step).toInt()
    val rows = floor(size.height / step).toInt()
    for (row in 0..rows) {
        for (col in 0..cols) {
            val hash = ((row * GRAIN_HASH_X) xor (col * GRAIN_HASH_Y)) and GRAIN_HASH_MASK
            if (hash % GRAIN_DENSITY != 0) continue
            val shade = if (hash % 2 == 0) Color.Black else Color.White
            drawCircle(
                color = shade.copy(
                    alpha = alpha * (GRAIN_MIN_SHADE + (hash % GRAIN_SHADE_RANGE) / GRAIN_SHADE_DIVISOR),
                ),
                radius = GRAIN_RADIUS_PX,
                center = Offset(col * step + 1f, row * step + 1f),
            )
        }
    }
}

private const val GRAIN_OVERLAY_ALPHA = 0.07f
private const val CHIP_GRAIN_ALPHA = 0.06f
private const val HIGHLIGHT_ALPHA = 0.10f
private const val HIGHLIGHT_FADE_STOP = 0.35f
private const val GRAIN_STEP_PX = 3.5f
private const val GRAIN_HASH_X = 73_856_093
private const val GRAIN_HASH_Y = 19_349_663
private const val GRAIN_HASH_MASK = 0xFF
private const val GRAIN_DENSITY = 5
private const val GRAIN_MIN_SHADE = 0.35f
private const val GRAIN_SHADE_RANGE = 40
private const val GRAIN_SHADE_DIVISOR = 100f
private const val GRAIN_RADIUS_PX = 0.7f
private const val CORNER_FRAC_OF_HEIGHT = 0.28f
private const val BORDER_FRAC_OF_HEIGHT = 0.03f
private const val CHIP_PAD_H_FRAC = 0.28f
private const val OUTLINE_CORNER_FRAC = 0.32f
private const val OUTLINE_BORDER_FRAC = 0.035f
private const val OUTLINE_PAD_H_FRAC = 0.45f
