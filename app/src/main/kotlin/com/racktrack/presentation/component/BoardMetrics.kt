package com.racktrack.presentation.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Live-board sizing derived from the available pane (half-screen or full).
 * Used by race + 14/1 boards so phones, phablets, and tablets share one scale model.
 */
@Immutable
data class BoardMetrics(
    val scoreSp: TextUnit,
    val nameSp: TextUnit,
    val visitStatSp: TextUnit,
    val warnSp: TextUnit,
    val clearHintSp: TextUnit,
    val actionHeight: Dp,
    val actionGap: Dp,
    val panelPaddingH: Dp,
    val panelPaddingV: Dp,
    val scoreToActionsGap: Dp,
    val nameToScoreGap: Dp,
    val statIconSize: Dp,
    val statIconGap: Dp,
    val cueBallSize: Dp,
    val cueInset: Dp,
    val footerActionHeight: Dp,
) {
    companion object {
        /**
         * @param paneWidth available width of one player pane (or board column)
         * @param paneHeight available height of one player pane
         */
        fun fromPane(
            paneWidth: Dp,
            paneHeight: Dp,
        ): BoardMetrics {
            val short = minOf(paneWidth, paneHeight)
            val tall = maxOf(paneWidth, paneHeight)
            // Prefer height for vertical rhythm; width caps oversized tablets in landscape halves.
            val unit = (short.value * 0.55f + tall.value * 0.12f).dp

            fun Dp.clamp(min: Dp, max: Dp): Dp = coerceIn(min, max)
            fun Float.spClamp(min: Float, max: Float): TextUnit =
                coerceIn(min, max).sp

            val score = (unit.value * 0.72f).spClamp(44f, 110f)
            val name = (unit.value * 0.28f).spClamp(18f, 40f)
            val actionH = (unit.value * 0.38f).dp.clamp(36.dp, 64.dp)
            val icon = (unit.value * 0.34f).dp.clamp(28.dp, 56.dp)
            val cue = (unit.value * 0.40f).dp.clamp(28.dp, 64.dp)

            return BoardMetrics(
                scoreSp = score,
                nameSp = name,
                visitStatSp = (unit.value * 0.18f).spClamp(14f, 28f),
                warnSp = (unit.value * 0.16f).spClamp(12f, 22f),
                clearHintSp = (unit.value * 0.12f).spClamp(10f, 14f),
                actionHeight = actionH,
                actionGap = (unit.value * 0.07f).dp.clamp(4.dp, 12.dp),
                panelPaddingH = (unit.value * 0.10f).dp.clamp(6.dp, 20.dp),
                panelPaddingV = (unit.value * 0.05f).dp.clamp(2.dp, 12.dp),
                scoreToActionsGap = (unit.value * 0.08f).dp.clamp(4.dp, 16.dp),
                nameToScoreGap = (unit.value * 0.06f).dp.clamp(2.dp, 12.dp),
                statIconSize = icon,
                statIconGap = (unit.value * 0.18f).dp.clamp(12.dp, 28.dp),
                cueBallSize = cue,
                cueInset = (unit.value * 0.22f).dp.clamp(12.dp, 36.dp),
                footerActionHeight = (unit.value * 0.36f).dp.clamp(36.dp, 56.dp),
            )
        }
    }
}
