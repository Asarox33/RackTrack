package com.racktrack

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.racktrack.presentation.screen.MatchBoardScreen
import com.racktrack.presentation.screen.SettingsSheet
import com.racktrack.presentation.screen.SetupScreen
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.viewmodel.AppScreen
import com.racktrack.presentation.viewmodel.MatchViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val settingsOpen by viewModel.settingsOpen.collectAsStateWithLifecycle()

            LaunchedEffect(settings.keepScreenOn) {
                if (settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            RackTrackTheme(
                feltTone = settings.feltTone,
                hapticsEnabled = settings.hapticsEnabled,
            ) {
                val screen by viewModel.screen.collectAsStateWithLifecycle()
                val setup by viewModel.setup.collectAsStateWithLifecycle()

                when (val current = screen) {
                    AppScreen.Setup -> SetupScreen(
                        state = setup,
                        onPlayer1Change = viewModel::updatePlayer1Name,
                        onPlayer2Change = viewModel::updatePlayer2Name,
                        onGameModeChange = viewModel::updateGameMode,
                        onRacksChange = viewModel::updateRacksToWin,
                        onPointsChange = viewModel::updatePointsToWin,
                        onInningsChange = viewModel::updateInningsLimit,
                        onBreakerChange = viewModel::setPlayer1BreaksFirst,
                        onStart = viewModel::startMatch,
                        onOpenSettings = viewModel::openSettings,
                    )
                    is AppScreen.MatchBoard -> MatchBoardScreen(
                        match = current.match,
                        onPlusOne = viewModel::plusOne,
                        onRunOut = viewModel::runOut,
                        onGoldenBreak = viewModel::goldenBreak,
                        onDryBreak = viewModel::dryBreak,
                        onEightBallLoss = viewModel::eightBallLoss,
                        onAddPoints = viewModel::addPoints,
                        onPass = viewModel::pass,
                        onBreakFoul = viewModel::breakFoul,
                        onFoul = viewModel::foul,
                        onClearFouls = viewModel::clearFouls,
                        onUndo = viewModel::undo,
                        onNewMatch = viewModel::newMatch,
                        onOpenSettings = viewModel::openSettings,
                    )
                }

                if (settingsOpen) {
                    SettingsSheet(
                        settings = settings,
                        onFeltSelected = viewModel::setFeltTone,
                        onKeepScreenOnChange = viewModel::setKeepScreenOn,
                        onHapticsChange = viewModel::setHapticsEnabled,
                        onDefaultRacksChange = viewModel::setDefaultRacksToWin,
                        onDefaultPointsChange = viewModel::setDefaultPointsToWin,
                        onDefaultInningsChange = viewModel::setDefaultInningsLimit,
                        onDismiss = viewModel::closeSettings,
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
