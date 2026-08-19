package com.racktrack.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.domain.FourteenOneEngine
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.Player
import com.racktrack.domain.model.PlayerId
import com.racktrack.presentation.component.BoardMetrics
import com.racktrack.presentation.component.CueBallBreakIndicator
import com.racktrack.presentation.component.ScrollMoreHint
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.TwoChoiceModal
import com.racktrack.presentation.component.rememberClickHaptic
import com.racktrack.presentation.theme.ButtonFoul
import com.racktrack.presentation.theme.ButtonFoulDark
import com.racktrack.presentation.theme.ButtonFoulLight
import com.racktrack.presentation.theme.ButtonPlus
import com.racktrack.presentation.theme.ButtonPlusDark
import com.racktrack.presentation.theme.ButtonPlusLight
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.ButtonRunOutDark
import com.racktrack.presentation.theme.ButtonRunOutLight
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite

private enum class VisitEndAction { PASS, FOUL }

private data class VisitEndDraft(
    val playerId: PlayerId,
    val action: VisitEndAction,
)

@Composable
fun FourteenOneBoardContent(
    match: Match,
    landscape: Boolean,
    onAddPoints: (PlayerId, Int) -> Unit,
    onPassWithRemaining: (PlayerId, Int, Int) -> Unit,
    onFoulWithRemaining: (PlayerId, Int, Int) -> Unit,
    onBreakFoul: (PlayerId) -> Unit,
    onAcceptIllegalOpen: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var visitEnd by remember { mutableStateOf<VisitEndDraft?>(null) }
    var dismissedIllegalOpenAt by remember { mutableStateOf<Long?>(null) }
    val lastEvent = match.history.lastOrNull()
    val showIllegalOpenChoice =
        match.awaitingOpeningBreak &&
            lastEvent?.type == MatchEventType.BREAK_FOUL &&
            dismissedIllegalOpenAt != lastEvent.atMillis

    Box(modifier = modifier) {
        if (match.solo) {
            FourteenOnePlayerPanel(
                match = match,
                player = match.player1,
                score = match.score1,
                innings = match.innings1,
                fouls = match.foul1,
                highRun = match.highRun1,
                hasHand = true,
                handTowardEnd = true,
                onAddPoints = onAddPoints,
                onRequestPass = {
                    visitEnd = VisitEndDraft(match.player1.id, VisitEndAction.PASS)
                },
                onRequestFoul = {
                    visitEnd = VisitEndDraft(match.player1.id, VisitEndAction.FOUL)
                },
                onBreakFoul = onBreakFoul,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (landscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                FourteenOnePlayerPanel(
                    match = match,
                    player = match.player1,
                    score = match.score1,
                    innings = match.innings1,
                    fouls = match.foul1,
                    highRun = match.highRun1,
                    hasHand = match.currentShooterId == match.player1.id,
                    handTowardEnd = true,
                    onAddPoints = onAddPoints,
                    onRequestPass = {
                        visitEnd = VisitEndDraft(match.player1.id, VisitEndAction.PASS)
                    },
                    onRequestFoul = {
                        visitEnd = VisitEndDraft(match.player1.id, VisitEndAction.FOUL)
                    },
                    onBreakFoul = onBreakFoul,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                FourteenMedianDivider(landscape = true)
                FourteenOnePlayerPanel(
                    match = match,
                    player = match.player2,
                    score = match.score2,
                    innings = match.innings2,
                    fouls = match.foul2,
                    highRun = match.highRun2,
                    hasHand = match.currentShooterId == match.player2.id,
                    handTowardEnd = false,
                    onAddPoints = onAddPoints,
                    onRequestPass = {
                        visitEnd = VisitEndDraft(match.player2.id, VisitEndAction.PASS)
                    },
                    onRequestFoul = {
                        visitEnd = VisitEndDraft(match.player2.id, VisitEndAction.FOUL)
                    },
                    onBreakFoul = onBreakFoul,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                FourteenOnePlayerPanel(
                    match = match,
                    player = match.player1,
                    score = match.score1,
                    innings = match.innings1,
                    fouls = match.foul1,
                    highRun = match.highRun1,
                    hasHand = match.currentShooterId == match.player1.id,
                    handTowardEnd = true,
                    onAddPoints = onAddPoints,
                    onRequestPass = {
                        visitEnd = VisitEndDraft(match.player1.id, VisitEndAction.PASS)
                    },
                    onRequestFoul = {
                        visitEnd = VisitEndDraft(match.player1.id, VisitEndAction.FOUL)
                    },
                    onBreakFoul = onBreakFoul,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = PortraitNameTopInset, bottom = PortraitMedianInset),
                )
                FourteenMedianDivider(landscape = false)
                FourteenOnePlayerPanel(
                    match = match,
                    player = match.player2,
                    score = match.score2,
                    innings = match.innings2,
                    fouls = match.foul2,
                    highRun = match.highRun2,
                    hasHand = match.currentShooterId == match.player2.id,
                    handTowardEnd = true,
                    onAddPoints = onAddPoints,
                    onRequestPass = {
                        visitEnd = VisitEndDraft(match.player2.id, VisitEndAction.PASS)
                    },
                    onRequestFoul = {
                        visitEnd = VisitEndDraft(match.player2.id, VisitEndAction.FOUL)
                    },
                    onBreakFoul = onBreakFoul,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = PortraitMedianInset),
                )
            }
        }

        visitEnd?.let { draft ->
            VisitEndBallsModal(
                match = match,
                action = draft.action,
                onDismiss = { visitEnd = null },
                onConfirm = { remaining, priorPoints ->
                    when (draft.action) {
                        VisitEndAction.PASS ->
                            onPassWithRemaining(draft.playerId, remaining, priorPoints)
                        VisitEndAction.FOUL ->
                            onFoulWithRemaining(draft.playerId, remaining, priorPoints)
                    }
                    visitEnd = null
                },
            )
        }

        if (showIllegalOpenChoice) {
            TwoChoiceModal(
                title = "ILLEGAL OPEN",
                subtitle = "Opponent accepts the table?",
                primaryLabel = "ACCEPT",
                primaryBase = ButtonRunOut,
                primaryLight = ButtonRunOutLight,
                primaryDark = ButtonRunOutDark,
                secondaryLabel = "RE-BREAK",
                secondaryBase = ButtonFoul,
                secondaryLight = ButtonFoulLight,
                secondaryDark = ButtonFoulDark,
                onPrimary = {
                    onAcceptIllegalOpen()
                    dismissedIllegalOpenAt = lastEvent.atMillis
                },
                onSecondary = {
                    dismissedIllegalOpenAt = lastEvent.atMillis
                },
            )
        }
    }
}

fun fourteenOneHeader(match: Match): String {
    val inningsLabel = if (match.solo) {
        match.inningsLimit?.let { limit ->
            "inn ${match.innings1}/$limit"
        } ?: "inn ${match.innings1}"
    } else {
        match.inningsLimit?.let { limit ->
            "inn ${match.innings1}–${match.innings2}/$limit"
        } ?: "inn ${match.innings1}–${match.innings2}"
    }
    val mode = if (match.solo) "14/1 solo" else "14/1"
    return "$mode  ·  ${match.pointsToWin} pts  ·  $inningsLabel"
}

/** Completed visits for this seat; with a limit, show `Inn 29/30` (not the next visit). */
fun fourteenOneInningsLine(innings: Int, limit: Int?): String =
    limit?.let { "Inn $innings/$it" } ?: "Inn $innings"

@Composable
private fun FourteenOnePlayerPanel(
    match: Match,
    player: Player,
    score: Int,
    innings: Int,
    fouls: Int,
    highRun: Int,
    hasHand: Boolean,
    handTowardEnd: Boolean,
    onAddPoints: (PlayerId, Int) -> Unit,
    onRequestPass: () -> Unit,
    onRequestFoul: () -> Unit,
    onBreakFoul: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = match.status == MatchStatus.IN_PROGRESS && hasHand
    val showBreakFoul = enabled && match.awaitingOpeningBreak
    val clearRackPoints = (match.objectBallsOnTable - 1).coerceAtLeast(1)

    BoxWithConstraints(modifier = modifier) {
        val metrics = BoardMetrics.fromPane(maxWidth, maxHeight)
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
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = metrics.nameSp),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(metrics.nameToScoreGap))

            FourteenOneScoreCluster(
                match = match,
                score = score,
                innings = innings,
                fouls = fouls,
                highRun = highRun,
                hasHand = hasHand,
                handTowardEnd = handTowardEnd,
                metrics = metrics,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(metrics.scoreToActionsGap))

            FourteenOneActionButtons(
                enabled = enabled,
                showBreakFoul = showBreakFoul,
                clearRackPoints = clearRackPoints,
                actionHeight = metrics.actionHeight,
                actionGap = metrics.actionGap,
                onAddPoints = { onAddPoints(player.id, clearRackPoints) },
                onRequestPass = onRequestPass,
                onRequestFoul = onRequestFoul,
                onBreakFoul = { onBreakFoul(player.id) },
            )
        }
    }
}

@Composable
private fun FourteenOneScoreCluster(
    match: Match,
    score: Int,
    innings: Int,
    fouls: Int,
    highRun: Int,
    hasHand: Boolean,
    handTowardEnd: Boolean,
    metrics: BoardMetrics,
    modifier: Modifier = Modifier,
) {
    val foulWarn = fouls == FourteenOneEngine.CONSECUTIVE_FOULS_TO_PENALTY - 1
    val felt = LocalFeltPalette.current
    val handAlpha by animateFloatAsState(
        targetValue = if (hasHand) 1f else 0f,
        label = "hand-alpha",
    )
    val warnAlpha by animateFloatAsState(
        targetValue = if (foulWarn && hasHand) 1f else 0f,
        label = "fourteen-foul-warn",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = score,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "fourteen-score",
                ) { value ->
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = metrics.scoreSp),
                    )
                }
                CueBallBreakIndicator(
                    modifier = Modifier
                        .align(if (handTowardEnd) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(
                            if (handTowardEnd) {
                                PaddingValues(end = metrics.cueInset)
                            } else {
                                PaddingValues(start = metrics.cueInset)
                            },
                        )
                        .alpha(handAlpha),
                    size = metrics.cueBallSize,
                )
            }
            Text(
                text = fourteenOneInningsLine(
                    innings = innings,
                    limit = match.inningsLimit,
                ),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = metrics.visitStatSp),
                color = ScoreWhite.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = metrics.nameToScoreGap),
            )
            Text(
                text = "HR $highRun  ·  Foul $fouls/3",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = metrics.visitStatSp),
                color = ScoreWhite.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (hasHand) {
                FourteenOneVisitStats(
                    objectBallsOnTable = match.objectBallsOnTable,
                    currentRun = match.currentRun,
                    visitStatSize = metrics.visitStatSp,
                    accent = felt.accentLight,
                    modifier = Modifier.padding(top = metrics.nameToScoreGap),
                )
            }
            if (match.awaitingOpeningBreak && hasHand) {
                Text(
                    text = "OPENING BREAK",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = metrics.warnSp),
                    color = felt.accentLight,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (foulWarn && hasHand) {
                Text(
                    text = "NEXT FOUL = −15",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = metrics.warnSp),
                    color = ButtonFoulLight.copy(alpha = warnAlpha),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .alpha(warnAlpha),
                )
            }
        }
    }
}

@Composable
private fun FourteenOneVisitStats(
    objectBallsOnTable: Int,
    currentRun: Int,
    visitStatSize: TextUnit,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "On Table : $objectBallsOnTable",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = visitStatSize),
            color = accent,
        )
        if (currentRun > 0) {
            Text(
                text = "  ·  ",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = visitStatSize),
                color = ScoreWhite.copy(alpha = 0.45f),
            )
            Text(
                text = "RUN $currentRun",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = visitStatSize),
                color = ButtonRunOutLight,
            )
        }
    }
}

@Composable
private fun FourteenOneActionButtons(
    enabled: Boolean,
    showBreakFoul: Boolean,
    clearRackPoints: Int,
    actionHeight: Dp,
    actionGap: Dp,
    onAddPoints: () -> Unit,
    onRequestPass: () -> Unit,
    onRequestFoul: () -> Unit,
    onBreakFoul: () -> Unit,
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
                label = "+$clearRackPoints",
                base = ButtonPlus,
                light = ButtonPlusLight,
                dark = ButtonPlusDark,
                enabled = enabled,
                onClick = onAddPoints,
                modifier = Modifier.weight(1f),
                height = actionHeight,
            )
            TexturedActionButton(
                label = "PASS",
                base = ButtonRunOut,
                light = ButtonRunOutLight,
                dark = ButtonRunOutDark,
                enabled = enabled,
                onClick = onRequestPass,
                modifier = Modifier.weight(1f),
                height = actionHeight,
            )
            TexturedActionButton(
                label = "FOUL",
                base = ButtonFoul,
                light = ButtonFoulLight,
                dark = ButtonFoulDark,
                enabled = enabled,
                onClick = onRequestFoul,
                modifier = Modifier.weight(1f),
                height = actionHeight,
            )
        }
        if (showBreakFoul) {
            TexturedActionButton(
                label = "BREAK −2",
                base = ButtonFoul,
                light = ButtonFoulLight,
                dark = ButtonFoulDark,
                enabled = true,
                onClick = onBreakFoul,
                modifier = Modifier.fillMaxWidth(),
                height = actionHeight,
            )
            Text(
                text = "Opening break",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun VisitEndBallsModal(
    match: Match,
    action: VisitEndAction,
    onDismiss: () -> Unit,
    onConfirm: (remaining: Int, priorPoints: Int) -> Unit,
) {
    val felt = LocalFeltPalette.current
    val minBalls = FourteenOneEngine.MIN_OBJECT_BALLS_REMAINING
    val maxBalls = Match.OBJECT_BALLS_FULL_RACK
    val tableAtOpen = match.objectBallsOnTable.coerceIn(minBalls, maxBalls)
    var remaining by remember(match.objectBallsOnTable, action) {
        mutableIntStateOf(tableAtOpen)
    }
    var priorPoints by remember(match.objectBallsOnTable, action) { mutableIntStateOf(0) }
    var draftBalls by remember(match.objectBallsOnTable, action) { mutableIntStateOf(tableAtOpen) }
    var modalRacks by remember(match.objectBallsOnTable, action) { mutableIntStateOf(0) }
    val syncPoints = remember(draftBalls, remaining) {
        FourteenOneEngine.pointsFromTableToRemaining(draftBalls, remaining)
    }
    val impliesAutoRerack = remaining > draftBalls
    val boardRacks = remember(match.history, match.currentShooterId, match.currentRun) {
        fullRacksInCurrentVisit(match)
    }
    val boardRemainder = (match.currentRun - boardRacks * POINTS_FOURTEEN).coerceAtLeast(0)
    val modalClearPoints = (priorPoints - modalRacks * POINTS_FOURTEEN).coerceAtLeast(0)
    val totalRacks = boardRacks + modalRacks
    val rackPoints = totalRacks * POINTS_FOURTEEN
    val partialPoints = boardRemainder + modalClearPoints + syncPoints
    val visitTotal = match.currentRun + priorPoints + syncPoints
    val racksLabel = if (totalRacks <= 1) "$totalRacks rack" else "$totalRacks racks"
    val title = when (action) {
        VisitEndAction.PASS -> "END INNING — PASS"
        VisitEndAction.FOUL -> "END INNING — FOUL"
    }
    val confirmBase = when (action) {
        VisitEndAction.PASS -> ButtonRunOut
        VisitEndAction.FOUL -> ButtonFoul
    }
    val confirmLight = when (action) {
        VisitEndAction.PASS -> ButtonRunOutLight
        VisitEndAction.FOUL -> ButtonFoulLight
    }
    val confirmDark = when (action) {
        VisitEndAction.PASS -> ButtonRunOutDark
        VisitEndAction.FOUL -> ButtonFoulDark
    }

    fun tapPlusFourteen() {
        // Match board clear-to-re-rack if needed, then one full continuous rack from 15.
        if (draftBalls != maxBalls) {
            priorPoints += draftBalls - 1
            draftBalls = maxBalls
        }
        priorPoints += POINTS_FOURTEEN
        modalRacks += 1
        draftBalls = maxBalls
    }

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
        // Cap height so landscape (and short portrait) keep CANCEL/CONFIRM on-screen when
        // the auto re-rack warning (remaining > On Table) adds extra lines.
        val panelMaxHeight = maxHeight * MODAL_MAX_HEIGHT_FRACTION
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(MODAL_CONTENT_WIDTH_FRACTION)
                .heightIn(max = panelMaxHeight)
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
            val bodyScroll = rememberScrollState()
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(bodyScroll),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ScoreWhite.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Balls left on table",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ScoreWhite.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    StepperButton(
                        label = "−",
                        enabled = remaining > minBalls,
                        onClick = { remaining -= 1 },
                    )
                    Text(
                        text = remaining.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                        color = ScoreWhite,
                        modifier = Modifier.widthIn(min = 96.dp),
                        textAlign = TextAlign.Center,
                    )
                    StepperButton(
                        label = "+",
                        enabled = remaining < maxBalls,
                        onClick = { remaining += 1 },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                TexturedActionButton(
                    label = "+14",
                    base = ButtonPlus,
                    light = ButtonPlusLight,
                    dark = ButtonPlusDark,
                    enabled = true,
                    onClick = { tapPlusFourteen() },
                    modifier = Modifier.fillMaxWidth(MODAL_PLUS_FOURTEEN_WIDTH_FRACTION),
                    height = 44.dp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Full continuous rack (use for racks missed on the board)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ScoreWhite.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$racksLabel (+$rackPoints)  ·  +$partialPoints partial  ·  Visit $visitTotal",
                    style = MaterialTheme.typography.titleLarge,
                    color = ButtonRunOutLight,
                    textAlign = TextAlign.Center,
                )
                if (impliesAutoRerack) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "On Table $draftBalls → $remaining: at most one re-rack. Tap +14 for each extra full rack.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OutlineWarm,
                        textAlign = TextAlign.Center,
                    )
                }
                if (action == VisitEndAction.FOUL) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Then foul −1",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ButtonFoulLight,
                    )
                }
                }
                ScrollMoreHint(
                    scrollState = bodyScroll,
                    fadeColor = felt.vignette,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TexturedActionButton(
                    label = "CANCEL",
                    base = felt.mid,
                    light = felt.light,
                    dark = felt.dark,
                    enabled = true,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
                TexturedActionButton(
                    label = "CONFIRM",
                    base = confirmBase,
                    light = confirmLight,
                    dark = confirmDark,
                    enabled = true,
                    onClick = { onConfirm(remaining, priorPoints) },
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
            }
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val felt = LocalFeltPalette.current
    val performHaptic = rememberClickHaptic()
    val interaction = remember { MutableInteractionSource() }
    val alpha = if (enabled) 1f else 0.38f
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(72.dp, 64.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        felt.accentLight.copy(alpha = alpha),
                        felt.accentDark.copy(alpha = alpha),
                    ),
                ),
            )
            .border(
                width = 2.dp,
                color = OutlineWarm.copy(alpha = if (enabled) 0.85f else 0.28f),
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = {
                    performHaptic()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineLarge,
            color = ScoreWhite.copy(alpha = alpha),
        )
    }
}

@Composable
private fun FourteenMedianDivider(landscape: Boolean) {
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
                .padding(horizontal = 12.dp)
                .height(3.dp)
                .background(brush)
        },
    )
}

private val PortraitMedianInset = 16.dp
private val PortraitNameTopInset = 8.dp
private const val MODAL_CONTENT_WIDTH_FRACTION = 0.92f
private const val MODAL_MAX_HEIGHT_FRACTION = 0.92f
private const val MODAL_PLUS_FOURTEEN_WIDTH_FRACTION = 0.55f
private const val POINTS_FOURTEEN = 14

/** Full continuous racks (+14) already scored in the current unfinished visit. */
private fun fullRacksInCurrentVisit(match: Match): Int {
    var racks = 0
    for (event in match.history.asReversed()) {
        when (event.type) {
            MatchEventType.POINTS ->
                if (event.playerId == match.currentShooterId) {
                    racks += event.value / POINTS_FOURTEEN
                } else {
                    return racks
                }
            MatchEventType.PASS,
            MatchEventType.FOUL,
            MatchEventType.BREAK_FOUL,
            MatchEventType.ACCEPT_ILLEGAL_OPEN,
            MatchEventType.THREE_FOUL_PENALTY,
            -> return racks
            else -> Unit
        }
    }
    return racks
}
