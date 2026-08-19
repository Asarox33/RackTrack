package com.racktrack.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.GameMode
import com.racktrack.presentation.MatchFormatOptions
import com.racktrack.presentation.component.IntStepperModal
import com.racktrack.presentation.component.SettingsGearButton
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.TexturedChip
import com.racktrack.presentation.component.TexturedSettingButton
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.theme.ScoreWhite
import com.racktrack.presentation.viewmodel.SetupUiState

@Composable
fun SetupScreen(
    state: SetupUiState,
    onPlayer1Change: (String) -> Unit,
    onPlayer2Change: (String) -> Unit,
    onGameModeChange: (GameMode) -> Unit,
    onSoloTrainingChange: (Boolean) -> Unit = {},
    onRacksChange: (Int) -> Unit,
    onPointsChange: (Int) -> Unit,
    onInningsChange: (Int?) -> Unit,
    onBreakerChange: (Boolean) -> Unit,
    onBreakRuleChange: (BreakRule) -> Unit = {},
    onStart: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    rootModifier: Modifier = Modifier,
) {
    val landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    FeltBackground(modifier = rootModifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (landscape) {
                LandscapeSetup(
                    state = state,
                    onPlayer1Change = onPlayer1Change,
                    onPlayer2Change = onPlayer2Change,
                    onGameModeChange = onGameModeChange,
                    onSoloTrainingChange = onSoloTrainingChange,
                    onRacksChange = onRacksChange,
                    onPointsChange = onPointsChange,
                    onInningsChange = onInningsChange,
                    onBreakerChange = onBreakerChange,
                    onBreakRuleChange = onBreakRuleChange,
                    onStart = onStart,
                    onOpenHistory = onOpenHistory,
                )
            } else {
                PortraitSetup(
                    state = state,
                    onPlayer1Change = onPlayer1Change,
                    onPlayer2Change = onPlayer2Change,
                    onGameModeChange = onGameModeChange,
                    onSoloTrainingChange = onSoloTrainingChange,
                    onRacksChange = onRacksChange,
                    onPointsChange = onPointsChange,
                    onInningsChange = onInningsChange,
                    onBreakerChange = onBreakerChange,
                    onBreakRuleChange = onBreakRuleChange,
                    onStart = onStart,
                    onOpenHistory = onOpenHistory,
                )
            }
            SettingsGearButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(top = 6.dp, end = 10.dp),
            )
        }
    }
}

@Composable
private fun LandscapeSetup(
    state: SetupUiState,
    onPlayer1Change: (String) -> Unit,
    onPlayer2Change: (String) -> Unit,
    onGameModeChange: (GameMode) -> Unit,
    onSoloTrainingChange: (Boolean) -> Unit,
    onRacksChange: (Int) -> Unit,
    onPointsChange: (Int) -> Unit,
    onInningsChange: (Int?) -> Unit,
    onBreakerChange: (Boolean) -> Unit,
    onBreakRuleChange: (BreakRule) -> Unit,
    onStart: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val felt = LocalFeltPalette.current
    val solo = state.gameMode.isPointScoring && state.soloTraining
    Row(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("RackTrack", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "American pool",
                style = MaterialTheme.typography.bodyLarge,
                color = ScoreWhite.copy(alpha = 0.78f),
            )
            GameModeRow(state = state, onGameModeChange = onGameModeChange, compact = true)
            if (state.gameMode.isPointScoring) {
                SoloTrainingRow(
                    selected = state.soloTraining,
                    onChange = onSoloTrainingChange,
                )
            }
            // Stack names vertically in landscape so the text field stays tall enough to read.
            NameField(
                label = if (solo) "Player" else "Player 1",
                value = state.player1Name,
                onValueChange = onPlayer1Change,
                fieldModifier = Modifier.fillMaxWidth(),
            )
            if (!solo) {
                NameField(
                    label = "Player 2",
                    value = state.player2Name,
                    onValueChange = onPlayer2Change,
                    fieldModifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Column(
            modifier = Modifier.weight(LANDSCAPE_CONTROLS_WEIGHT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FormatControls(
                state = state,
                onRacksChange = onRacksChange,
                onPointsChange = onPointsChange,
                onInningsChange = onInningsChange,
                compact = true,
            )
            if (!solo) {
                Text(
                    text = if (state.gameMode.isPointScoring) "Who starts?" else "Who breaks first?",
                    style = MaterialTheme.typography.titleLarge,
                )
                BreakerRow(state = state, onBreakerChange = onBreakerChange, compact = true)
            }
            if (!state.gameMode.isPointScoring) {
                BreakRuleRow(
                    state = state,
                    onBreakRuleChange = onBreakRuleChange,
                    compact = true,
                )
            }
            TexturedActionButton(
                label = if (solo) "START TRAINING" else "START MATCH",
                base = felt.accent,
                light = felt.accentLight,
                dark = felt.accentDark,
                enabled = true,
                onClick = onStart,
                modifier = Modifier.widthIn(min = 240.dp),
                height = 52.dp,
            )
            TexturedActionButton(
                label = "HISTORY",
                base = felt.accent,
                light = felt.accentLight,
                dark = felt.accentDark,
                enabled = true,
                onClick = onOpenHistory,
                modifier = Modifier.widthIn(min = 240.dp),
                height = 48.dp,
            )
        }
    }
}

@Composable
private fun PortraitSetup(
    state: SetupUiState,
    onPlayer1Change: (String) -> Unit,
    onPlayer2Change: (String) -> Unit,
    onGameModeChange: (GameMode) -> Unit,
    onSoloTrainingChange: (Boolean) -> Unit,
    onRacksChange: (Int) -> Unit,
    onPointsChange: (Int) -> Unit,
    onInningsChange: (Int?) -> Unit,
    onBreakerChange: (Boolean) -> Unit,
    onBreakRuleChange: (BreakRule) -> Unit,
    onStart: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val felt = LocalFeltPalette.current
    val solo = state.gameMode.isPointScoring && state.soloTraining
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("RackTrack", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "American pool",
                style = MaterialTheme.typography.bodyLarge,
                color = ScoreWhite.copy(alpha = 0.78f),
            )
        }

        GameModeRow(state = state, onGameModeChange = onGameModeChange, compact = false)
        if (state.gameMode.isPointScoring) {
            SoloTrainingRow(
                selected = state.soloTraining,
                onChange = onSoloTrainingChange,
            )
        }

        NameField(
            label = if (solo) "Player" else "Player 1",
            value = state.player1Name,
            onValueChange = onPlayer1Change,
            fieldModifier = Modifier.fillMaxWidth(),
        )
        if (!solo) {
            NameField(
                label = "Player 2",
                value = state.player2Name,
                onValueChange = onPlayer2Change,
                fieldModifier = Modifier.fillMaxWidth(),
            )
        }

        FormatControls(
            state = state,
            onRacksChange = onRacksChange,
            onPointsChange = onPointsChange,
            onInningsChange = onInningsChange,
            compact = false,
        )

        if (!solo) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (state.gameMode.isPointScoring) {
                        "Who starts?"
                    } else {
                        "Who breaks first?"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Box(modifier = Modifier.height(10.dp))
                BreakerRow(state = state, onBreakerChange = onBreakerChange, compact = false)
                if (!state.gameMode.isPointScoring) {
                    Box(modifier = Modifier.height(10.dp))
                    BreakRuleRow(
                        state = state,
                        onBreakRuleChange = onBreakRuleChange,
                        compact = false,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        TexturedActionButton(
            label = if (solo) "START TRAINING" else "START MATCH",
            base = felt.accent,
            light = felt.accentLight,
            dark = felt.accentDark,
            enabled = true,
            onClick = onStart,
            modifier = Modifier.widthIn(min = 280.dp),
            height = 56.dp,
        )
        TexturedActionButton(
            label = "HISTORY",
            base = felt.accent,
            light = felt.accentLight,
            dark = felt.accentDark,
            enabled = true,
            onClick = onOpenHistory,
            modifier = Modifier.widthIn(min = 280.dp),
            height = 52.dp,
        )
    }
}

@Composable
private fun FormatControls(
    state: SetupUiState,
    onRacksChange: (Int) -> Unit,
    onPointsChange: (Int) -> Unit,
    onInningsChange: (Int?) -> Unit,
    compact: Boolean,
) {
    val felt = LocalFeltPalette.current
    if (state.gameMode.isPointScoring) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Distance", style = MaterialTheme.typography.titleLarge)
            if (!compact) Box(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MatchFormatOptions.pointsToWin.forEach { n ->
                    TexturedChip(
                        label = n.toString(),
                        selected = state.pointsToWin == n,
                        onClick = { onPointsChange(n) },
                        selectedLight = felt.accentLight,
                        selectedDark = felt.accentDark,
                        idleLight = felt.mid,
                        idleDark = felt.dark,
                        height = if (compact) 40.dp else 44.dp,
                    )
                }
            }
            Box(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
            Text("Innings", style = MaterialTheme.typography.titleLarge)
            if (!compact) Box(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MatchFormatOptions.inningsLimits.forEach { n ->
                    TexturedChip(
                        label = n.toString(),
                        selected = state.inningsLimit == n,
                        onClick = { onInningsChange(n) },
                        selectedLight = felt.accentLight,
                        selectedDark = felt.accentDark,
                        idleLight = felt.mid,
                        idleDark = felt.dark,
                        height = if (compact) 40.dp else 44.dp,
                    )
                }
                TexturedChip(
                    label = "∞",
                    selected = state.inningsLimit == null,
                    onClick = { onInningsChange(null) },
                    selectedLight = felt.accentLight,
                    selectedDark = felt.accentDark,
                    idleLight = felt.mid,
                    idleDark = felt.dark,
                    height = if (compact) 40.dp else 44.dp,
                )
            }
        }
    } else {
        var raceEditorOpen by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!compact) {
                Text("Race to", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
            TexturedSettingButton(
                label = if (compact) "Race to" else "Tap to set",
                value = state.racksToWin.toString(),
                onClick = { raceEditorOpen = true },
                modifier = Modifier.widthIn(max = 360.dp),
                height = if (compact) 44.dp else 52.dp,
                light = felt.accentLight,
                dark = felt.accentDark,
            )
        }
        if (raceEditorOpen) {
            IntStepperModal(
                title = "RACE TO",
                valueLabel = { it.toString() },
                initial = state.racksToWin,
                min = MatchFormatOptions.RACE_TO_MIN,
                max = MatchFormatOptions.RACE_TO_MAX,
                onDismiss = { raceEditorOpen = false },
                onConfirm = { n ->
                    onRacksChange(n)
                    raceEditorOpen = false
                },
            )
        }
    }
}

@Composable
private fun BreakerRow(
    state: SetupUiState,
    onBreakerChange: (Boolean) -> Unit,
    compact: Boolean,
) {
    val felt = LocalFeltPalette.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TexturedChip(
            label = state.player1Name.ifBlank { "Player 1" }.uppercase(),
            selected = state.player1BreaksFirst,
            onClick = { onBreakerChange(true) },
            modifier = Modifier.widthIn(min = 120.dp),
            selectedLight = felt.accentLight,
            selectedDark = felt.accentDark,
            idleLight = felt.light,
            idleDark = felt.dark,
            height = if (compact) 44.dp else 48.dp,
        )
        TexturedChip(
            label = state.player2Name.ifBlank { "Player 2" }.uppercase(),
            selected = !state.player1BreaksFirst,
            onClick = { onBreakerChange(false) },
            modifier = Modifier.widthIn(min = 120.dp),
            selectedLight = felt.accentLight,
            selectedDark = felt.accentDark,
            idleLight = felt.light,
            idleDark = felt.dark,
            height = if (compact) 44.dp else 48.dp,
        )
    }
}

@Composable
private fun BreakRuleRow(
    state: SetupUiState,
    onBreakRuleChange: (BreakRule) -> Unit,
    compact: Boolean,
) {
    val felt = LocalFeltPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Break rule", style = MaterialTheme.typography.titleLarge)
        if (!compact) Box(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TexturedChip(
                label = "ALTERNATE",
                selected = state.breakRule == BreakRule.ALTERNATE,
                onClick = { onBreakRuleChange(BreakRule.ALTERNATE) },
                modifier = Modifier.widthIn(min = 120.dp),
                selectedLight = felt.accentLight,
                selectedDark = felt.accentDark,
                idleLight = felt.light,
                idleDark = felt.dark,
                height = if (compact) 40.dp else 44.dp,
            )
            TexturedChip(
                label = "WINNER",
                selected = state.breakRule == BreakRule.WINNER,
                onClick = { onBreakRuleChange(BreakRule.WINNER) },
                modifier = Modifier.widthIn(min = 120.dp),
                selectedLight = felt.accentLight,
                selectedDark = felt.accentDark,
                idleLight = felt.light,
                idleDark = felt.dark,
                height = if (compact) 40.dp else 44.dp,
            )
        }
    }
}

@Composable
private fun SoloTrainingRow(
    selected: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val felt = LocalFeltPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Solo Training",
            style = MaterialTheme.typography.titleLarge,
            color = ScoreWhite,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = selected,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ScoreWhite,
                checkedTrackColor = felt.accent,
                checkedBorderColor = felt.accentDark,
                uncheckedThumbColor = ScoreWhite.copy(alpha = 0.85f),
                uncheckedTrackColor = felt.dark.copy(alpha = 0.55f),
                uncheckedBorderColor = OutlineWarm.copy(alpha = 0.45f),
            ),
        )
    }
}

@Composable
private fun GameModeRow(
    state: SetupUiState,
    onGameModeChange: (GameMode) -> Unit,
    compact: Boolean,
) {
    val felt = LocalFeltPalette.current
    Column(
        horizontalAlignment = if (compact) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Game mode", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                GameMode.EIGHT_BALL to "8",
                GameMode.NINE_BALL to "9",
                GameMode.TEN_BALL to "10",
                GameMode.FOURTEEN_ONE to "14/1",
            ).forEach { (mode, label) ->
                TexturedChip(
                    label = label,
                    selected = state.gameMode == mode,
                    onClick = { onGameModeChange(mode) },
                    modifier = Modifier.widthIn(min = if (compact) 64.dp else 72.dp),
                    selectedLight = felt.accentLight,
                    selectedDark = felt.accentDark,
                    idleLight = felt.light,
                    idleDark = felt.dark,
                    height = if (compact) 40.dp else 44.dp,
                )
            }
        }
    }
}

@Composable
private fun NameField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldModifier: Modifier = Modifier,
) {
    val felt = LocalFeltPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = fieldModifier.heightIn(min = 64.dp),
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ScoreWhite,
            unfocusedTextColor = ScoreWhite,
            focusedBorderColor = OutlineWarm,
            unfocusedBorderColor = OutlineWarm.copy(alpha = 0.45f),
            focusedLabelColor = ScoreWhite,
            unfocusedLabelColor = ScoreWhite.copy(alpha = 0.7f),
            cursorColor = ScoreWhite,
            focusedContainerColor = felt.dark.copy(alpha = 0.35f),
            unfocusedContainerColor = felt.dark.copy(alpha = 0.22f),
        ),
    )
}

@Preview(widthDp = 820, heightDp = 380, showBackground = true)
@Composable
private fun SetupLandscapePreview() {
    RackTrackTheme {
        SetupScreen(
            state = SetupUiState(gameMode = GameMode.FOURTEEN_ONE),
            onPlayer1Change = {},
            onPlayer2Change = {},
            onGameModeChange = {},
            onRacksChange = {},
            onPointsChange = {},
            onInningsChange = {},
            onBreakerChange = {},
            onStart = {},
        )
    }
}

private const val LANDSCAPE_CONTROLS_WEIGHT = 1.15f
