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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite

/** Two primary actions (no cancel) — 14/1 accept open, push-out choice, etc. */
private const val CHOICE_MODAL_MAX_HEIGHT_FRACTION = 0.72f
private const val CHOICE_MODAL_WIDTH_FRACTION = 0.88f
private const val CHOICE_MODAL_SCRIM_ALPHA = 0.55f

@Composable
fun TwoChoiceModal(
    title: String,
    subtitle: String? = null,
    primaryLabel: String,
    primaryBase: Color,
    primaryLight: Color,
    primaryDark: Color,
    secondaryLabel: String,
    secondaryBase: Color,
    secondaryLight: Color,
    secondaryDark: Color,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    val felt = LocalFeltPalette.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = CHOICE_MODAL_SCRIM_ALPHA))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(CHOICE_MODAL_WIDTH_FRACTION)
                .heightIn(max = maxHeight * CHOICE_MODAL_MAX_HEIGHT_FRACTION)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(felt.dark.copy(alpha = 0.98f), felt.vignette),
                    ),
                )
                .border(2.dp, OutlineWarm.copy(alpha = 0.75f), RoundedCornerShape(22.dp))
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OutlineWarm,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TexturedActionButton(
                    label = secondaryLabel,
                    base = secondaryBase,
                    light = secondaryLight,
                    dark = secondaryDark,
                    enabled = true,
                    onClick = onSecondary,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
                TexturedActionButton(
                    label = primaryLabel,
                    base = primaryBase,
                    light = primaryLight,
                    dark = primaryDark,
                    enabled = true,
                    onClick = onPrimary,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
            }
        }
    }
}
