package com.racktrack.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.data.StoredMatch
import com.racktrack.domain.model.GameMode
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.formatDuration
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite
import com.racktrack.presentation.viewmodel.HistoryUiState
import java.text.DateFormat
import java.util.Date

private val DeleteCrossRed = Color(0xFFE05252)

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onPlayerFilter1Change: (String) -> Unit,
    onPlayerFilter2Change: (String) -> Unit,
    onOpenMatch: (String) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val felt = LocalFeltPalette.current
    var pendingDelete by remember { mutableStateOf<StoredMatch?>(null) }

    FeltBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "HISTORY",
                style = MaterialTheme.typography.headlineLarge,
                color = ScoreWhite,
            )
            Text(
                text = modeLabel(state.gameMode),
                style = MaterialTheme.typography.titleLarge,
                color = OutlineWarm,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HistoryFilterField(
                    label = "Player 1",
                    value = state.playerFilter1,
                    onValueChange = onPlayerFilter1Change,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
                HistoryFilterField(
                    label = "Player 2",
                    value = state.playerFilter2,
                    onValueChange = onPlayerFilter2Change,
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Showing ${modeLabel(state.gameMode)} only · name filters match either seat.",
                style = MaterialTheme.typography.labelLarge,
                color = ScoreWhite.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
            )

            if (state.matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No ${modeLabel(state.gameMode)} matches yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = ScoreWhite.copy(alpha = 0.65f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.matches, key = { it.id }) { match ->
                        HistoryMatchRow(
                            match = match,
                            onClick = { onOpenMatch(match.id) },
                            onDelete = { pendingDelete = match },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TexturedActionButton(
                label = "BACK",
                base = felt.accent,
                light = felt.accentLight,
                dark = felt.accentDark,
                enabled = true,
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(min = 200.dp),
                height = 52.dp,
            )
        }

        pendingDelete?.let { match ->
            DeleteHistoryConfirmDialog(
                match = match,
                onConfirm = {
                    onDeleteMatch(match.id)
                    pendingDelete = null
                },
                onDismiss = { pendingDelete = null },
            )
        }
    }
}

@Composable
private fun DeleteHistoryConfirmDialog(
    match: StoredMatch,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val summary = match.summary
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete match?")
        },
        text = {
            Text(
                "${summary.player1Name}  ${summary.score1} – ${summary.score2}  ${summary.player2Name}\n" +
                    "This cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("DELETE", color = DeleteCrossRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
    )
}

@Composable
fun HistoryDetailScreen(
    match: StoredMatch?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val felt = LocalFeltPalette.current
    FeltBackground(modifier = modifier) {
        if (match == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                contentAlignment = Alignment.Center,
            ) {
                TexturedActionButton(
                    label = "BACK",
                    base = felt.accent,
                    light = felt.accentLight,
                    dark = felt.accentDark,
                    enabled = true,
                    onClick = onBack,
                    modifier = Modifier.widthIn(min = 200.dp),
                    height = 52.dp,
                )
            }
        } else {
            MatchSummaryScaffold(
                title = "MATCH STATS",
                summary = match.summary,
                actionLabel = "BACK",
                onAction = onBack,
                modifier = Modifier.fillMaxSize(),
                scrim = false,
                actionUsesFelt = true,
            )
        }
    }
}

@Composable
private fun HistoryFilterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ScoreWhite,
            unfocusedTextColor = ScoreWhite,
            focusedBorderColor = OutlineWarm,
            unfocusedBorderColor = OutlineWarm.copy(alpha = 0.55f),
            focusedLabelColor = OutlineWarm,
            unfocusedLabelColor = ScoreWhite.copy(alpha = 0.65f),
            cursorColor = OutlineWarm,
        ),
        modifier = modifier,
    )
}

@Composable
private fun HistoryMatchRow(
    match: StoredMatch,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val felt = LocalFeltPalette.current
    val summary = match.summary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(felt.dark.copy(alpha = 0.72f))
            .border(1.dp, OutlineWarm.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(top = 2.dp, bottom = 2.dp, end = 8.dp),
        ) {
            Text(
                text = modeLabel(summary.gameMode),
                style = MaterialTheme.typography.labelLarge,
                color = OutlineWarm,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${summary.player1Name}  ${summary.score1}  –  ${summary.score2}  ${summary.player2Name}",
                style = MaterialTheme.typography.titleLarge,
                color = ScoreWhite,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    append(
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(Date(match.completedAtMillis)),
                    )
                    append("  ·  ")
                    append(formatDuration(summary.totalDurationMillis))
                    if (summary.winnerName.isNotEmpty()) {
                        append("  ·  ")
                        append(summary.winnerName)
                        append(" wins")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = ScoreWhite.copy(alpha = 0.7f),
            )
        }
        Text(
            text = "✕",
            color = DeleteCrossRed,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onDelete)
                .padding(top = 6.dp),
        )
    }
}

private fun modeLabel(mode: GameMode): String =
    when (mode) {
        GameMode.EIGHT_BALL -> "8-BALL"
        GameMode.NINE_BALL -> "9-BALL"
        GameMode.TEN_BALL -> "10-BALL"
        GameMode.FOURTEEN_ONE -> "14/1"
    }
