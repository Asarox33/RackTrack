package com.racktrack.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.racktrack.domain.MatchEngine
import com.racktrack.domain.MatchStats
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.Player
import com.racktrack.domain.model.PlayerId
import com.racktrack.presentation.component.CueBallBreakIndicator
import com.racktrack.presentation.component.PlayerStatIcons
import com.racktrack.presentation.component.SettingsGearButton
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.TexturedOutlineAction
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
    onPass: (PlayerId) -> Unit = {},
    onBreakFoul: (PlayerId) -> Unit = {},
    onFoul: (PlayerId) -> Unit,
    onUndo: () -> Unit,
    onNewMatch: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    FeltBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (match.gameMode.isPointScoring) {
                        fourteenOneHeader(match)
                    } else {
                        "${match.gameMode.shortLabel()}  ·  RACE TO ${match.racksToWin}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 44.dp),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (match.gameMode.isPointScoring) {
                    FourteenOneBoardContent(
                        match = match,
                        landscape = landscape,
                        onAddPoints = onAddPoints,
                        onPass = onPass,
                        onFoul = onFoul,
                        onBreakFoul = onBreakFoul,
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
                        onFoul = onFoul,
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
                        onFoul = onFoul,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TexturedOutlineAction(
                        label = "UNDO",
                        onClick = onUndo,
                        enabled = match.history.isNotEmpty() &&
                            match.status == MatchStatus.IN_PROGRESS,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TexturedOutlineAction(
                        label = "NEW MATCH",
                        onClick = onNewMatch,
                        enabled = true,
                    )
                }
            }

            SettingsGearButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(top = 6.dp, end = 10.dp),
            )

            if (match.status == MatchStatus.COMPLETED) {
                MatchSummaryModal(
                    summary = MatchStats.summarize(match),
                    onNewMatch = onNewMatch,
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
    onFoul: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        PlayerPanel(
            match = match,
            player = match.player1,
            score = match.score1,
            fouls = match.foul1,
            runOuts = match.runOut1,
            hasBreak = match.currentBreakerId == match.player1.id,
            breakAnchor = BreakAnchor.TowardEnd,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player1.id) },
            onRunOut = { onRunOut(match.player1.id) },
            onGoldenBreak = { onGoldenBreak(match.player1.id) },
            onDryBreak = { onDryBreak(match.player1.id) },
            onEightBallLoss = { onEightBallLoss(match.player1.id) },
            onFoul = { onFoul(match.player1.id) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        MedianDivider(landscape = true)
        PlayerPanel(
            match = match,
            player = match.player2,
            score = match.score2,
            fouls = match.foul2,
            runOuts = match.runOut2,
            hasBreak = match.currentBreakerId == match.player2.id,
            breakAnchor = BreakAnchor.TowardStart,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player2.id) },
            onRunOut = { onRunOut(match.player2.id) },
            onGoldenBreak = { onGoldenBreak(match.player2.id) },
            onDryBreak = { onDryBreak(match.player2.id) },
            onEightBallLoss = { onEightBallLoss(match.player2.id) },
            onFoul = { onFoul(match.player2.id) },
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
    onFoul: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlayerPanel(
            match = match,
            player = match.player1,
            score = match.score1,
            fouls = match.foul1,
            runOuts = match.runOut1,
            hasBreak = match.currentBreakerId == match.player1.id,
            breakAnchor = BreakAnchor.TowardEnd,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player1.id) },
            onRunOut = { onRunOut(match.player1.id) },
            onGoldenBreak = { onGoldenBreak(match.player1.id) },
            onDryBreak = { onDryBreak(match.player1.id) },
            onEightBallLoss = { onEightBallLoss(match.player1.id) },
            onFoul = { onFoul(match.player1.id) },
            compact = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        MedianDivider(landscape = false)
        PlayerPanel(
            match = match,
            player = match.player2,
            score = match.score2,
            fouls = match.foul2,
            runOuts = match.runOut2,
            hasBreak = match.currentBreakerId == match.player2.id,
            breakAnchor = BreakAnchor.TowardEnd,
            enabled = match.status == MatchStatus.IN_PROGRESS,
            onPlusOne = { onPlusOne(match.player2.id) },
            onRunOut = { onRunOut(match.player2.id) },
            onGoldenBreak = { onGoldenBreak(match.player2.id) },
            onDryBreak = { onDryBreak(match.player2.id) },
            onEightBallLoss = { onEightBallLoss(match.player2.id) },
            onFoul = { onFoul(match.player2.id) },
            compact = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = PortraitPlayer2TopInset),
        )
    }
}

@Composable
private fun MedianDivider(landscape: Boolean) {
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
                .width(3.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .background(brush)
        } else {
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(horizontal = 12.dp)
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
    onFoul: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val scoreSize = if (compact) 72.sp else 92.sp
    val nameStyle = if (compact) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.headlineLarge
    }
    val actionHeight = if (compact) ActionRowHeightCompact else ActionRowHeight
    val cueSize = if (compact) BreakCueBallSizeCompact else BreakCueBallSize
    val canRunOut = MatchEngine.canBreakAndClear(match, player.id)
    val canGolden = MatchEngine.canRecordGoldenBreak(match, player.id)
    val canDry = MatchEngine.canRecordDryBreak(match, player.id)
    val canEarlyEight = MatchEngine.canRecordEightBallLoss(match, player.id)
    val showFoulWarning = match.gameMode.supportsThreeFoulRackLoss
    val showModeExtras =
        match.gameMode.supportsGoldenBreak ||
            match.gameMode.supportsDryBreak ||
            match.gameMode.supportsEightBallLoss

    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = player.name.uppercase(),
            style = nameStyle,
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val breakAlpha by animateFloatAsState(
                    targetValue = if (hasBreak) 1f else 0f,
                    label = "break-alpha",
                )
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
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = scoreSize),
                        )
                    }
                    CueBallBreakIndicator(
                        modifier = Modifier
                            .align(breakAnchor.alignment())
                            .padding(breakAnchor.inset())
                            .alpha(breakAlpha),
                        size = cueSize,
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
                    modifier = Modifier.padding(top = 12.dp),
                )
                val warnAlpha by animateFloatAsState(
                    targetValue = if (
                        showFoulWarning &&
                        fouls == MatchEngine.CONSECUTIVE_FOULS_TO_LOSE_RACK - 1
                    ) {
                        1f
                    } else {
                        0f
                    },
                    label = "foul-warn-alpha",
                )
                Box(
                    modifier = Modifier
                        .height(FoulWarnSlotHeight)
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "1 MORE FOUL = RACK LOSS",
                        style = MaterialTheme.typography.titleLarge,
                        color = ButtonFoulLight.copy(alpha = warnAlpha),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(warnAlpha),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                )
            }
            if (showModeExtras) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (match.gameMode.supportsGoldenBreak) {
                        TexturedActionButton(
                            label = "GOLDEN",
                            base = ButtonGolden,
                            light = ButtonGoldenLight,
                            dark = ButtonGoldenDark,
                            enabled = enabled && canGolden,
                            onClick = onGoldenBreak,
                            modifier = Modifier.weight(1f),
                            height = actionHeight,
                        )
                    }
                    if (match.gameMode.supportsEightBallLoss) {
                        TexturedActionButton(
                            label = "EARLY 8",
                            base = ButtonFoul,
                            light = ButtonFoulLight,
                            dark = ButtonFoulDark,
                            enabled = enabled && canEarlyEight,
                            onClick = onEightBallLoss,
                            modifier = Modifier.weight(1f),
                            height = actionHeight,
                        )
                    }
                    if (match.gameMode.supportsDryBreak) {
                        TexturedActionButton(
                            label = "DRY",
                            base = ButtonDry,
                            light = ButtonDryLight,
                            dark = ButtonDryDark,
                            enabled = enabled && canDry,
                            onClick = onDryBreak,
                            modifier = Modifier.weight(1f),
                            height = actionHeight,
                        )
                    }
                }
            }
        }
    }
}

private fun BreakAnchor.alignment(): Alignment =
    when (this) {
        BreakAnchor.TowardEnd -> Alignment.CenterEnd
        BreakAnchor.TowardStart -> Alignment.CenterStart
    }

private fun BreakAnchor.inset(): PaddingValues =
    when (this) {
        BreakAnchor.TowardEnd -> PaddingValues(end = BreakMedianInset)
        BreakAnchor.TowardStart -> PaddingValues(start = BreakMedianInset)
    }

private fun GameMode.shortLabel(): String =
    when (this) {
        GameMode.EIGHT_BALL -> "8-BALL"
        GameMode.NINE_BALL -> "9-BALL"
        GameMode.TEN_BALL -> "10-BALL"
        GameMode.FOURTEEN_ONE -> "14/1"
    }

private val BreakCueBallSize: Dp = 52.dp
private val BreakCueBallSizeCompact: Dp = 44.dp
private val BreakMedianInset: Dp = 36.dp
private val PortraitPlayer2TopInset: Dp = 14.dp
private val FoulWarnSlotHeight: Dp = 36.dp
private val ActionRowHeight: Dp = 52.dp
private val ActionRowHeightCompact: Dp = 48.dp

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
