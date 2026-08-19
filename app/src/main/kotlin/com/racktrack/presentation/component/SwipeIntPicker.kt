package com.racktrack.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite

private const val SWIPE_STEP_DP = 48f
private const val VALUE_SP_DEFAULT = 64f
private const val CHEVRON_SP = 36f
private const val NEIGHBOR_SPAN = 2
private const val NEAR_SCALE = 0.55f
private const val FAR_SCALE = 0.38f
private const val NEAR_ALPHA = 0.48f
private const val FAR_ALPHA = 0.26f
private const val CENTER_WEIGHT = 1.35f
private const val SIDE_WEIGHT = 1f

/**
 * Integer value picker: swipe left = +1, swipe right = −1.
 * Shows ±2 neighbors smaller/faded so the strip reads as swipeable.
 * Side chevrons remain tappable for accessibility / no-swipe fallback.
 */
@Composable
fun SwipeIntPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier,
    valueLabel: (Int) -> String = { it.toString() },
    valueSp: TextUnit = VALUE_SP_DEFAULT.sp,
    hint: String = "Swipe  ·  or tap a number",
) {
    val performHaptic = rememberClickHaptic()
    val density = LocalDensity.current
    val stepPx = with(density) { SWIPE_STEP_DP.dp.toPx() }
    var dragAccum by remember { mutableFloatStateOf(0f) }
    val local = remember { mutableIntStateOf(value.coerceIn(min, max)) }
    LaunchedEffect(value, min, max) {
        local.intValue = value.coerceIn(min, max)
    }
    val onValueChangeUpdated by rememberUpdatedState(onValueChange)
    val hapticUpdated by rememberUpdatedState(performHaptic)
    val minUpdated by rememberUpdatedState(min)
    val maxUpdated by rememberUpdatedState(max)

    fun commit(next: Int) {
        val coerced = next.coerceIn(minUpdated, maxUpdated)
        if (coerced != local.intValue) {
            local.intValue = coerced
            hapticUpdated()
            onValueChangeUpdated(coerced)
        }
    }

    fun stepBy(delta: Int) = commit(local.intValue + delta)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .semantics {
                    contentDescription =
                        "Value ${valueLabel(local.intValue)}. Swipe left to increase, right to decrease."
                }
                .pointerInput(stepPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = { dragAccum = 0f },
                        onDragCancel = { dragAccum = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccum += dragAmount
                            while (dragAccum <= -stepPx) {
                                dragAccum += stepPx
                                commit(local.intValue + 1)
                            }
                            while (dragAccum >= stepPx) {
                                dragAccum -= stepPx
                                commit(local.intValue - 1)
                            }
                        },
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ChevronTap(
                label = "‹",
                enabled = local.intValue > min,
                onClick = { stepBy(-1) },
            )
            NeighborStrip(
                center = local.intValue,
                min = min,
                max = max,
                valueLabel = valueLabel,
                valueSp = valueSp,
                onSelect = ::commit,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )
            ChevronTap(
                label = "›",
                enabled = local.intValue < max,
                onClick = { stepBy(+1) },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.labelLarge,
            color = OutlineWarm.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NeighborStrip(
    center: Int,
    min: Int,
    max: Int,
    valueLabel: (Int) -> String,
    valueSp: TextUnit,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (offset in -NEIGHBOR_SPAN..NEIGHBOR_SPAN) {
            val n = center + offset
            val inRange = n in min..max
            val (scale, alpha) = when (kotlin.math.abs(offset)) {
                0 -> 1f to 1f
                1 -> NEAR_SCALE to NEAR_ALPHA
                else -> FAR_SCALE to FAR_ALPHA
            }
            Box(
                modifier = Modifier
                    .weight(if (offset == 0) CENTER_WEIGHT else SIDE_WEIGHT)
                    .heightIn(min = 64.dp)
                    .then(
                        if (inRange && offset != 0) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelect(n) },
                            )
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (inRange) {
                    Text(
                        text = valueLabel(n),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = (valueSp.value * scale).sp,
                        ),
                        color = ScoreWhite.copy(alpha = alpha),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChevronTap(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .widthIn(min = 44.dp)
            .heightIn(min = 64.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = CHEVRON_SP.sp),
            color = ScoreWhite.copy(alpha = if (enabled) 0.9f else 0.28f),
        )
    }
}
