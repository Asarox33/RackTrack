package com.racktrack.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.presentation.theme.OutlineWarm

/**
 * Bottom fade + chevron when [scrollState] can still scroll down.
 * Place inside a [Box] that wraps the vertically scrolling content.
 */
@Composable
fun BoxScope.ScrollMoreHint(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    fadeColor: Color,
) {
    val canScrollDown by remember {
        derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue
        }
    }
    AnimatedVisibility(
        visible = canScrollDown,
        modifier = modifier.align(Alignment.BottomCenter),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HINT_HEIGHT)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to fadeColor.copy(alpha = 0f),
                            FADE_MID_STOP to fadeColor.copy(alpha = FADE_MID_ALPHA),
                            1f to fadeColor.copy(alpha = FADE_END_ALPHA),
                        ),
                    ),
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = "▼",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = CHEVRON_SP.sp),
                color = OutlineWarm.copy(alpha = 0.95f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}

private val HINT_HEIGHT = 36.dp
private const val FADE_MID_STOP = 0.35f
private const val FADE_MID_ALPHA = 0.55f
private const val FADE_END_ALPHA = 0.92f
private const val CHEVRON_SP = 16
