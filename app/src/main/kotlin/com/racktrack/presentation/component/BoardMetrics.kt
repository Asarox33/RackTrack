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
        private const val SHORT_WEIGHT = 0.55f
        private const val TALL_WEIGHT = 0.12f
        private const val SCORE_FACTOR = 0.72f
        private const val NAME_FACTOR = 0.28f
        private const val ACTION_FACTOR = 0.38f
        private const val ICON_FACTOR = 0.34f
        private const val CUE_FACTOR = 0.40f
        private const val VISIT_STAT_FACTOR = 0.18f
        private const val WARN_FACTOR = 0.16f
        private const val CLEAR_HINT_FACTOR = 0.12f
        private const val ACTION_GAP_FACTOR = 0.07f
        private const val PAD_H_FACTOR = 0.10f
        private const val PAD_V_FACTOR = 0.05f
        private const val SCORE_ACTIONS_GAP_FACTOR = 0.08f
        private const val NAME_SCORE_GAP_FACTOR = 0.06f
        private const val STAT_GAP_FACTOR = 0.18f
        private const val CUE_INSET_FACTOR = 0.22f
        private const val FOOTER_FACTOR = 0.36f

        private const val SCORE_MIN_SP = 44f
        private const val SCORE_MAX_SP = 110f
        private const val NAME_MIN_SP = 18f
        private const val NAME_MAX_SP = 40f
        private const val VISIT_MIN_SP = 14f
        private const val VISIT_MAX_SP = 28f
        private const val WARN_MIN_SP = 12f
        private const val WARN_MAX_SP = 22f
        private const val HINT_MIN_SP = 10f
        private const val HINT_MAX_SP = 14f

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
            val unit = (short.value * SHORT_WEIGHT + tall.value * TALL_WEIGHT).dp

            fun Dp.clamp(min: Dp, max: Dp): Dp = coerceIn(min, max)
            fun Float.spClamp(min: Float, max: Float): TextUnit =
                coerceIn(min, max).sp

            val score = (unit.value * SCORE_FACTOR).spClamp(SCORE_MIN_SP, SCORE_MAX_SP)
            val name = (unit.value * NAME_FACTOR).spClamp(NAME_MIN_SP, NAME_MAX_SP)
            val actionH = (unit.value * ACTION_FACTOR).dp.clamp(36.dp, 64.dp)
            val icon = (unit.value * ICON_FACTOR).dp.clamp(28.dp, 56.dp)
            val cue = (unit.value * CUE_FACTOR).dp.clamp(28.dp, 64.dp)

            return BoardMetrics(
                scoreSp = score,
                nameSp = name,
                visitStatSp = (unit.value * VISIT_STAT_FACTOR).spClamp(VISIT_MIN_SP, VISIT_MAX_SP),
                warnSp = (unit.value * WARN_FACTOR).spClamp(WARN_MIN_SP, WARN_MAX_SP),
                clearHintSp = (unit.value * CLEAR_HINT_FACTOR).spClamp(HINT_MIN_SP, HINT_MAX_SP),
                actionHeight = actionH,
                actionGap = (unit.value * ACTION_GAP_FACTOR).dp.clamp(4.dp, 12.dp),
                panelPaddingH = (unit.value * PAD_H_FACTOR).dp.clamp(6.dp, 20.dp),
                panelPaddingV = (unit.value * PAD_V_FACTOR).dp.clamp(2.dp, 12.dp),
                scoreToActionsGap = (unit.value * SCORE_ACTIONS_GAP_FACTOR).dp.clamp(4.dp, 16.dp),
                nameToScoreGap = (unit.value * NAME_SCORE_GAP_FACTOR).dp.clamp(2.dp, 12.dp),
                statIconSize = icon,
                statIconGap = (unit.value * STAT_GAP_FACTOR).dp.clamp(12.dp, 28.dp),
                cueBallSize = cue,
                cueInset = (unit.value * CUE_INSET_FACTOR).dp.clamp(12.dp, 36.dp),
                footerActionHeight = (unit.value * FOOTER_FACTOR).dp.clamp(36.dp, 56.dp),
            )
        }
    }
}
