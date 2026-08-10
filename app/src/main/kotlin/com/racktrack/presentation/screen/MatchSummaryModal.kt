package com.racktrack.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.domain.InningStat
import com.racktrack.domain.MatchSummary
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import com.racktrack.presentation.component.FoulIcon
import com.racktrack.presentation.component.RunOutIcon
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.formatDuration
import com.racktrack.presentation.share.MatchSummaryShare
import com.racktrack.presentation.theme.ButtonDryLight
import com.racktrack.presentation.theme.ButtonFoulLight
import com.racktrack.presentation.theme.ButtonGoldenLight
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.ButtonRunOutDark
import com.racktrack.presentation.theme.ButtonRunOutLight
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite
import kotlinx.coroutines.launch

@Composable
fun MatchSummaryModal(
    summary: MatchSummary,
    onNewMatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MatchSummaryScaffold(
        title = "MATCH OVER",
        summary = summary,
        actionLabel = "BACK",
        onAction = onNewMatch,
        modifier = modifier,
        scrim = true,
        actionUsesFelt = true,
    )
}

@Composable
fun MatchSummaryScaffold(
    title: String,
    summary: MatchSummary,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    scrim: Boolean = false,
    actionUsesFelt: Boolean = false,
) {
    val felt = LocalFeltPalette.current
    val panel = Modifier
        .widthIn(max = 720.dp)
        .fillMaxWidth(if (scrim) MODAL_WIDTH_FRACTION else 1f)
        .then(
            if (scrim) Modifier.fillMaxHeight(MODAL_HEIGHT_FRACTION) else Modifier.fillMaxHeight(),
        )
        .clip(RoundedCornerShape(22.dp))
        .background(
            Brush.verticalGradient(
                listOf(felt.dark.copy(alpha = PANEL_TOP_ALPHA), felt.vignette),
            ),
        )
        .border(2.dp, OutlineWarm.copy(alpha = PANEL_BORDER_ALPHA), RoundedCornerShape(22.dp))
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 16.dp)

    if (scrim) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            MatchSummaryContent(
                title = title,
                summary = summary,
                actionLabel = actionLabel,
                onAction = onAction,
                actionUsesFelt = actionUsesFelt,
                modifier = panel,
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            MatchSummaryContent(
                title = title,
                summary = summary,
                actionLabel = actionLabel,
                onAction = onAction,
                actionUsesFelt = actionUsesFelt,
                modifier = panel,
            )
        }
    }
}

@Composable
private fun MatchSummaryContent(
    title: String,
    summary: MatchSummary,
    actionLabel: String,
    onAction: () -> Unit,
    actionUsesFelt: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val felt = LocalFeltPalette.current
    var sharing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ScoreWhite.copy(alpha = 0.7f),
        )
        Text(
            text = summary.winnerName.uppercase().ifEmpty { "DRAW" },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = WINNER_FONT_SP.sp),
            color = ButtonRunOutLight,
            textAlign = TextAlign.Center,
        )
        if (summary.winnerName.isNotEmpty()) {
            Text(
                text = "WINS",
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
            )
        }
        Text(
            text = summarySubtitle(summary),
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.8f),
        )
        Text(
            text = "Total  ${formatDuration(summary.totalDurationMillis)}",
            style = MaterialTheme.typography.titleLarge,
            color = OutlineWarm,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerSummaryColumn(
                name = summary.player1Name,
                fouls = summary.totalFouls1,
                runOuts = summary.runOuts1,
                goldenBreaks = summary.goldenBreaks1,
                dryBreaks = summary.dryBreaks1,
                eightBallLosses = summary.eightBallLosses1,
                highRun = summary.highRun1,
                average = summary.average1,
                innings = summary.innings1,
                gameMode = summary.gameMode,
                modifier = Modifier.weight(1f),
            )
            PlayerSummaryColumn(
                name = summary.player2Name,
                fouls = summary.totalFouls2,
                runOuts = summary.runOuts2,
                goldenBreaks = summary.goldenBreaks2,
                dryBreaks = summary.dryBreaks2,
                eightBallLosses = summary.eightBallLosses2,
                highRun = summary.highRun2,
                average = summary.average2,
                innings = summary.innings2,
                gameMode = summary.gameMode,
                modifier = Modifier.weight(1f),
            )
        }

        if (summary.gameMode.isPointScoring) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "REPRISES",
                style = MaterialTheme.typography.titleLarge,
                color = ScoreWhite.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            FourteenOneInningsTable(
                player1Name = summary.player1Name,
                player2Name = summary.player2Name,
                innings1 = summary.inningScores1,
                innings2 = summary.inningScores2,
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "RACKS",
                style = MaterialTheme.typography.titleLarge,
                color = ScoreWhite.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (summary.racks.isEmpty()) {
                Text(
                    text = "No rack timings recorded",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ScoreWhite.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    summary.racks.forEach { rack ->
                        RackDurationRow(
                            index = rack.index,
                            winnerName = rack.winnerName,
                            durationLabel = formatDuration(rack.durationMillis),
                            endLabel = rackEndLabel(rack.endType),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TexturedActionButton(
            label = if (sharing) "SHARING…" else "SHARE PDF",
            base = felt.accent,
            light = felt.accentLight,
            dark = felt.accentDark,
            enabled = !sharing,
            onClick = {
                if (sharing) return@TexturedActionButton
                sharing = true
                scope.launch {
                    runCatching {
                        MatchSummaryShare.sharePdf(
                            context = context.applicationContext,
                            summary = summary,
                            title = title,
                            accentArgb = felt.accent.toArgb(),
                        )
                    }
                    sharing = false
                }
            },
            modifier = Modifier.widthIn(min = 240.dp),
            height = 52.dp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        TexturedActionButton(
            label = actionLabel,
            base = if (actionUsesFelt) felt.accent else ButtonRunOut,
            light = if (actionUsesFelt) felt.accentLight else ButtonRunOutLight,
            dark = if (actionUsesFelt) felt.accentDark else ButtonRunOutDark,
            enabled = true,
            onClick = onAction,
            modifier = Modifier.widthIn(min = 240.dp),
            height = 52.dp,
        )
    }
}

fun summarySubtitle(summary: MatchSummary): String =
    if (summary.gameMode.isPointScoring) {
        val innings = summary.inningsLimit?.let { "${summary.innings1}/${summary.innings2} inn · lim $it" }
            ?: "${summary.innings1}/${summary.innings2} inn"
        "14/1  ·  ${summary.score1}  –  ${summary.score2}  ·  ${summary.pointsToWin} pts  ·  $innings"
    } else {
        when (summary.gameMode) {
            GameMode.EIGHT_BALL -> "8-BALL"
            GameMode.NINE_BALL -> "9-BALL"
            GameMode.TEN_BALL -> "10-BALL"
            GameMode.FOURTEEN_ONE -> "14/1"
        } + "  ·  ${summary.score1}  –  ${summary.score2}  ·  race to ${summary.racksToWin}"
    }

@Composable
private fun PlayerSummaryColumn(
    name: String,
    fouls: Int,
    runOuts: Int,
    goldenBreaks: Int,
    dryBreaks: Int,
    eightBallLosses: Int,
    highRun: Int,
    average: Double,
    innings: Int,
    gameMode: GameMode,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, OutlineWarm.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (gameMode.isPointScoring) {
            Text(
                text = "HR $highRun  ·  avg ${"%.2f".format(average)}",
                style = MaterialTheme.typography.labelLarge,
                color = ButtonRunOutLight,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Inn $innings",
                style = MaterialTheme.typography.labelLarge,
                color = ScoreWhite.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FoulIcon()
                Text("$fouls", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RunOutIcon(gameMode = gameMode)
                    Text("$runOuts", style = MaterialTheme.typography.labelLarge)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FoulIcon()
                    Text("$fouls", style = MaterialTheme.typography.labelLarge)
                }
            }
            if (
                gameMode.supportsGoldenBreak ||
                gameMode.supportsDryBreak ||
                gameMode.supportsEightBallLoss
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (gameMode.supportsGoldenBreak) {
                        Text(
                            text = "G $goldenBreaks",
                            style = MaterialTheme.typography.labelLarge,
                            color = ButtonGoldenLight,
                        )
                    }
                    if (gameMode.supportsEightBallLoss) {
                        Text(
                            text = "Early 8 $eightBallLosses",
                            style = MaterialTheme.typography.labelLarge,
                            color = ButtonFoulLight,
                        )
                    }
                    if (gameMode.supportsDryBreak) {
                        Text(
                            text = "D $dryBreaks",
                            style = MaterialTheme.typography.labelLarge,
                            color = ButtonDryLight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FourteenOneInningsTable(
    player1Name: String,
    player2Name: String,
    innings1: List<InningStat>,
    innings2: List<InningStat>,
) {
    if (innings1.isEmpty() && innings2.isEmpty()) {
        Text(
            text = "No innings recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InningScoreColumn(
            name = player1Name,
            innings = innings1,
            modifier = Modifier.weight(1f),
        )
        InningScoreColumn(
            name = player2Name,
            innings = innings2,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InningScoreColumn(
    name: String,
    innings: List<InningStat>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = OutlineWarm,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        if (innings.isEmpty()) {
            Text(
                text = "—",
                style = MaterialTheme.typography.bodyLarge,
                color = ScoreWhite.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        } else {
            innings.forEach { inning ->
                InningScoreRow(inning = inning)
            }
        }
    }
}

@Composable
private fun InningScoreRow(inning: InningStat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#${inning.index}",
            style = MaterialTheme.typography.labelLarge,
            color = OutlineWarm,
            modifier = Modifier.width(36.dp),
        )
        Text(
            text = "${inning.points}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
        Text(
            text = inningEndLabel(inning.endType),
            style = MaterialTheme.typography.labelLarge,
            color = ScoreWhite.copy(alpha = 0.55f),
            modifier = Modifier
                .width(52.dp)
                .padding(start = 8.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RackDurationRow(
    index: Int,
    winnerName: String,
    durationLabel: String,
    endLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$index",
            style = MaterialTheme.typography.labelLarge,
            color = OutlineWarm,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = winnerName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = endLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.65f),
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = durationLabel,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private fun rackEndLabel(type: MatchEventType): String =
    when (type) {
        MatchEventType.PLUS_ONE -> "+1"
        MatchEventType.RUN_OUT -> "Run out"
        MatchEventType.GOLDEN_BREAK -> "Golden"
        MatchEventType.EIGHT_BALL_LOSS -> "Early 8"
        MatchEventType.THREE_FOULS_LOSS -> "3 fouls"
        MatchEventType.FOUL,
        MatchEventType.FOULS_CLEARED,
        MatchEventType.DRY_BREAK,
        MatchEventType.POINTS,
        MatchEventType.PASS,
        MatchEventType.BREAK_FOUL,
        MatchEventType.THREE_FOUL_PENALTY,
        -> ""
    }

private fun inningEndLabel(type: MatchEventType?): String =
    when (type) {
        MatchEventType.PASS -> "pass"
        MatchEventType.FOUL -> "foul"
        MatchEventType.BREAK_FOUL -> "brk"
        null -> "win"
        else -> ""
    }

private const val SCRIM_ALPHA = 0.55f
private const val MODAL_WIDTH_FRACTION = 0.92f
private const val MODAL_HEIGHT_FRACTION = 0.92f
private const val PANEL_TOP_ALPHA = 0.97f
private const val PANEL_BORDER_ALPHA = 0.55f
private const val WINNER_FONT_SP = 48
