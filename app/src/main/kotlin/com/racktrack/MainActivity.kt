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
import com.racktrack.presentation.screen.HistoryDetailScreen
import com.racktrack.presentation.screen.HistoryScreen
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
                val history by viewModel.history.collectAsStateWithLifecycle()
                val selectedHistory by viewModel.selectedHistoryMatch.collectAsStateWithLifecycle()

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
                        onBreakRuleChange = viewModel::setBreakRule,
                        onStart = viewModel::startMatch,
                        onOpenHistory = viewModel::openHistory,
                        onOpenSettings = viewModel::openSettings,
                    )
                    is AppScreen.MatchBoard -> {
                        val matchPaused by viewModel.matchPaused.collectAsStateWithLifecycle()
                        MatchBoardScreen(
                            match = current.match,
                            onPlusOne = viewModel::plusOne,
                            onRunOut = viewModel::runOut,
                            onGoldenBreak = viewModel::goldenBreak,
                            onDryBreak = viewModel::dryBreak,
                            onEightBallLoss = viewModel::eightBallLoss,
                            onAddPoints = viewModel::addPoints,
                            onPassWithRemaining = viewModel::passWithRemaining,
                            onBreakFoul = viewModel::breakFoul,
                            onFoul = viewModel::foul,
                            onFoulWithRemaining = viewModel::foulWithRemaining,
                            onClearFouls = viewModel::clearFouls,
                            onUndo = viewModel::undo,
                            onNewMatch = viewModel::newMatch,
                            onOpenSettings = viewModel::openSettings,
                            matchPaused = matchPaused,
                            onTogglePause = viewModel::toggleMatchPause,
                        )
                    }
                    AppScreen.History -> HistoryScreen(
                        state = history,
                        onPlayerFilter1Change = viewModel::setHistoryPlayerFilter1,
                        onPlayerFilter2Change = viewModel::setHistoryPlayerFilter2,
                        onOpenMatch = viewModel::openHistoryDetail,
                        onDeleteMatch = viewModel::deleteHistoryMatch,
                        onBack = viewModel::closeHistory,
                    )
                    is AppScreen.HistoryDetail -> HistoryDetailScreen(
                        match = selectedHistory,
                        onBack = viewModel::closeHistoryDetail,
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
                        onDefaultBreakRuleChange = viewModel::setDefaultBreakRule,
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
