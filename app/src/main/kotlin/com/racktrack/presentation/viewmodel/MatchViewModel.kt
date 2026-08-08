package com.racktrack.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.racktrack.appearance.FeltTone
import com.racktrack.data.AppPreferences
import com.racktrack.data.UserSettings
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.PlayerId
import kotlinx.coroutines.flow.StateFlow

data class SetupUiState(
    val player1Name: String = "",
    val player2Name: String = "",
    val gameMode: GameMode = GameMode.TEN_BALL,
    val racksToWin: Int = 6,
    val pointsToWin: Int = 100,
    /** Null = unlimited innings. */
    val inningsLimit: Int? = 30,
    val player1BreaksFirst: Boolean = true,
)

sealed interface AppScreen {
    data object Setup : AppScreen
    data class MatchBoard(val match: Match) : AppScreen
}

/** Thin Android shell: preferences + [MatchCoordinator]. */
class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val coordinator = MatchCoordinator(
        initialSettings = preferences.load(),
        persistSettings = preferences::save,
    )

    val settings: StateFlow<UserSettings> = coordinator.settings
    val setup: StateFlow<SetupUiState> = coordinator.setup
    val screen: StateFlow<AppScreen> = coordinator.screen
    val settingsOpen: StateFlow<Boolean> = coordinator.settingsOpen

    fun openSettings() = coordinator.openSettings()

    fun closeSettings() = coordinator.closeSettings()

    fun setFeltTone(tone: FeltTone) = coordinator.setFeltTone(tone)

    fun setKeepScreenOn(enabled: Boolean) = coordinator.setKeepScreenOn(enabled)

    fun setHapticsEnabled(enabled: Boolean) = coordinator.setHapticsEnabled(enabled)

    fun setDefaultRacksToWin(value: Int) = coordinator.setDefaultRacksToWin(value)

    fun setDefaultPointsToWin(value: Int) = coordinator.setDefaultPointsToWin(value)

    fun setDefaultInningsLimit(value: Int?) = coordinator.setDefaultInningsLimit(value)

    fun updatePlayer1Name(value: String) = coordinator.updatePlayer1Name(value)

    fun updatePlayer2Name(value: String) = coordinator.updatePlayer2Name(value)

    fun updateGameMode(value: GameMode) = coordinator.updateGameMode(value)

    fun updateRacksToWin(value: Int) = coordinator.updateRacksToWin(value)

    fun updatePointsToWin(value: Int) = coordinator.updatePointsToWin(value)

    fun updateInningsLimit(value: Int?) = coordinator.updateInningsLimit(value)

    fun setPlayer1BreaksFirst(value: Boolean) = coordinator.setPlayer1BreaksFirst(value)

    fun startMatch() = coordinator.startMatch()

    fun plusOne(playerId: PlayerId) = coordinator.plusOne(playerId)

    fun runOut(playerId: PlayerId) = coordinator.runOut(playerId)

    fun goldenBreak(playerId: PlayerId) = coordinator.goldenBreak(playerId)

    fun dryBreak(playerId: PlayerId) = coordinator.dryBreak(playerId)

    fun eightBallLoss(playerId: PlayerId) = coordinator.eightBallLoss(playerId)

    fun foul(playerId: PlayerId) = coordinator.foul(playerId)

    fun addPoints(playerId: PlayerId, points: Int) = coordinator.addPoints(playerId, points)

    fun pass(playerId: PlayerId) = coordinator.pass(playerId)

    fun breakFoul(playerId: PlayerId) = coordinator.breakFoul(playerId)

    fun undo() = coordinator.undo()

    fun newMatch() = coordinator.newMatch()
}
