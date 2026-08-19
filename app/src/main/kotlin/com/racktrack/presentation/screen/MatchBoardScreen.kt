package com.racktrack.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.domain.MatchEngine
import com.racktrack.domain.MatchStats
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.Player
import com.racktrack.domain.model.PlayerId
import com.racktrack.domain.model.PushOutPhase
import com.racktrack.presentation.component.BoardMetrics
import com.racktrack.presentation.component.CueBallBreakIndicator
import com.racktrack.presentation.component.MatchPauseButton
import com.racktrack.presentation.component.PlayerStatIcons
import com.racktrack.presentation.component.SettingsGearButton
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.TexturedOutlineAction
import com.racktrack.presentation.component.TwoChoiceModal
import com.racktrack.presentation.theme.ButtonDry
import com.racktrack.presentation.theme.ButtonDryDark
import com.racktrack.presentation.theme.ButtonDryLight
import com.racktrack.presentation.theme.ButtonFoul
import com.racktrack.presentation.theme.ButtonFoulDark
import com.racktrack.presentation.theme.ButtonFoulLight
import com.racktrack.presentation.theme.ButtonGolden
import com.racktrack.presentation.theme.ButtonGoldenDark
import com.racktrack.presentation.theme.ButtonGoldenLight
import com.racktrack.presentation.theme.ButtonPlus
import com.racktrack.presentation.theme.ButtonPlusDark
import com.racktrack.presentation.theme.ButtonPlusLight
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.ButtonRunOutDark
import com.racktrack.presentation.theme.ButtonRunOutLight
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.theme.ScoreWhite

private enum class BreakAnchor {
    TowardEnd,
    TowardStart,
}

@Composable
fun MatchBoardScreen(
    match: Match,
    onPlusOne: (PlayerId) -> Unit,
    onRunOut: (PlayerId) -> Unit,
    onGoldenBreak: (PlayerId) -> Unit,
    onDryBreak: (PlayerId) -> Unit,
    onEightBallLoss: (PlayerId) -> Unit,
    onAddPoints: (PlayerId, Int) -> Unit = { _, _ -> },
    onPassWithRemaining: (PlayerId, Int, Int) -> Unit = { _, _, _ -> },
    onBreakFoul: (PlayerId) -> Unit = {},
    onAcceptIllegalOpen: () -> Unit = {},
    onAnnouncePushOut: (PlayerId) -> Unit = {},
    onResolvePushOutClean: (PlayerId) -> Unit = {},
    onResolvePushOutFoul: (PlayerId) -> Unit = {},
    onTakePushOut: () -> Unit = {},
    onReturnPushOut: () -> Unit = {},
    onFoul: (PlayerId) -> Unit,
    onFoulWithRemaining: (PlayerId, Int, Int) -> Unit = { _, _, _ -> },
    onClearFouls: (PlayerId) -> Unit = {},
    onUndo: () -> Unit,
    onNewMatch: () -> Unit,
    onOpenSettings: () -> Unit = {},
    matchPaused: Boolean = false,
    onTogglePause: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val felt = LocalFeltPalette.current
    val playEnabled = match.status == MatchStatus.IN_PROGRESS && !matchPaused

    FeltBackground(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenW = maxWidth
            val screenH = maxHeight
            val chrome = BoardMetrics.fromPane(
                paneWidth = if (landscape) screenW / 2 else screenW,
                paneHeight = if (landscape) screenH else screenH / 2,
                screenWidth = screenW,
                screenHeight = screenH,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(
                        horizontal = chrome.screenPadH,
                        vertical = chrome.screenPadV,
                    ),
            ) {
                Text(
                    text = if (match.gameMode.isPointScoring) {
                        fourteenOneHeader(match)
                    } else {
                        "${match.gameMode.shortLabel()}  ·  RACE TO ${match.racksToWin}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = felt.accentLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = chrome.headerSideReserve),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(chrome.headerToBoardGap))

                if (match.gameMode.isPointScoring) {
                    FourteenOneBoardContent(
                        match = match,
                        landscape = landscape,
                        onAddPoints = onAddPoints,
                        onPassWithRemaining = onPassWithRemaining,
                        onFoulWithRemaining = onFoulWithRemaining,
                        onBreakFoul = onBreakFoul,
                        onAcceptIllegalOpen = onAcceptIllegalOpen,
                        screenWidth = screenW,
                        screenHeight = screenH,
                        medianThickness = chrome.medianThickness,
                        medianInset = chrome.medianInset,
                        topChromePad = chrome.topChromePad,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                } else if (landscape) {
                    LandscapeBoard(
                        match = match,
                        onPlusOne = onPlusOne,
                        onRunOut = onRunOut,
                        onGoldenBreak = onGoldenBreak,
                        onDryBreak = onDryBreak,
                        onEightBallLoss = onEightBallLoss,
                        onAnnouncePushOut = onAnnouncePushOut,
                        onFoul = onFoul,
                        onClearFouls = onClearFouls,
                        screenWidth = screenW,
                        screenHeight = screenH,
                        medianThickness = chrome.medianThickness,
                        medianInset = chrome.medianInset,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                } else {
                    PortraitBoard(
                        match = match,
                        onPlusOne = onPlusOne,
                        onRunOut = onRunOut,
                        onGoldenBreak = onGoldenBreak,
                        onDryBreak = onDryBreak,
                        onEightBallLoss = onEightBallLoss,
                        onAnnouncePushOut = onAnnouncePushOut,
                        onFoul = onFoul,
                        onClearFouls = onClearFouls,
                        screenWidth = screenW,
                        screenHeight = screenH,
                        medianThickness = chrome.medianThickness,
                        medianInset = chrome.medianInset,
                        topChromePad = chrome.topChromePad,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(chrome.boardToFooterGap))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TexturedOutlineAction(
                        label = "UNDO",
                        onClick = onUndo,
                        enabled = match.history.isNotEmpty() && playEnabled,
                        height = chrome.footerActionHeight,
                    )
                    Spacer(modifier = Modifier.width(chrome.footerGap))
                    TexturedOutlineAction(
                        label = if (match.solo) "NEW TRAINING" else "NEW MATCH",
                        onClick = onNewMatch,
                        enabled = true,
                        height = chrome.footerActionHeight,
                    )
                }
            }

            if (matchPaused && match.status == MatchStatus.IN_PROGRESS) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTogglePause,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = (minOf(screenW.value, screenH.value) * 0.09f).sp,
                            ),
                            color = OutlineWarm,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(chrome.headerToBoardGap))
                        Text(
                            text = if (match.solo) {
                                "Training timing stopped  ·  Tap to resume"
                            } else {
                                "Match timing stopped  ·  Tap to resume"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = (minOf(screenW.value, screenH.value) * 0.035f).sp,
                            ),
                            color = ScoreWhite.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(top = chrome.topChromePad, end = chrome.screenPadH),
                horizontalArrangement = Arrangement.spacedBy(chrome.topChromeGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (match.status == MatchStatus.IN_PROGRESS) {
                    MatchPauseButton(
                        paused = matchPaused,
                        onClick = onTogglePause,
                        solo = match.solo,
                    )
                }
                SettingsGearButton(onClick = onOpenSettings)
            }

            if (match.status == MatchStatus.COMPLETED) {
                MatchSummaryModal(
                    summary = MatchStats.summarize(match),
                    onNewMatch = onNewMatch,
                )
            }

            if (playEnabled && match.pushOutPhase == PushOutPhase.ANNOUNCED) {
                val announcer = match.currentShooterId
                TwoChoiceModal(
                    title = "PUSH-OUT",
                    subtitle = "Shot result?",
                    primaryLabel = "CLEAN",
                    primaryBase = ButtonRunOut,
                    primaryLight = ButtonRunOutLight,
                    primaryDark = ButtonRunOutDark,
                    secondaryLabel = "FOUL",
                    secondaryBase = ButtonFoul,
                    secondaryLight = ButtonFoulLight,
                    secondaryDark = ButtonFoulDark,
                    onPrimary = { onResolvePushOutClean(announcer) },
                    onSecondary = { onResolvePushOutFoul(announcer) },
                )
            }

            if (playEnabled && match.pushOutPhase == PushOutPhase.AWAITING_CHOICE) {
                TwoChoiceModal(
                    title = "PUSH-OUT",
                    subtitle = "Opponent chooses",
                    primaryLabel = "TAKE",
                    primaryBase = ButtonRunOut,
                    primaryLight = ButtonRunOutLight,
                    primaryDark = ButtonRunOutDark,
                    secondaryLabel = "GIVE BACK",
                    secondaryBase = ButtonDry,
                    secondaryLight = ButtonDryLight,
                    secondaryDark = ButtonDryDark,
                    onPrimary = onTakePushOut,
                    onSecondary = onReturnPushOut,
                )
            }
        }
    }
}

@Composable
private fun LandscapeBoard(
    match: Match,
    onPlusOne: (PlayerId) -> Unit,
    onRunOut: (PlayerId) -> Unit,
    onGoldenBreak: (PlayerId) -> Unit,
    onDryBreak: (PlayerId) -> Unit,
    onEightBallLoss: (PlayerId) -> Unit,
    onAnnouncePushOut: (PlayerId) -> Unit,
    onFoul: (PlayerId) -> Unit,
    onClearFouls: (PlayerId) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
    medianThickness: Dp,
    medianInset: Dp,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        PlayerPanel(
            match = match,
            player = match.player1,
            score = match.score1,
            fouls = match.foul1,
            runOuts = match.runOut1,
            hasBreak = match.currentShooterId == match.player1.id,
            breakAnchor = BreakAnchor.TowardEnd,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player1.id) },
            onRunOut = { onRunOut(match.player1.id) },
            onGoldenBreak = { onGoldenBreak(match.player1.id) },
            onDryBreak = { onDryBreak(match.player1.id) },
            onEightBallLoss = { onEightBallLoss(match.player1.id) },
            onAnnouncePushOut = { onAnnouncePushOut(match.player1.id) },
            onFoul = { onFoul(match.player1.id) },
            onClearFouls = { onClearFouls(match.player1.id) },
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        MedianDivider(
            landscape = true,
            thickness = medianThickness,
            inset = medianInset,
        )
        PlayerPanel(
            match = match,
            player = match.player2,
            score = match.score2,
            fouls = match.foul2,
            runOuts = match.runOut2,
            hasBreak = match.currentShooterId == match.player2.id,
            breakAnchor = BreakAnchor.TowardStart,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player2.id) },
            onRunOut = { onRunOut(match.player2.id) },
            onGoldenBreak = { onGoldenBreak(match.player2.id) },
            onDryBreak = { onDryBreak(match.player2.id) },
            onEightBallLoss = { onEightBallLoss(match.player2.id) },
            onAnnouncePushOut = { onAnnouncePushOut(match.player2.id) },
            onFoul = { onFoul(match.player2.id) },
            onClearFouls = { onClearFouls(match.player2.id) },
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun PortraitBoard(
    match: Match,
    onPlusOne: (PlayerId) -> Unit,
    onRunOut: (PlayerId) -> Unit,
    onGoldenBreak: (PlayerId) -> Unit,
    onDryBreak: (PlayerId) -> Unit,
    onEightBallLoss: (PlayerId) -> Unit,
    onAnnouncePushOut: (PlayerId) -> Unit,
    onFoul: (PlayerId) -> Unit,
    onClearFouls: (PlayerId) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
    medianThickness: Dp,
    medianInset: Dp,
    topChromePad: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlayerPanel(
            match = match,
            player = match.player1,
            score = match.score1,
            fouls = match.foul1,
            runOuts = match.runOut1,
            hasBreak = match.currentShooterId == match.player1.id,
            breakAnchor = BreakAnchor.TowardEnd,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player1.id) },
            onRunOut = { onRunOut(match.player1.id) },
            onGoldenBreak = { onGoldenBreak(match.player1.id) },
            onDryBreak = { onDryBreak(match.player1.id) },
            onEightBallLoss = { onEightBallLoss(match.player1.id) },
            onAnnouncePushOut = { onAnnouncePushOut(match.player1.id) },
            onFoul = { onFoul(match.player1.id) },
            onClearFouls = { onClearFouls(match.player1.id) },
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = topChromePad, bottom = medianInset),
        )
        MedianDivider(
            landscape = false,
            thickness = medianThickness,
            inset = medianInset,
        )
        PlayerPanel(
            match = match,
            player = match.player2,
            score = match.score2,
            fouls = match.foul2,
            runOuts = match.runOut2,
            hasBreak = match.currentShooterId == match.player2.id,
            breakAnchor = BreakAnchor.TowardEnd,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player2.id) },
            onRunOut = { onRunOut(match.player2.id) },
            onGoldenBreak = { onGoldenBreak(match.player2.id) },
            onDryBreak = { onDryBreak(match.player2.id) },
            onEightBallLoss = { onEightBallLoss(match.player2.id) },
            onAnnouncePushOut = { onAnnouncePushOut(match.player2.id) },
            onFoul = { onFoul(match.player2.id) },
            onClearFouls = { onClearFouls(match.player2.id) },
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = medianInset),
        )
    }
}

@Composable
private fun MedianDivider(
    landscape: Boolean,
    thickness: Dp,
    inset: Dp,
) {
    val brush = if (landscape) {
        Brush.verticalGradient(
            listOf(Color.Transparent, ScoreWhite.copy(alpha = 0.35f), Color.Transparent),
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color.Transparent, ScoreWhite.copy(alpha = 0.35f), Color.Transparent),
        )
    }
    Box(
        modifier = if (landscape) {
            Modifier
                .width(thickness)
                .fillMaxHeight()
                .padding(vertical = inset)
                .background(brush)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(horizontal = inset)
                .height(thickness)
                .background(brush)
        },
    )
}

@Composable
private fun PlayerPanel(
    match: Match,
    player: Player,
    score: Int,
    fouls: Int,
    runOuts: Int,
    hasBreak: Boolean,
    breakAnchor: BreakAnchor,
    enabled: Boolean,
    onPlusOne: () -> Unit,
    onRunOut: () -> Unit,
    onGoldenBreak: () -> Unit,
    onDryBreak: () -> Unit,
    onEightBallLoss: () -> Unit,
    onAnnouncePushOut: () -> Unit,
    onFoul: () -> Unit,
    onClearFouls: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val canRunOut = MatchEngine.canBreakAndClear(match, player.id)
    val canGolden = MatchEngine.canRecordGoldenBreak(match, player.id)
    val canDry = MatchEngine.canRecordDryBreak(match, player.id)
    val canEarlyEight = MatchEngine.canRecordEightBallLoss(match, player.id)
    val canPushOut = MatchEngine.canAnnouncePushOut(match, player.id)
    val showFoulWarning = match.gameMode.supportsThreeFoulRackLoss
    val showModeExtras =
        match.gameMode.supportsGoldenBreak ||
            match.gameMode.supportsDryBreak ||
            match.gameMode.supportsEightBallLoss ||
            match.gameMode.supportsPushOut

    BoxWithConstraints(modifier = modifier) {
        val actionRows = if (showModeExtras) 2 else 1
        val metrics = BoardMetrics.fromPane(
            paneWidth = maxWidth,
            paneHeight = maxHeight,
            actionRowCount = actionRows,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )
        val nameStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = metrics.nameSp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = metrics.panelPaddingH,
                    vertical = metrics.panelPaddingV,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = player.name.uppercase(),
                style = nameStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(metrics.nameToScoreGap))

            RaceScoreCluster(
                match = match,
                score = score,
                fouls = fouls,
                runOuts = runOuts,
                hasBreak = hasBreak,
                breakAnchor = breakAnchor,
                enabled = enabled,
                showFoulWarning = showFoulWarning,
                onClearFouls = onClearFouls,
                metrics = metrics,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(metrics.scoreToActionsGap))

            RaceActionButtons(
                gameMode = match.gameMode,
                enabled = enabled,
                canRunOut = canRunOut,
                canGolden = canGolden,
                canDry = canDry,
                canEarlyEight = canEarlyEight,
                canPushOut = canPushOut,
                showModeExtras = showModeExtras,
                actionHeight = metrics.actionHeight,
                actionGap = metrics.actionGap,
                actionCorner = metrics.actionCorner,
                onPlusOne = onPlusOne,
                onRunOut = onRunOut,
                onGoldenBreak = onGoldenBreak,
                onDryBreak = onDryBreak,
                onEightBallLoss = onEightBallLoss,
                onAnnouncePushOut = onAnnouncePushOut,
                onFoul = onFoul,
            )
        }
    }
}

@Composable
private fun RaceScoreCluster(
    match: Match,
    score: Int,
    fouls: Int,
    runOuts: Int,
    hasBreak: Boolean,
    breakAnchor: BreakAnchor,
    enabled: Boolean,
    showFoulWarning: Boolean,
    onClearFouls: () -> Unit,
    metrics: BoardMetrics,
    modifier: Modifier = Modifier,
) {
    val nearRackLoss =
        showFoulWarning && fouls == MatchEngine.CONSECUTIVE_FOULS_TO_LOSE_RACK - 1
    val breakAlpha by animateFloatAsState(
        targetValue = if (hasBreak) 1f else 0f,
        label = "break-alpha",
    )
    val warnAlpha by animateFloatAsState(
        targetValue = if (nearRackLoss) 1f else 0f,
        label = "foul-warn-alpha",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = score,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "score",
                ) { value ->
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = metrics.scoreSp),
                        maxLines = 1,
                    )
                }
                CueBallBreakIndicator(
                    modifier = Modifier
                        .align(breakAnchor.alignment())
                        .padding(breakAnchor.inset(metrics.cueInset))
                        .alpha(breakAlpha),
                    size = metrics.cueBallSize,
                )
            }
            PlayerStatIcons(
                gameMode = match.gameMode,
                runOuts = runOuts,
                consecutiveFouls = fouls,
                maxConsecutiveFouls = if (showFoulWarning) {
                    MatchEngine.CONSECUTIVE_FOULS_TO_LOSE_RACK
                } else {
                    0
                },
                onClearFouls = if (enabled && showFoulWarning) onClearFouls else null,
                iconSize = metrics.statIconSize,
                iconGap = metrics.statIconGap,
                clearHintSp = metrics.clearHintSp,
                countSp = metrics.statCountSp,
                modifier = Modifier
                    .padding(top = metrics.nameToScoreGap)
                    .heightIn(min = metrics.statIconSize),
            )
            if (nearRackLoss) {
                Text(
                    text = "1 MORE FOUL = RACK LOSS",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = metrics.warnSp),
                    color = ButtonFoulLight.copy(alpha = warnAlpha),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = metrics.nameToScoreGap)
                        .alpha(warnAlpha),
                )
            }
        }
    }
}

@Composable
private fun RaceActionButtons(
    gameMode: GameMode,
    enabled: Boolean,
    canRunOut: Boolean,
    canGolden: Boolean,
    canDry: Boolean,
    canEarlyEight: Boolean,
    canPushOut: Boolean,
    showModeExtras: Boolean,
    actionHeight: Dp,
    actionGap: Dp,
    actionCorner: Dp,
    onPlusOne: () -> Unit,
    onRunOut: () -> Unit,
    onGoldenBreak: () -> Unit,
    onDryBreak: () -> Unit,
    onEightBallLoss: () -> Unit,
    onAnnouncePushOut: () -> Unit,
    onFoul: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(actionGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(actionGap),
        ) {
            TexturedActionButton(
                label = "+1",
                base = ButtonPlus,
                light = ButtonPlusLight,
                dark = ButtonPlusDark,
                enabled = enabled,
                onClick = onPlusOne,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
            TexturedActionButton(
                label = "RUN OUT",
                base = ButtonRunOut,
                light = ButtonRunOutLight,
                dark = ButtonRunOutDark,
                enabled = enabled && canRunOut,
                onClick = onRunOut,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
            TexturedActionButton(
                label = "FOUL",
                base = ButtonFoul,
                light = ButtonFoulLight,
                dark = ButtonFoulDark,
                enabled = enabled,
                onClick = onFoul,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
        }
        if (showModeExtras) {
            RaceModeExtraButtons(
                gameMode = gameMode,
                enabled = enabled,
                canGolden = canGolden,
                canDry = canDry,
                canEarlyEight = canEarlyEight,
                canPushOut = canPushOut,
                actionHeight = actionHeight,
                actionGap = actionGap,
                actionCorner = actionCorner,
                onGoldenBreak = onGoldenBreak,
                onDryBreak = onDryBreak,
                onEightBallLoss = onEightBallLoss,
                onAnnouncePushOut = onAnnouncePushOut,
            )
        }
    }
}

@Composable
private fun RaceModeExtraButtons(
    gameMode: GameMode,
    enabled: Boolean,
    canGolden: Boolean,
    canDry: Boolean,
    canEarlyEight: Boolean,
    canPushOut: Boolean,
    actionHeight: Dp,
    actionGap: Dp,
    actionCorner: Dp,
    onGoldenBreak: () -> Unit,
    onDryBreak: () -> Unit,
    onEightBallLoss: () -> Unit,
    onAnnouncePushOut: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(actionGap),
    ) {
        if (gameMode.supportsGoldenBreak) {
            TexturedActionButton(
                label = "GOLDEN",
                base = ButtonGolden,
                light = ButtonGoldenLight,
                dark = ButtonGoldenDark,
                enabled = enabled && canGolden,
                onClick = onGoldenBreak,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
        }
        if (gameMode.supportsEightBallLoss) {
            TexturedActionButton(
                label = "EARLY 8",
                base = ButtonFoul,
                light = ButtonFoulLight,
                dark = ButtonFoulDark,
                enabled = enabled && canEarlyEight,
                onClick = onEightBallLoss,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
        }
        if (gameMode.supportsDryBreak) {
            TexturedActionButton(
                label = "DRY",
                base = ButtonDry,
                light = ButtonDryLight,
                dark = ButtonDryDark,
                enabled = enabled && canDry,
                onClick = onDryBreak,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
        }
        if (gameMode.supportsPushOut) {
            TexturedActionButton(
                label = "PUSH OUT",
                base = ButtonDry,
                light = ButtonDryLight,
                dark = ButtonDryDark,
                enabled = enabled && canPushOut,
                onClick = onAnnouncePushOut,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                corner = actionCorner,
            )
        }
    }
}

private fun BreakAnchor.alignment(): Alignment =
    when (this) {
        BreakAnchor.TowardEnd -> Alignment.CenterEnd
        BreakAnchor.TowardStart -> Alignment.CenterStart
    }

private fun BreakAnchor.inset(inset: Dp): PaddingValues =
    when (this) {
        BreakAnchor.TowardEnd -> PaddingValues(end = inset)
        BreakAnchor.TowardStart -> PaddingValues(start = inset)
    }

private fun GameMode.shortLabel(): String =
    when (this) {
        GameMode.EIGHT_BALL -> "8-BALL"
        GameMode.NINE_BALL -> "9-BALL"
        GameMode.TEN_BALL -> "10-BALL"
        GameMode.FOURTEEN_ONE -> "14/1"
    }

@Preview(widthDp = 820, heightDp = 380, showBackground = true)
@Composable
private fun MatchBoardLandscapePreview() {
    val match = Match.start(
        "Alex",
        "Sam",
        6,
        initialBreakerIsPlayer1 = true,
        startedAtMillis = 0L,
        gameMode = GameMode.NINE_BALL,
    )
    RackTrackTheme {
        MatchBoardScreen(
            match = match,
            onPlusOne = {},
            onRunOut = {},
            onGoldenBreak = {},
            onDryBreak = {},
            onEightBallLoss = {},
            onFoul = {},
            onUndo = {},
            onNewMatch = {},
        )
    }
}

@Preview(widthDp = 380, heightDp = 820, showBackground = true)
@Composable
private fun MatchBoardPortraitPreview() {
    val match = Match.start(
        "Alex",
        "Sam",
        6,
        initialBreakerIsPlayer1 = true,
        startedAtMillis = 0L,
        gameMode = GameMode.EIGHT_BALL,
    )
    RackTrackTheme {
        MatchBoardScreen(
            match = match,
            onPlusOne = {},
            onRunOut = {},
            onGoldenBreak = {},
            onDryBreak = {},
            onEightBallLoss = {},
            onFoul = {},
            onUndo = {},
            onNewMatch = {},
        )
    }
}
