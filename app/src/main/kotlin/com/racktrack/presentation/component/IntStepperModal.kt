package com.racktrack.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.ButtonRunOutDark
import com.racktrack.presentation.theme.ButtonRunOutLight
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite

/** Shared integer picker modal (race length, etc.) — swipe or tap arrows. */
private const val MODAL_MAX_HEIGHT_FRACTION = 0.72f
private const val MODAL_WIDTH_FRACTION = 0.88f
private const val MODAL_WIDTH_CEIL_FRAC = 0.92f
private const val MODAL_SCRIM_ALPHA = 0.55f
private const val VALUE_SP_FRAC = 0.11f
private const val VALUE_SP_FLOOR = 40f
private const val VALUE_SP_CEIL = 72f
private const val ACTION_H_FRAC = 0.055f
private const val ACTION_H_FLOOR = 40f
private const val ACTION_H_CEIL = 52f
private const val CORNER_FRAC = 0.028f
private const val PAD_H_FRAC = 0.05f
private const val PAD_V_FRAC = 0.028f

@Composable
fun IntStepperModal(
    title: String,
    valueLabel: (Int) -> String,
    initial: Int,
    min: Int,
    max: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val felt = LocalFeltPalette.current
    var value by remember(initial) { mutableIntStateOf(initial.coerceIn(min, max)) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = MODAL_SCRIM_ALPHA))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val shortSide = minOf(maxWidth, maxHeight)
        val panelMaxWidth = maxWidth * MODAL_WIDTH_CEIL_FRAC
        val corner = (shortSide.value * CORNER_FRAC).coerceIn(14f, 28f).dp
        val padH = (maxWidth.value * PAD_H_FRAC).coerceIn(16f, 28f).dp
        val padV = (maxHeight.value * PAD_V_FRAC).coerceIn(14f, 24f).dp
        val valueSp = (shortSide.value * VALUE_SP_FRAC)
            .coerceIn(VALUE_SP_FLOOR, VALUE_SP_CEIL)
            .sp
        val actionH = (shortSide.value * ACTION_H_FRAC)
            .coerceIn(ACTION_H_FLOOR, ACTION_H_CEIL)
            .dp

        Column(
            modifier = Modifier
                .widthIn(max = panelMaxWidth)
                .fillMaxWidth(MODAL_WIDTH_FRACTION)
                .heightIn(max = maxHeight * MODAL_MAX_HEIGHT_FRACTION)
                .clip(RoundedCornerShape(corner))
                .background(
                    Brush.verticalGradient(
                        listOf(felt.dark.copy(alpha = 0.98f), felt.vignette),
                    ),
                )
                .border(2.dp, OutlineWarm.copy(alpha = 0.75f), RoundedCornerShape(corner))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = padH, vertical = padV),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(padV))
            SwipeIntPicker(
                value = value,
                onValueChange = { value = it },
                min = min,
                max = max,
                valueLabel = valueLabel,
                valueSp = valueSp,
            )
            Spacer(modifier = Modifier.height(padV))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TexturedOutlineAction(
                    label = "CANCEL",
                    enabled = true,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    height = actionH,
                )
                TexturedActionButton(
                    label = "CONFIRM",
                    base = ButtonRunOut,
                    light = ButtonRunOutLight,
                    dark = ButtonRunOutDark,
                    enabled = true,
                    onClick = { onConfirm(value) },
                    modifier = Modifier.weight(1f),
                    height = actionH,
                )
            }
        }
    }
}
