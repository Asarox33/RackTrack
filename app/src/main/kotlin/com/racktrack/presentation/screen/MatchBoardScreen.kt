package com.racktrack.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.Player
import com.racktrack.domain.model.PlayerId
import com.racktrack.presentation.theme.BreakBadgeBg
import com.racktrack.presentation.theme.BreakBadgeFg
import com.racktrack.presentation.theme.ButtonFoul
import com.racktrack.presentation.theme.ButtonPlus
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.theme.ScoreWhite

@Composable
fun MatchBoardScreen(
    match: Match,
    onPlusOne: (PlayerId) -> Unit,
    onRunOut: (PlayerId) -> Unit,
    onFoul: (PlayerId) -> Unit,
    onUndo: () -> Unit,
    onNewMatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeltBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "RACE TO ${match.racksToWin}",
                    style = MaterialTheme.typography.titleLarge,
                )
                if (match.status == MatchStatus.COMPLETED) {
                    val winnerName = match.winner?.name ?: ""
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "·  $winnerName WINS",
                        style = MaterialTheme.typography.titleLarge,
                        color = ButtonRunOut,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                PlayerPanel(
                    player = match.player1,
                    score = match.score1,
                    fouls = match.foul1,
                    hasBreak = match.currentBreakerId == match.player1.id,
                    enabled = match.status == MatchStatus.IN_PROGRESS,
                    onPlusOne = { onPlusOne(match.player1.id) },
                    onRunOut = { onRunOut(match.player1.id) },
                    onFoul = { onFoul(match.player1.id) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .background(ScoreWhite.copy(alpha = 0.35f)),
                )

                PlayerPanel(
                    player = match.player2,
                    score = match.score2,
                    fouls = match.foul2,
                    hasBreak = match.currentBreakerId == match.player2.id,
                    enabled = match.status == MatchStatus.IN_PROGRESS,
                    onPlusOne = { onPlusOne(match.player2.id) },
                    onRunOut = { onRunOut(match.player2.id) },
                    onFoul = { onFoul(match.player2.id) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlineAction(
                    label = "UNDO",
                    onClick = onUndo,
                    enabled = match.history.isNotEmpty(),
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlineAction(
                    label = "NEW MATCH",
                    onClick = onNewMatch,
                    enabled = true,
                )
            }
        }
    }
}

@Composable
private fun PlayerPanel(
    player: Player,
    score: Int,
    fouls: Int,
    hasBreak: Boolean,
    enabled: Boolean,
    onPlusOne: () -> Unit,
    onRunOut: () -> Unit,
    onFoul: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = player.name.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hasBreak) {
                Box(
                    modifier = Modifier
                        .background(BreakBadgeBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "BREAK",
                        color = BreakBadgeFg,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 88.sp),
            )
            Text(
                text = if (fouls > 0) "RACKS  ·  FOULS $fouls" else "RACKS",
                style = MaterialTheme.typography.bodyLarge,
                color = ScoreWhite.copy(alpha = 0.85f),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BigActionButton("+1", ButtonPlus, enabled, onPlusOne)
            BigActionButton("RUN OUT", ButtonRunOut, enabled, onRunOut)
            BigActionButton("FOUL", ButtonFoul, enabled, onFoul)
        }
    }
}

@Composable
private fun BigActionButton(
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (enabled) color else color.copy(alpha = 0.35f), shape)
            .border(2.dp, ScoreWhite.copy(alpha = if (enabled) 0.9f else 0.3f), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun OutlineAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .height(40.dp)
            .border(
                2.dp,
                ScoreWhite.copy(alpha = if (enabled) 0.9f else 0.3f),
                shape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ScoreWhite.copy(alpha = if (enabled) 1f else 0.4f),
        )
    }
}

@Preview(widthDp = 820, heightDp = 380, showBackground = true)
@Composable
private fun MatchBoardPreview() {
    val match = Match.start("Alex", "Sam", 6, initialBreakerIsPlayer1 = true)
    RackTrackTheme {
        MatchBoardScreen(
            match = match,
            onPlusOne = {},
            onRunOut = {},
            onFoul = {},
            onUndo = {},
            onNewMatch = {},
        )
    }
}
