package com.racktrack.presentation.screen

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.domain.FourteenOneEngine
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.Player
import com.racktrack.domain.model.PlayerId
import com.racktrack.presentation.component.CueBallBreakIndicator
import com.racktrack.presentation.component.TexturedActionButton
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

@Composable
fun FourteenOneBoardContent(
    match: Match,
    landscape: Boolean,
    onAddPoints: (PlayerId, Int) -> Unit,
    onPass: (PlayerId) -> Unit,
    onFoul: (PlayerId) -> Unit,
    onBreakFoul: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (landscape) {
        Row(modifier = modifier) {
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
                onPass = onPass,
                onFoul = onFoul,
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
                onPass = onPass,
                onFoul = onFoul,
                onBreakFoul = onBreakFoul,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    } else {
        Column(modifier = modifier) {
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
                onPass = onPass,
                onFoul = onFoul,
                onBreakFoul = onBreakFoul,
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
                onPass = onPass,
                onFoul = onFoul,
                onBreakFoul = onBreakFoul,
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )
        }
    }
}

fun fourteenOneHeader(match: Match): String {
    val inningsLabel = match.inningsLimit?.let { limit ->
        val shown = maxOf(match.innings1, match.innings2)
        "inning $shown/$limit"
    } ?: "inning ${maxOf(match.innings1, match.innings2)}"
    return "14/1  ·  ${match.pointsToWin} pts  ·  $inningsLabel"
}

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
    onPass: (PlayerId) -> Unit,
    onFoul: (PlayerId) -> Unit,
    onBreakFoul: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val enabled = match.status == MatchStatus.IN_PROGRESS && hasHand
    val scoreSize = if (compact) 64.sp else 76.sp
    val actionHeight = if (compact) 44.dp else 46.dp
    val cueSize = if (compact) 44.dp else 48.dp
    val visitStatSize = if (compact) 22.sp else 26.sp
    val showBreakFoul = enabled && match.awaitingOpeningBreak
    val foulWarn = fouls == FourteenOneEngine.CONSECUTIVE_FOULS_TO_PENALTY - 1
    val handAlpha by animateFloatAsState(
        targetValue = if (hasHand) 1f else 0f,
        label = "hand-alpha",
    )
    val warnAlpha by animateFloatAsState(
        targetValue = if (foulWarn && hasHand) 1f else 0f,
        label = "fourteen-foul-warn",
    )

    Column(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = player.name.uppercase(),
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineLarge
            },
            textAlign = TextAlign.Center,
        )

        // Score can shrink; visit stats stay pinned above the action buttons (no landscape crop).
        Box(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
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
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = scoreSize),
                    )
                }
                CueBallBreakIndicator(
                    modifier = Modifier
                        .align(if (handTowardEnd) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(
                            if (handTowardEnd) {
                                PaddingValues(end = 28.dp)
                            } else {
                                PaddingValues(start = 28.dp)
                            },
                        )
                        .alpha(handAlpha),
                    size = cueSize,
                )
            }
        }

        Text(
            text = "HR $highRun  ·  Inn $innings  ·  Foul $fouls/3",
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )

        // Always reserve one line so the active shooter’s TABLE / RUN never jump the layout.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(if (compact) 28.dp else 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (hasHand) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "On Table : ${match.objectBallsOnTable}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = visitStatSize),
                        color = ScoreWhite,
                    )
                    if (match.currentRun > 0) {
                        Text(
                            text = "  ·  ",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = visitStatSize),
                            color = ScoreWhite.copy(alpha = 0.45f),
                        )
                        Text(
                            text = "RUN ${match.currentRun}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = visitStatSize),
                            color = ButtonRunOutLight,
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .height(if (foulWarn && hasHand) 28.dp else 0.dp)
                .padding(top = if (foulWarn && hasHand) 4.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "NEXT FOUL = −15",
                style = MaterialTheme.typography.titleLarge,
                color = ButtonFoulLight.copy(alpha = warnAlpha),
                modifier = Modifier.alpha(warnAlpha),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    onClick = { onAddPoints(player.id, 1) },
                    modifier = Modifier.weight(1f),
                    height = actionHeight,
                )
                TexturedActionButton(
                    label = "+5",
                    base = ButtonPlus,
                    light = ButtonPlusLight,
                    dark = ButtonPlusDark,
                    enabled = enabled,
                    onClick = { onAddPoints(player.id, 5) },
                    modifier = Modifier.weight(1f),
                    height = actionHeight,
                )
                TexturedActionButton(
                    label = "+14",
                    base = ButtonPlus,
                    light = ButtonPlusLight,
                    dark = ButtonPlusDark,
                    enabled = enabled,
                    onClick = { onAddPoints(player.id, POINTS_FOURTEEN) },
                    modifier = Modifier.weight(1f),
                    height = actionHeight,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TexturedActionButton(
                    label = "PASS",
                    base = ButtonRunOut,
                    light = ButtonRunOutLight,
                    dark = ButtonRunOutDark,
                    enabled = enabled,
                    onClick = { onPass(player.id) },
                    modifier = Modifier.weight(1f),
                    height = actionHeight,
                )
                TexturedActionButton(
                    label = "FOUL",
                    base = ButtonFoul,
                    light = ButtonFoulLight,
                    dark = ButtonFoulDark,
                    enabled = enabled,
                    onClick = { onFoul(player.id) },
                    modifier = Modifier.weight(1f),
                    height = actionHeight,
                )
                TexturedActionButton(
                    label = "BREAK −2",
                    base = ButtonFoul,
                    light = ButtonFoulLight,
                    dark = ButtonFoulDark,
                    enabled = showBreakFoul,
                    onClick = { onBreakFoul(player.id) },
                    modifier = Modifier.weight(1f),
                    height = actionHeight,
                )
            }
            if (match.awaitingOpeningBreak && hasHand) {
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
                .height(3.dp)
                .padding(horizontal = 12.dp)
                .background(brush)
        },
    )
}

private const val POINTS_FOURTEEN = 14
