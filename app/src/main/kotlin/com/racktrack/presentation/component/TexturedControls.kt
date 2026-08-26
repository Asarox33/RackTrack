@file:Suppress("MagicNumber")

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racktrack.presentation.theme.ActionTone
import com.racktrack.presentation.theme.LocalAppTheme
import kotlin.math.floor

@Composable
fun TexturedActionButton(
    label: String,
    tone: ActionTone,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    corner: Dp = (height.value * CORNER_FRAC_OF_HEIGHT).dp,
    useFeltGrain: Boolean = true,
) {
    TexturedActionButton(
        label = label,
        base = tone.base,
        light = tone.light,
        dark = tone.dark,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        height = height,
        corner = corner,
        useFeltGrain = useFeltGrain,
        // Same hue as the fill, one step darker — not chrome cyan.
        rimColor = tone.dark,
    )
}

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
    /** Optional cloth grain — off by default for sleek theme buttons. */
    useFeltGrain: Boolean = false,
    /** Border color; defaults to [dark] (tonal edge). */
    rimColor: Color? = null,
) {
    val theme = LocalAppTheme.current
    val performHaptic = rememberClickHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(corner)
    val alpha = if (enabled) 1f else DISABLED_FILL_ALPHA
    val top = if (pressed && enabled) base else light
    val bottom = if (pressed && enabled) dark else base
    val borderW = (height.value * BORDER_FRAC_OF_HEIGHT).coerceIn(1.1f, 1.9f).dp
    val labelColor = theme.contentOn(base).copy(alpha = if (enabled) 1f else 0.42f)
    val edge = (rimColor ?: dark).copy(
        alpha = when {
            !enabled -> RIM_DISABLED_ALPHA
            theme.isDark -> RIM_TONAL_DARK_ALPHA
            else -> RIM_TONAL_LIGHT_ALPHA
        },
    )

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
                if (useFeltGrain) {
                    drawFeltGrain(alpha = GRAIN_OVERLAY_ALPHA * alpha)
                }
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = HIGHLIGHT_ALPHA * alpha),
                        HIGHLIGHT_FADE_STOP to Color.Transparent,
                    ),
                )
            }
            .border(
                width = borderW,
                color = edge,
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
            color = labelColor,
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
    useFeltGrain: Boolean = true,
) {
    val theme = LocalAppTheme.current
    val performHaptic = rememberClickHaptic()
    val shape = RoundedCornerShape((height.value * CORNER_FRAC_OF_HEIGHT).dp)
    val top = if (selected) selectedLight else idleLight
    val bottom = if (selected) selectedDark else idleDark
    val fill = if (selected) selectedDark else idleDark
    val labelColor = theme.contentOn(fill)
    val rimColor = if (selected) {
        theme.rim.copy(alpha = RIM_SELECTED_ALPHA)
    } else {
        theme.rim.copy(alpha = RIM_IDLE_ALPHA)
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .drawWithContent {
                drawContent()
                if (useFeltGrain) {
                    drawFeltGrain(alpha = CHIP_GRAIN_ALPHA * 0.45f)
                }
                if (selected) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to theme.rim.copy(alpha = 0.22f),
                            0.2f to Color.Transparent,
                        ),
                    )
                }
            }
            .border(
                width = (height.value * BORDER_FRAC_OF_HEIGHT).coerceIn(1.25f, 2.25f).dp,
                color = rimColor,
                shape = shape,
            )
            .clickable {
                performHaptic()
                onClick()
            }
            .padding(horizontal = (height.value * CHIP_PAD_H_FRAC * 0.65f).dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
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
    val theme = LocalAppTheme.current
    val performHaptic = rememberClickHaptic()
    val shape = RoundedCornerShape((height.value * CORNER_FRAC_OF_HEIGHT).dp)
    val labelColor = theme.contentOn(dark)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(light, dark)))
            .drawWithContent {
                drawContent()
                drawFeltGrain(alpha = CHIP_GRAIN_ALPHA * 0.4f)
            }
            .border(
                width = (height.value * BORDER_FRAC_OF_HEIGHT).coerceIn(1.25f, 2.25f).dp,
                color = theme.rim.copy(alpha = RIM_ENABLED_ALPHA),
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
            color = labelColor.copy(alpha = 0.85f),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = labelColor,
                maxLines = 1,
            )
            Text(
                text = "  ›",
                style = MaterialTheme.typography.titleLarge,
                color = labelColor.copy(alpha = 0.7f),
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
    val theme = LocalAppTheme.current
    val performHaptic = rememberClickHaptic()
    val shape = RoundedCornerShape((height.value * OUTLINE_CORNER_FRAC).dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        theme.surfaceElevated.copy(alpha = if (enabled) 0.85f else 0.4f),
                        theme.surfaceDeep.copy(alpha = if (enabled) 0.95f else 0.45f),
                    ),
                ),
            )
            .border(
                (height.value * OUTLINE_BORDER_FRAC).coerceIn(1.1f, 1.9f).dp,
                theme.outline.copy(alpha = if (enabled) 0.70f else 0.30f),
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
            color = theme.textPrimary.copy(alpha = if (enabled) 1f else 0.4f),
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

private const val GRAIN_OVERLAY_ALPHA = 0.055f
private const val CHIP_GRAIN_ALPHA = 0.04f
private const val HIGHLIGHT_ALPHA = 0.12f
private const val HIGHLIGHT_FADE_STOP = 0.42f
private const val RIM_ENABLED_ALPHA = 0.72f
private const val RIM_LIGHT_THEME_ALPHA = 0.65f
private const val RIM_TONAL_DARK_ALPHA = 0.88f
private const val RIM_TONAL_LIGHT_ALPHA = 0.92f
private const val RIM_DISABLED_ALPHA = 0.28f
private const val RIM_SELECTED_ALPHA = 0.85f
private const val RIM_IDLE_ALPHA = 0.40f
private const val DISABLED_FILL_ALPHA = 0.32f
private const val GRAIN_STEP_PX = 3.5f
private const val GRAIN_HASH_X = 73_856_093
private const val GRAIN_HASH_Y = 19_349_663
private const val GRAIN_HASH_MASK = 0xFF
private const val GRAIN_DENSITY = 5
private const val GRAIN_MIN_SHADE = 0.35f
private const val GRAIN_SHADE_RANGE = 40
private const val GRAIN_SHADE_DIVISOR = 100f
private const val GRAIN_RADIUS_PX = 0.7f
private const val CORNER_FRAC_OF_HEIGHT = 0.20f
private const val BORDER_FRAC_OF_HEIGHT = 0.032f
private const val CHIP_PAD_H_FRAC = 0.28f
private const val OUTLINE_CORNER_FRAC = 0.26f
private const val OUTLINE_BORDER_FRAC = 0.040f
private const val OUTLINE_PAD_H_FRAC = 0.45f
