package com.racktrack.presentation.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Live-board sizing derived from the available pane (half-screen or full).
 * Reserves room for name, stat chips (run-out / fouls), and action rows so tall
 * phones (e.g. Pixel) do not let a huge score hide the counters.
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
        private const val SHORT_WEIGHT = 0.42f
        private const val TALL_WEIGHT = 0.10f

        private const val NAME_FACTOR = 0.22f
        private const val ACTION_FACTOR = 0.28f
        private const val ICON_FACTOR = 0.26f
        private const val CUE_FACTOR = 0.30f
        private const val VISIT_STAT_FACTOR = 0.14f
        private const val WARN_FACTOR = 0.12f
        private const val CLEAR_HINT_FACTOR = 0.10f
        private const val ACTION_GAP_FACTOR = 0.05f
        private const val PAD_H_FACTOR = 0.08f
        private const val PAD_V_FACTOR = 0.04f
        private const val SCORE_ACTIONS_GAP_FACTOR = 0.05f
        private const val NAME_SCORE_GAP_FACTOR = 0.04f
        private const val STAT_GAP_FACTOR = 0.12f
        private const val CUE_INSET_FACTOR = 0.16f
        private const val FOOTER_FACTOR = 0.28f

        /** Fraction of the score-cluster budget used by the big score digits. */
        private const val SCORE_IN_CLUSTER_FRACTION = 0.62f

        private const val SCORE_MIN_SP = 36f
        private const val SCORE_MAX_SP = 72f
        private const val NAME_MIN_SP = 16f
        private const val NAME_MAX_SP = 32f
        private const val VISIT_MIN_SP = 12f
        private const val VISIT_MAX_SP = 22f
        private const val WARN_MIN_SP = 11f
        private const val WARN_MAX_SP = 18f
        private const val HINT_MIN_SP = 10f
        private const val HINT_MAX_SP = 13f

        private val ACTION_MIN = 34.dp
        private val ACTION_MAX = 52.dp
        private val ICON_MIN = 28.dp
        private val ICON_MAX = 44.dp
        private val CUE_MIN = 26.dp
        private val CUE_MAX = 48.dp

        /**
         * @param paneWidth available width of one player pane (or board column)
         * @param paneHeight available height of one player pane
         * @param actionRowCount 1 = +1/RUN/FOUL only; 2 = extras row (GOLDEN/DRY/PUSH…)
         */
        fun fromPane(
            paneWidth: Dp,
            paneHeight: Dp,
            actionRowCount: Int = 2,
        ): BoardMetrics {
            val short = minOf(paneWidth, paneHeight)
            val tall = maxOf(paneWidth, paneHeight)
            val unit = (short.value * SHORT_WEIGHT + tall.value * TALL_WEIGHT).dp

            fun Dp.clamp(min: Dp, max: Dp): Dp = coerceIn(min, max)
            fun Float.spClamp(min: Float, max: Float): TextUnit =
                coerceIn(min, max).sp

            val name = (unit.value * NAME_FACTOR).spClamp(NAME_MIN_SP, NAME_MAX_SP)
            val actionH = (unit.value * ACTION_FACTOR).dp.clamp(ACTION_MIN, ACTION_MAX)
            val icon = (unit.value * ICON_FACTOR).dp.clamp(ICON_MIN, ICON_MAX)
            val cue = (unit.value * CUE_FACTOR).dp.clamp(CUE_MIN, CUE_MAX)
            val actionGap = (unit.value * ACTION_GAP_FACTOR).dp.clamp(3.dp, 8.dp)
            val padV = (unit.value * PAD_V_FACTOR).dp.clamp(2.dp, 8.dp)
            val nameToScore = (unit.value * NAME_SCORE_GAP_FACTOR).dp.clamp(2.dp, 8.dp)
            val scoreToActions = (unit.value * SCORE_ACTIONS_GAP_FACTOR).dp.clamp(2.dp, 10.dp)
            val rows = actionRowCount.coerceIn(1, 3)

            // Keep name + stat row + action rows on-screen; score fills what remains.
            val reserved =
                padV * 2 +
                    name.value.dp +
                    nameToScore +
                    icon +
                    nameToScore +
                    scoreToActions +
                    actionH * rows +
                    actionGap * (rows - 1).coerceAtLeast(0)
            val clusterBudget = (paneHeight - reserved).coerceAtLeast(SCORE_MIN_SP.dp)
            val score = (clusterBudget.value * SCORE_IN_CLUSTER_FRACTION)
                .spClamp(SCORE_MIN_SP, SCORE_MAX_SP)

            return BoardMetrics(
                scoreSp = score,
                nameSp = name,
                visitStatSp = (unit.value * VISIT_STAT_FACTOR).spClamp(VISIT_MIN_SP, VISIT_MAX_SP),
                warnSp = (unit.value * WARN_FACTOR).spClamp(WARN_MIN_SP, WARN_MAX_SP),
                clearHintSp = (unit.value * CLEAR_HINT_FACTOR).spClamp(HINT_MIN_SP, HINT_MAX_SP),
                actionHeight = actionH,
                actionGap = actionGap,
                panelPaddingH = (unit.value * PAD_H_FACTOR).dp.clamp(6.dp, 16.dp),
                panelPaddingV = padV,
                scoreToActionsGap = scoreToActions,
                nameToScoreGap = nameToScore,
                statIconSize = icon,
                statIconGap = (unit.value * STAT_GAP_FACTOR).dp.clamp(10.dp, 20.dp),
                cueBallSize = cue,
                cueInset = (unit.value * CUE_INSET_FACTOR).dp.clamp(8.dp, 28.dp),
                footerActionHeight = (unit.value * FOOTER_FACTOR).dp.clamp(34.dp, 48.dp),
            )
        }
    }
}
