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
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.ButtonRunOutDark
import com.racktrack.presentation.theme.ButtonRunOutLight
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite

/** Shared stepper modal (race length, etc.) — same visual family as 14/1 visit-end. */
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
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.88f)
                .heightIn(max = maxHeight * 0.72f)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(felt.dark.copy(alpha = 0.98f), felt.vignette),
                    ),
                )
                .border(2.dp, OutlineWarm.copy(alpha = 0.75f), RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TexturedActionButton(
                    label = "−",
                    base = felt.mid,
                    light = felt.light,
                    dark = felt.dark,
                    enabled = value > min,
                    onClick = { value = (value - 1).coerceAtLeast(min) },
                    modifier = Modifier.widthIn(min = 64.dp),
                    height = 48.dp,
                )
                Text(
                    text = valueLabel(value),
                    style = MaterialTheme.typography.displayLarge,
                    color = ScoreWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 96.dp),
                )
                TexturedActionButton(
                    label = "+",
                    base = felt.mid,
                    light = felt.light,
                    dark = felt.dark,
                    enabled = value < max,
                    onClick = { value = (value + 1).coerceAtMost(max) },
                    modifier = Modifier.widthIn(min = 64.dp),
                    height = 48.dp,
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TexturedOutlineAction(
                    label = "CANCEL",
                    enabled = true,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TexturedActionButton(
                    label = "CONFIRM",
                    base = ButtonRunOut,
                    light = ButtonRunOutLight,
                    dark = ButtonRunOutDark,
                    enabled = true,
                    onClick = { onConfirm(value) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
            }
        }
    }
}
