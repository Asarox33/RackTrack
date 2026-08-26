package com.racktrack

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.racktrack.i18n.LocaleCatalogs
import com.racktrack.i18n.Strings
import com.racktrack.monetization.MonetizationFacade
import com.racktrack.presentation.screen.AboutScreen
import com.racktrack.presentation.screen.HistoryDetailScreen
import com.racktrack.presentation.screen.HistoryScreen
import com.racktrack.presentation.screen.MatchBoardScreen
import com.racktrack.presentation.screen.SettingsScreen
import com.racktrack.presentation.screen.SetupScreen
import com.racktrack.presentation.theme.RackTrackTheme
import com.racktrack.presentation.viewmodel.AppScreen
import com.racktrack.presentation.viewmodel.MatchViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MatchViewModel by viewModels()
    private lateinit var monetization: MonetizationFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        monetization = MonetizationFacade(this, lifecycleScope)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        setContent {
            RackTrackRoot(
                activity = this,
                viewModel = viewModel,
                monetization = monetization,
            )
        }
    }

    override fun onDestroy() {
        if (::monetization.isInitialized) {
            monetization.stop()
        }
        super.onDestroy()
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

@Composable
private fun RackTrackRoot(
    activity: MainActivity,
    viewModel: MatchViewModel,
    monetization: MonetizationFacade,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val adsRemoved by monetization.adsRemoved.collectAsStateWithLifecycle()
    val billingStatus by monetization.billingStatusMessage.collectAsStateWithLifecycle()
    val stringProvider =
        remember(settings.appLanguage) {
            LocaleCatalogs.resolve(settings.appLanguage, Locale.getDefault().language)
        }
    Strings.provider = stringProvider

    // First frame first — never block cold start on UMP / Ads / Billing.
    LaunchedEffect(Unit) {
        monetization.start(activity)
    }

    LaunchedEffect(billingStatus) {
        val message = billingStatus ?: return@LaunchedEffect
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        monetization.consumeBillingStatusMessage()
    }

    LaunchedEffect(settings.keepScreenOn) {
        if (settings.keepScreenOn) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    RackTrackTheme(
        themeMode = settings.themeMode,
        hapticsEnabled = settings.hapticsEnabled,
        strings = stringProvider,
    ) {
        val screen by viewModel.screen.collectAsStateWithLifecycle()
        val setup by viewModel.setup.collectAsStateWithLifecycle()
        val history by viewModel.history.collectAsStateWithLifecycle()
        val selectedHistory by viewModel.selectedHistoryMatch.collectAsStateWithLifecycle()

        when (val current = screen) {
            AppScreen.Setup -> {
                LaunchedEffect(Unit) {
                    monetization.onSetupVisible()
                }
                SetupScreen(
                    state = setup,
                    onPlayer1Change = viewModel::updatePlayer1Name,
                    onPlayer2Change = viewModel::updatePlayer2Name,
                    onGameModeChange = viewModel::updateGameMode,
                    onSoloTrainingChange = viewModel::setSoloTraining,
                    onRacksChange = viewModel::updateRacksToWin,
                    onPointsChange = viewModel::updatePointsToWin,
                    onInningsChange = viewModel::updateInningsLimit,
                    onBreakerChange = viewModel::setPlayer1BreaksFirst,
                    onBreakRuleChange = viewModel::setBreakRule,
                    onStart = {
                        monetization.runAfterAdOpportunity(activity) {
                            viewModel.startMatch()
                        }
                    },
                    onOpenHistory = viewModel::openHistory,
                    onOpenSettings = viewModel::openSettings,
                    onOpenAbout = viewModel::openAbout,
                )
            }
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
                    onAcceptIllegalOpen = viewModel::acceptIllegalOpen,
                    onAnnouncePushOut = viewModel::announcePushOut,
                    onResolvePushOutClean = viewModel::resolvePushOutClean,
                    onResolvePushOutFoul = viewModel::resolvePushOutFoul,
                    onTakePushOut = viewModel::takePushOut,
                    onReturnPushOut = viewModel::returnPushOut,
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
            AppScreen.Settings -> SettingsScreen(
                settings = settings,
                adsRemoved = adsRemoved,
                onRemoveAds = { monetization.launchRemoveAdsPurchase(activity) },
                onRestorePurchases = { monetization.restorePurchases() },
                onThemeSelected = viewModel::setThemeMode,
                onAppLanguageSelected = viewModel::setAppLanguage,
                onKeepScreenOnChange = viewModel::setKeepScreenOn,
                onHapticsChange = viewModel::setHapticsEnabled,
                onDefaultRacksChange = viewModel::setDefaultRacksToWin,
                onDefaultPointsChange = viewModel::setDefaultPointsToWin,
                onDefaultInningsChange = viewModel::setDefaultInningsLimit,
                onDefaultBreakRuleChange = viewModel::setDefaultBreakRule,
                onBack = viewModel::closeSettings,
            )
            AppScreen.About -> AboutScreen(
                onBack = viewModel::closeAbout,
            )
        }
    }
}
