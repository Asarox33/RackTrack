package com.racktrack.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.racktrack.domain.MatchSummaryReport
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import com.racktrack.presentation.component.FoulIcon
import com.racktrack.presentation.component.RunOutIcon
import com.racktrack.presentation.component.ScrollMoreHint
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
        title = MatchSummaryReport.sessionOverTitle(summary),
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
    val scrollState = rememberScrollState()
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

    val panelBody: @Composable () -> Unit = {
        Box(modifier = panel) {
            MatchSummaryContent(
                title = title,
                summary = summary,
                actionLabel = actionLabel,
                onAction = onAction,
                actionUsesFelt = actionUsesFelt,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
            ScrollMoreHint(
                scrollState = scrollState,
                fadeColor = felt.vignette,
            )
        }
    }

    if (scrim) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            panelBody()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            panelBody()
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
    var saving by remember { mutableStateOf(false) }
    val busy = sharing || saving
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri: Uri? ->
        if (uri == null) {
            saving = false
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            runCatching {
                MatchSummaryShare.savePdfToUri(
                    context = context,
                    summary = summary,
                    destination = uri,
                    title = title,
                    accentArgb = felt.accent.toArgb(),
                )
            }
            saving = false
        }
    }

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
            text = if (summary.solo) {
                summary.player1Name.uppercase().ifEmpty { "PLAYER" }
            } else {
                summary.winnerName.uppercase().ifEmpty { "DRAW" }
            },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = WINNER_FONT_SP.sp),
            color = ButtonRunOutLight,
            textAlign = TextAlign.Center,
        )
        when {
            summary.solo -> Text(
                text = "SOLO",
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
            )
            summary.winnerName.isNotEmpty() -> Text(
                text = "WINS",
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
            )
        }
        Text(
            text = MatchSummaryReport.subtitle(summary),
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
                pushOuts = summary.pushOuts1,
                eightBallLosses = summary.eightBallLosses1,
                highRun = summary.highRun1,
                average = summary.average1,
                innings = summary.innings1,
                gameMode = summary.gameMode,
                modifier = Modifier.weight(1f),
            )
            if (!summary.solo) {
                PlayerSummaryColumn(
                    name = summary.player2Name,
                    fouls = summary.totalFouls2,
                    runOuts = summary.runOuts2,
                    goldenBreaks = summary.goldenBreaks2,
                    dryBreaks = summary.dryBreaks2,
                    pushOuts = summary.pushOuts2,
                    eightBallLosses = summary.eightBallLosses2,
                    highRun = summary.highRun2,
                    average = summary.average2,
                    innings = summary.innings2,
                    gameMode = summary.gameMode,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (summary.gameMode.isPointScoring) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "INNINGS",
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
                solo = summary.solo,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 240.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TexturedActionButton(
                label = if (sharing) "SHARING…" else "SHARE",
                base = felt.accent,
                light = felt.accentLight,
                dark = felt.accentDark,
                enabled = !busy,
                onClick = {
                    if (busy) return@TexturedActionButton
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
                modifier = Modifier.weight(1f),
                height = 52.dp,
            )
            TexturedActionButton(
                label = if (saving) "SAVING…" else "SAVE",
                base = felt.accent,
                light = felt.accentLight,
                dark = felt.accentDark,
                enabled = !busy,
                onClick = {
                    if (busy) return@TexturedActionButton
                    saving = true
                    savePdfLauncher.launch(MatchSummaryShare.suggestedFileName(summary))
                },
                modifier = Modifier.weight(1f),
                height = 52.dp,
            )
        }
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
    MatchSummaryReport.subtitle(summary)

@Composable
private fun PlayerSummaryColumn(
    name: String,
    fouls: Int,
    runOuts: Int,
    goldenBreaks: Int,
    dryBreaks: Int,
    pushOuts: Int,
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
            // Split HR / avg so wide averages (e.g. 10.00) never wrap the shared line and
            // skew the two player cards on narrow panes.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "HR $highRun",
                    style = MaterialTheme.typography.labelLarge,
                    color = ButtonRunOutLight,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = "avg ${"%.2f".format(average)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ButtonRunOutLight,
                    maxLines = 1,
                    softWrap = false,
                )
            }
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
            val showExtraRaceStats =
                gameMode.supportsGoldenBreak ||
                    gameMode.supportsDryBreak ||
                    gameMode.supportsEightBallLoss ||
                    gameMode.supportsPushOut
            if (showExtraRaceStats) {
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
                    if (gameMode.supportsPushOut) {
                        Text(
                            text = "PO $pushOuts",
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
    solo: Boolean = false,
) {
    if (solo) {
        SoloFourteenOneInningsTable(playerName = player1Name, innings = innings1)
        return
    }
    val rows = MatchSummaryReport.pairedInningRows(innings1, innings2)
    if (rows.isEmpty()) {
        Text(
            text = "No innings recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }
    val endWidth = 40.dp
    val indexWidth = 28.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(indexWidth))
            Text(
                text = player1Name,
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(3f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = player2Name,
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(3f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.width(indexWidth),
            )
            Text(
                text = "End",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.width(endWidth),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Pts",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tot",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tot",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Pts",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "End",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.width(endWidth),
                textAlign = TextAlign.Center,
            )
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${row.index}",
                    style = MaterialTheme.typography.labelLarge,
                    color = OutlineWarm,
                    modifier = Modifier.width(indexWidth),
                )
                Text(
                    text = row.player1?.let { MatchSummaryReport.inningEndLabel(it.endType) } ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    color = ScoreWhite.copy(alpha = 0.55f),
                    modifier = Modifier.width(endWidth),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = row.player1?.points?.toString() ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = row.total1?.toString() ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = ButtonDryLight,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = row.total2?.toString() ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = ButtonDryLight,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = row.player2?.points?.toString() ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = row.player2?.let { MatchSummaryReport.inningEndLabel(it.endType) } ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    color = ScoreWhite.copy(alpha = 0.55f),
                    modifier = Modifier.width(endWidth),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SoloFourteenOneInningsTable(
    playerName: String,
    innings: List<InningStat>,
) {
    val rows = MatchSummaryReport.soloInningRows(innings)
    if (rows.isEmpty()) {
        Text(
            text = "No innings recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }
    val endWidth = 44.dp
    val indexWidth = 36.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.labelLarge,
            color = OutlineWarm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.width(indexWidth),
            )
            Text(
                text = "End",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.width(endWidth),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Pts",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tot",
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${row.inning.index}",
                    style = MaterialTheme.typography.labelLarge,
                    color = OutlineWarm,
                    modifier = Modifier.width(indexWidth),
                )
                Text(
                    text = MatchSummaryReport.inningEndLabel(row.inning.endType),
                    style = MaterialTheme.typography.labelLarge,
                    color = ScoreWhite.copy(alpha = 0.55f),
                    modifier = Modifier.width(endWidth),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = row.inning.points.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = row.total.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = ButtonDryLight,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
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
        MatchEventType.ACCEPT_ILLEGAL_OPEN,
        MatchEventType.THREE_FOUL_PENALTY,
        MatchEventType.PUSH_OUT,
        MatchEventType.PUSH_OUT_CLEAN,
        MatchEventType.PUSH_OUT_FOUL,
        MatchEventType.PUSH_OUT_TAKE,
        MatchEventType.PUSH_OUT_RETURN,
        -> ""
    }

private const val SCRIM_ALPHA = 0.55f
private const val MODAL_WIDTH_FRACTION = 0.92f
private const val MODAL_HEIGHT_FRACTION = 0.92f
private const val PANEL_TOP_ALPHA = 0.97f
private const val PANEL_BORDER_ALPHA = 0.55f
private const val WINNER_FONT_SP = 48
