package com.racktrack.presentation.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Live-board metrics: every size is a **fraction of the available pane / screen**,
 * clamped with a touch **floor** and a tablet **ceiling**.
 *
 * No device-specific breakpoints (S25 / tablet / Pixel).
 */
@Immutable
data class BoardMetrics(
    val scoreSp: TextUnit,
    val nameSp: TextUnit,
    val visitStatSp: TextUnit,
    val warnSp: TextUnit,
    val clearHintSp: TextUnit,
    val statCountSp: TextUnit,
    val actionHeight: Dp,
    val actionGap: Dp,
    val actionCorner: Dp,
    val panelPaddingH: Dp,
    val panelPaddingV: Dp,
    val scoreToActionsGap: Dp,
    val nameToScoreGap: Dp,
    val statIconSize: Dp,
    val statIconGap: Dp,
    val cueBallSize: Dp,
    val cueInset: Dp,
    val footerActionHeight: Dp,
    val screenPadH: Dp,
    val screenPadV: Dp,
    val headerToBoardGap: Dp,
    val boardToFooterGap: Dp,
    val footerGap: Dp,
    val medianThickness: Dp,
    val medianInset: Dp,
    val headerSideReserve: Dp,
    val topChromePad: Dp,
    val topChromeGap: Dp,
) {
    companion object {
        // --- Pane vertical fractions (portrait half-board) ---
        private const val PAD_V_FRAC = 0.018f
        private const val PAD_H_FRAC = 0.045f
        private const val NAME_FRAC = 0.072f
        private const val NAME_SCORE_GAP_FRAC = 0.012f
        private const val SCORE_ACTIONS_GAP_FRAC = 0.016f
        private const val ACTION_GAP_FRAC = 0.012f
        /** Share of pane height for all action rows together. */
        private const val ACTIONS_BLOCK_FRAC = 0.38f
        private const val STAT_ICON_FRAC_OF_CLUSTER = 0.30f
        private const val SCORE_FRAC_OF_CLUSTER = 0.58f
        private const val CUE_FRAC_OF_CLUSTER = 0.36f
        private const val CUE_INSET_FRAC = 0.04f
        private const val VISIT_STAT_FRAC = 0.045f
        private const val WARN_FRAC = 0.038f
        private const val HINT_FRAC = 0.030f
        private const val STAT_COUNT_FRAC = 0.042f
        private const val STAT_GAP_FRAC = 0.035f
        private const val ACTION_CORNER_FRAC = 0.28f

        // --- Full-screen chrome (of min(screenW, screenH)) ---
        private const val SCREEN_PAD_H_FRAC = 0.028f
        private const val SCREEN_PAD_V_FRAC = 0.012f
        private const val HEADER_GAP_FRAC = 0.012f
        private const val FOOTER_GAP_FRAC = 0.014f
        private const val FOOTER_BTN_GAP_FRAC = 0.035f
        private const val FOOTER_BTN_H_FRAC = 0.055f
        private const val MEDIAN_THICK_FRAC = 0.006f
        private const val MEDIAN_INSET_FRAC = 0.018f
        private const val HEADER_SIDE_FRAC = 0.20f
        private const val TOP_CHROME_PAD_FRAC = 0.012f
        private const val TOP_CHROME_GAP_FRAC = 0.008f

        /** Soft touch floor — only kicks in on very small panes. */
        private const val TOUCH_FLOOR_FRAC = 0.055f
        private const val TOUCH_FLOOR_MIN_DP = 28f
        private const val TOUCH_FLOOR_MAX_DP = 48f

        /** Ceilings so tablets / tall panes do not grow chrome to absurd sizes. */
        private const val ACTION_HEIGHT_CEIL_DP = 56f
        private const val FOOTER_HEIGHT_CEIL_DP = 48f
        private const val SCORE_SP_CEIL = 92f
        private const val NAME_SP_CEIL = 34f
        private const val STAT_ICON_CEIL_DP = 52f
        private const val CUE_CEIL_DP = 56f
        private const val VISIT_STAT_SP_CEIL = 22f
        private const val WARN_SP_CEIL = 18f
        private const val HINT_SP_CEIL = 14f
        private const val STAT_COUNT_SP_CEIL = 20f
        private const val SCREEN_PAD_H_CEIL_DP = 24f
        private const val SCREEN_PAD_V_CEIL_DP = 14f
        private const val HEADER_SIDE_CEIL_DP = 120f

        /**
         * @param paneWidth / [paneHeight] one player column (or full 14/1 solo column)
         * @param screenWidth / [screenHeight] full board window (for chrome)
         * @param actionRowCount 1..3 action rows under the score
         */
        fun fromPane(
            paneWidth: Dp,
            paneHeight: Dp,
            actionRowCount: Int = 2,
            screenWidth: Dp = paneWidth * 2,
            screenHeight: Dp = paneHeight * 2,
        ): BoardMetrics {
            val h = paneHeight.value.coerceAtLeast(1f)
            val w = paneWidth.value.coerceAtLeast(1f)
            val screenShort = minOf(screenWidth.value, screenHeight.value).coerceAtLeast(1f)
            val rows = actionRowCount.coerceIn(1, 3)

            val touchFloor = (h * TOUCH_FLOOR_FRAC)
                .coerceIn(TOUCH_FLOOR_MIN_DP, TOUCH_FLOOR_MAX_DP)

            val padV = (h * PAD_V_FRAC).dp
            val padH = (w * PAD_H_FRAC).dp
            val nameSp = (h * NAME_FRAC).coerceAtMost(NAME_SP_CEIL).sp
            val nameToScore = (h * NAME_SCORE_GAP_FRAC).dp
            val scoreToActions = (h * SCORE_ACTIONS_GAP_FRAC).dp
            val actionGap = (h * ACTION_GAP_FRAC).dp

            val actionsBlock = h * ACTIONS_BLOCK_FRAC
            val actionH = (
                (actionsBlock - actionGap.value * (rows - 1)) / rows
                ).coerceIn(touchFloor, ACTION_HEIGHT_CEIL_DP).dp

            val reserved =
                padV.value * 2 +
                    nameSp.value +
                    nameToScore.value +
                    scoreToActions.value +
                    actionH.value * rows +
                    actionGap.value * (rows - 1)
            val cluster = (h - reserved).coerceAtLeast(h * 0.22f)

            val scoreSp = (cluster * SCORE_FRAC_OF_CLUSTER).coerceAtMost(SCORE_SP_CEIL).sp
            val icon = (cluster * STAT_ICON_FRAC_OF_CLUSTER)
                .coerceIn(touchFloor * 0.85f, STAT_ICON_CEIL_DP)
                .dp
            val cue = (cluster * CUE_FRAC_OF_CLUSTER)
                .coerceIn(touchFloor * 0.8f, CUE_CEIL_DP)
                .dp

            return BoardMetrics(
                scoreSp = scoreSp,
                nameSp = nameSp,
                visitStatSp = (h * VISIT_STAT_FRAC).coerceAtMost(VISIT_STAT_SP_CEIL).sp,
                warnSp = (h * WARN_FRAC).coerceAtMost(WARN_SP_CEIL).sp,
                clearHintSp = (h * HINT_FRAC).coerceAtMost(HINT_SP_CEIL).sp,
                statCountSp = (h * STAT_COUNT_FRAC).coerceAtMost(STAT_COUNT_SP_CEIL).sp,
                actionHeight = actionH,
                actionGap = actionGap,
                actionCorner = (actionH.value * ACTION_CORNER_FRAC).dp,
                panelPaddingH = padH,
                panelPaddingV = padV,
                scoreToActionsGap = scoreToActions,
                nameToScoreGap = nameToScore,
                statIconSize = icon,
                statIconGap = (w * STAT_GAP_FRAC).dp,
                cueBallSize = cue,
                cueInset = (w * CUE_INSET_FRAC).dp,
                footerActionHeight = (screenShort * FOOTER_BTN_H_FRAC)
                    .coerceIn(touchFloor, FOOTER_HEIGHT_CEIL_DP)
                    .dp,
                screenPadH = (screenShort * SCREEN_PAD_H_FRAC)
                    .coerceAtMost(SCREEN_PAD_H_CEIL_DP)
                    .dp,
                screenPadV = (screenShort * SCREEN_PAD_V_FRAC)
                    .coerceAtMost(SCREEN_PAD_V_CEIL_DP)
                    .dp,
                headerToBoardGap = (screenShort * HEADER_GAP_FRAC).dp,
                boardToFooterGap = (screenShort * FOOTER_GAP_FRAC).dp,
                footerGap = (screenShort * FOOTER_BTN_GAP_FRAC).dp,
                medianThickness = (screenShort * MEDIAN_THICK_FRAC).coerceAtLeast(2f).dp,
                medianInset = (screenShort * MEDIAN_INSET_FRAC).dp,
                headerSideReserve = (screenWidth.value * HEADER_SIDE_FRAC)
                    .coerceAtMost(HEADER_SIDE_CEIL_DP)
                    .dp,
                topChromePad = (screenShort * TOP_CHROME_PAD_FRAC).dp,
                topChromeGap = (screenShort * TOP_CHROME_GAP_FRAC).dp,
            )
        }
    }
}
