package com.racktrack.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.racktrack.appearance.FeltTone
import com.racktrack.data.AppPreferences
import com.racktrack.data.JsonMatchHistoryStore
import com.racktrack.data.MatchHistoryFilter
import com.racktrack.data.MatchHistoryStore
import com.racktrack.data.StoredMatch
import com.racktrack.data.UserSettings
import com.racktrack.domain.MatchStats
import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.PlayerId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetupUiState(
    val player1Name: String = "",
    val player2Name: String = "",
    val gameMode: GameMode = GameMode.TEN_BALL,
    val racksToWin: Int = 6,
    val pointsToWin: Int = 100,
    /** Null = unlimited innings. */
    val inningsLimit: Int? = 30,
    val player1BreaksFirst: Boolean = true,
    val breakRule: BreakRule = BreakRule.ALTERNATE,
    /** 14/1 only — practice without an opponent. */
    val soloTraining: Boolean = false,
)

data class HistoryUiState(
    val gameMode: GameMode = GameMode.TEN_BALL,
    val playerFilter1: String = "",
    val playerFilter2: String = "",
    val matches: List<StoredMatch> = emptyList(),
)

sealed interface AppScreen {
    data object Setup : AppScreen
    data class MatchBoard(val match: Match) : AppScreen
    data object History : AppScreen
    data class HistoryDetail(val matchId: String) : AppScreen
    data object Settings : AppScreen
}

/** Thin Android shell: preferences, history store, [MatchCoordinator]. */
class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val historyStore: MatchHistoryStore = JsonMatchHistoryStore(application)

    private val coordinator = MatchCoordinator(
        initialSettings = preferences.load(),
        persistSettings = preferences::save,
        onMatchCompleted = ::persistCompletedMatch,
    )

    private val _historyGameMode = MutableStateFlow(GameMode.TEN_BALL)
    private val _playerFilter1 = MutableStateFlow("")
    private val _playerFilter2 = MutableStateFlow("")

    val settings: StateFlow<UserSettings> = coordinator.settings
    val setup: StateFlow<SetupUiState> = coordinator.setup
    val screen: StateFlow<AppScreen> = coordinator.screen
    val matchPaused: StateFlow<Boolean> = coordinator.matchPaused

    val history: StateFlow<HistoryUiState> = combine(
        historyStore.matches,
        _historyGameMode,
        _playerFilter1,
        _playerFilter2,
    ) { matches, gameMode, filter1, filter2 ->
        HistoryUiState(
            gameMode = gameMode,
            playerFilter1 = filter1,
            playerFilter2 = filter2,
            matches = MatchHistoryFilter.apply(
                matches = matches,
                playerQuery1 = filter1,
                playerQuery2 = filter2,
                gameMode = gameMode,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    private val _selectedHistoryMatch = MutableStateFlow<StoredMatch?>(null)
    val selectedHistoryMatch: StateFlow<StoredMatch?> = _selectedHistoryMatch

    fun openSettings() = coordinator.openSettings()

    fun closeSettings() = coordinator.closeSettings()

    fun toggleMatchPause() = coordinator.toggleMatchPause()

    fun setFeltTone(tone: FeltTone) = coordinator.setFeltTone(tone)

    fun setKeepScreenOn(enabled: Boolean) = coordinator.setKeepScreenOn(enabled)

    fun setHapticsEnabled(enabled: Boolean) = coordinator.setHapticsEnabled(enabled)

    fun setDefaultRacksToWin(value: Int) = coordinator.setDefaultRacksToWin(value)

    fun setDefaultPointsToWin(value: Int) = coordinator.setDefaultPointsToWin(value)

    fun setDefaultInningsLimit(value: Int?) = coordinator.setDefaultInningsLimit(value)

    fun setDefaultBreakRule(value: BreakRule) = coordinator.setDefaultBreakRule(value)

    fun updatePlayer1Name(value: String) = coordinator.updatePlayer1Name(value)

    fun updatePlayer2Name(value: String) = coordinator.updatePlayer2Name(value)

    fun updateGameMode(value: GameMode) = coordinator.updateGameMode(value)

    fun setSoloTraining(value: Boolean) = coordinator.setSoloTraining(value)

    fun updateRacksToWin(value: Int) = coordinator.updateRacksToWin(value)

    fun updatePointsToWin(value: Int) = coordinator.updatePointsToWin(value)

    fun updateInningsLimit(value: Int?) = coordinator.updateInningsLimit(value)

    fun setPlayer1BreaksFirst(value: Boolean) = coordinator.setPlayer1BreaksFirst(value)

    fun setBreakRule(value: BreakRule) = coordinator.setBreakRule(value)

    fun startMatch() = coordinator.startMatch()

    fun plusOne(playerId: PlayerId) = coordinator.plusOne(playerId)

    fun runOut(playerId: PlayerId) = coordinator.runOut(playerId)

    fun goldenBreak(playerId: PlayerId) = coordinator.goldenBreak(playerId)

    fun dryBreak(playerId: PlayerId) = coordinator.dryBreak(playerId)

    fun eightBallLoss(playerId: PlayerId) = coordinator.eightBallLoss(playerId)

    fun foul(playerId: PlayerId) = coordinator.foul(playerId)

    fun foulWithRemaining(playerId: PlayerId, remaining: Int, priorPoints: Int = 0) =
        coordinator.foulWithRemaining(playerId, remaining, priorPoints)

    fun clearFouls(playerId: PlayerId) = coordinator.clearFouls(playerId)

    fun addPoints(playerId: PlayerId, points: Int) = coordinator.addPoints(playerId, points)

    fun pass(playerId: PlayerId) = coordinator.pass(playerId)

    fun passWithRemaining(playerId: PlayerId, remaining: Int, priorPoints: Int = 0) =
        coordinator.passWithRemaining(playerId, remaining, priorPoints)

    fun breakFoul(playerId: PlayerId) = coordinator.breakFoul(playerId)

    fun acceptIllegalOpen() = coordinator.acceptIllegalOpen()

    fun announcePushOut(playerId: PlayerId) = coordinator.announcePushOut(playerId)

    fun resolvePushOutClean(playerId: PlayerId) = coordinator.resolvePushOutClean(playerId)

    fun resolvePushOutFoul(playerId: PlayerId) = coordinator.resolvePushOutFoul(playerId)

    fun takePushOut() = coordinator.takePushOut()

    fun returnPushOut() = coordinator.returnPushOut()

    fun undo() = coordinator.undo()

    fun newMatch() = coordinator.newMatch()

    fun openHistory() {
        _historyGameMode.value = coordinator.setup.value.gameMode
        _playerFilter1.value = ""
        _playerFilter2.value = ""
        coordinator.openHistory()
    }

    fun setHistoryPlayerFilter1(value: String) {
        _playerFilter1.value = value
    }

    fun setHistoryPlayerFilter2(value: String) {
        _playerFilter2.value = value
    }

    fun openHistoryDetail(matchId: String) {
        viewModelScope.launch {
            _selectedHistoryMatch.value = historyStore.getById(matchId)
            coordinator.openHistoryDetail(matchId)
        }
    }

    fun closeHistoryDetail() {
        _selectedHistoryMatch.value = null
        coordinator.closeHistoryDetail()
    }

    fun closeHistory() {
        _selectedHistoryMatch.value = null
        _playerFilter1.value = ""
        _playerFilter2.value = ""
        coordinator.newMatch()
    }

    fun deleteHistoryMatch(matchId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historyStore.deleteById(matchId)
            if (_selectedHistoryMatch.value?.id == matchId) {
                _selectedHistoryMatch.value = null
            }
        }
    }

    private fun persistCompletedMatch(match: Match) {
        viewModelScope.launch(Dispatchers.IO) {
            historyStore.saveCompleted(
                summary = MatchStats.summarize(match),
                completedAtMillis = System.currentTimeMillis(),
            )
        }
    }
}
