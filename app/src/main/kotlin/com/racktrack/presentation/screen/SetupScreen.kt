package com.racktrack.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.racktrack.presentation.theme.ButtonRunOut
import com.racktrack.presentation.theme.FeltBlue
import com.racktrack.presentation.theme.FeltBlueDark
import com.racktrack.presentation.theme.FeltBlueLight
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.theme.ScoreWhite
import com.racktrack.presentation.viewmodel.SetupUiState

@Composable
fun SetupScreen(
    state: SetupUiState,
    onPlayer1Change: (String) -> Unit,
    onPlayer2Change: (String) -> Unit,
    onRacksChange: (Int) -> Unit,
    onBreakerChange: (Boolean) -> Unit,
    onStart: () -> Unit,
    rootModifier: Modifier = Modifier,
) {
    FeltBackground(modifier = rootModifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                text = "RackTrack",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "10-ball race · landscape",
                style = MaterialTheme.typography.bodyLarge,
                color = ScoreWhite.copy(alpha = 0.8f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                NameField(
                    label = "Player 1",
                    value = state.player1Name,
                    onValueChange = onPlayer1Change,
                    fieldModifier = Modifier.weight(1f),
                )
                NameField(
                    label = "Player 2",
                    value = state.player2Name,
                    onValueChange = onPlayer2Change,
                    fieldModifier = Modifier.weight(1f),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Race to", style = MaterialTheme.typography.titleLarge)
                Box(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(3, 5, 6, 7, 9).forEach { n ->
                        RaceChip(
                            label = n.toString(),
                            selected = state.racksToWin == n,
                            onClick = { onRacksChange(n) },
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Who breaks first?", style = MaterialTheme.typography.titleLarge)
                Box(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RaceChip(
                        label = state.player1Name.ifBlank { "Player 1" }.uppercase(),
                        selected = state.player1BreaksFirst,
                        onClick = { onBreakerChange(true) },
                        chipModifier = Modifier.widthIn(min = 160.dp),
                    )
                    RaceChip(
                        label = state.player2Name.ifBlank { "Player 2" }.uppercase(),
                        selected = !state.player1BreaksFirst,
                        onClick = { onBreakerChange(false) },
                        chipModifier = Modifier.widthIn(min = 160.dp),
                    )
                }
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .widthIn(min = 280.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRunOut,
                    contentColor = ScoreWhite,
                ),
            ) {
                Text("START MATCH", style = MaterialTheme.typography.labelLarge)
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = fieldModifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ScoreWhite,
            unfocusedTextColor = ScoreWhite,
            focusedBorderColor = ScoreWhite,
            unfocusedBorderColor = ScoreWhite.copy(alpha = 0.5f),
            focusedLabelColor = ScoreWhite,
            unfocusedLabelColor = ScoreWhite.copy(alpha = 0.7f),
            cursorColor = ScoreWhite,
        ),
    )
}

@Composable
private fun RaceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    chipModifier: Modifier = Modifier,
) {
    val bg = if (selected) ButtonRunOut else FeltBlueDark
    val border = if (selected) ScoreWhite else ScoreWhite.copy(alpha = 0.4f)
    Box(
        modifier = chipModifier
            .height(52.dp)
            .border(2.dp, border, RoundedCornerShape(14.dp))
            .background(bg, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FeltBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FeltBlueLight, FeltBlue, FeltBlueDark),
                ),
            ),
    ) {
        content()
    }
}

@Preview(widthDp = 820, heightDp = 380, showBackground = true)
@Composable
private fun SetupPreview() {
    RackTrackTheme {
        SetupScreen(
            state = SetupUiState(),
            onPlayer1Change = {},
            onPlayer2Change = {},
            onRacksChange = {},
            onBreakerChange = {},
            onStart = {},
        )
    }
}
