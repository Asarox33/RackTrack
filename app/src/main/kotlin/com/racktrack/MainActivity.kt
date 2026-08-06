package com.racktrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.racktrack.presentation.screen.MatchBoardScreen
import com.racktrack.presentation.screen.SetupScreen
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.viewmodel.AppScreen
import com.racktrack.presentation.viewmodel.MatchViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RackTrackTheme {
                val screen by viewModel.screen.collectAsStateWithLifecycle()
                val setup by viewModel.setup.collectAsStateWithLifecycle()

                when (val current = screen) {
                    AppScreen.Setup -> SetupScreen(
                        state = setup,
                        onPlayer1Change = viewModel::updatePlayer1Name,
                        onPlayer2Change = viewModel::updatePlayer2Name,
                        onRacksChange = viewModel::updateRacksToWin,
                        onBreakerChange = viewModel::setPlayer1BreaksFirst,
                        onStart = viewModel::startMatch,
                    )
                    is AppScreen.MatchBoard -> MatchBoardScreen(
                        match = current.match,
                        onPlusOne = viewModel::plusOne,
                        onRunOut = viewModel::runOut,
                        onFoul = viewModel::foul,
                        onUndo = viewModel::undo,
                        onNewMatch = viewModel::newMatch,
                    )
                }
            }
        }
    }
}
