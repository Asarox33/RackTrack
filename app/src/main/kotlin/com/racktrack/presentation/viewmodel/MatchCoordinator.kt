package com.racktrack.presentation.viewmodel

import com.racktrack.appearance.FeltTone
import com.racktrack.data.UserSettings
import com.racktrack.domain.FourteenOneEngine
import com.racktrack.domain.MatchEngine
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.PlayerId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Pure match/setup orchestration (no Android types). [MatchViewModel] only wires prefs + lifecycle.
 */
class MatchCoordinator(
    initialSettings: UserSettings,
    private val persistSettings: (UserSettings) -> Unit,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _settings = MutableStateFlow(initialSettings)
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _setup = MutableStateFlow(setupFromPreferences(initialSettings))
    val setup: StateFlow<SetupUiState> = _setup.asStateFlow()

    private val _screen = MutableStateFlow<AppScreen>(AppScreen.Setup)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    private val _settingsOpen = MutableStateFlow(false)
    val settingsOpen: StateFlow<Boolean> = _settingsOpen.asStateFlow()

    fun openSettings() {
        _settingsOpen.value = true
    }

    fun closeSettings() {
        _settingsOpen.value = false
    }

    fun setFeltTone(tone: FeltTone) {
        updateSettings { it.copy(feltTone = tone) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        updateSettings { it.copy(keepScreenOn = enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        updateSettings { it.copy(hapticsEnabled = enabled) }
    }

    fun setDefaultRacksToWin(value: Int) {
        updateSettings { it.copy(defaultRacksToWin = value) }
        _setup.update { it.copy(racksToWin = value) }
    }

    fun setDefaultPointsToWin(value: Int) {
        updateSettings { it.copy(defaultPointsToWin = value) }
        _setup.update { it.copy(pointsToWin = value) }
    }

    fun setDefaultInningsLimit(value: Int?) {
        updateSettings { it.copy(defaultInningsLimit = value) }
        _setup.update { it.copy(inningsLimit = value) }
    }

    fun updatePlayer1Name(value: String) {
        _setup.update { it.copy(player1Name = value) }
    }

    fun updatePlayer2Name(value: String) {
        _setup.update { it.copy(player2Name = value) }
    }

    fun updateGameMode(value: GameMode) {
        _setup.update { it.copy(gameMode = value) }
    }

    fun updateRacksToWin(value: Int) {
        _setup.update { it.copy(racksToWin = value) }
    }

    fun updatePointsToWin(value: Int) {
        _setup.update { it.copy(pointsToWin = value) }
    }

    fun updateInningsLimit(value: Int?) {
        _setup.update { it.copy(inningsLimit = value) }
    }

    fun setPlayer1BreaksFirst(value: Boolean) {
        _setup.update { it.copy(player1BreaksFirst = value) }
    }

    fun startMatch() {
        val s = _setup.value
        val now = clock()
        val match = if (s.gameMode.isPointScoring) {
            Match.start(
                player1Name = s.player1Name,
                player2Name = s.player2Name,
                racksToWin = 1,
                initialBreakerIsPlayer1 = s.player1BreaksFirst,
                startedAtMillis = now,
                gameMode = s.gameMode,
                pointsToWin = s.pointsToWin,
                inningsLimit = s.inningsLimit,
            )
        } else {
            Match.start(
                player1Name = s.player1Name,
                player2Name = s.player2Name,
                racksToWin = s.racksToWin,
                initialBreakerIsPlayer1 = s.player1BreaksFirst,
                startedAtMillis = now,
                gameMode = s.gameMode,
            )
        }
        _screen.value = AppScreen.MatchBoard(match)
    }

    fun plusOne(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordPlusOne(it, playerId, clock())
    }

    fun runOut(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordRunOut(it, playerId, clock())
    }

    fun goldenBreak(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordGoldenBreak(it, playerId, clock())
    }

    fun dryBreak(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordDryBreak(it, playerId, clock())
    }

    fun eightBallLoss(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordEightBallLoss(it, playerId, clock())
    }

    fun foul(playerId: PlayerId) = mutateMatch {
        if (it.gameMode.isPointScoring) {
            FourteenOneEngine.foul(it, playerId, clock())
        } else {
            MatchEngine.recordFoul(it, playerId, clock())
        }
    }

    fun clearFouls(playerId: PlayerId) = mutateMatch {
        MatchEngine.clearConsecutiveFouls(it, playerId, clock())
    }

    fun addPoints(playerId: PlayerId, points: Int) = mutateMatch {
        FourteenOneEngine.addPoints(it, playerId, points, clock())
    }

    fun pass(playerId: PlayerId) = mutateMatch {
        FourteenOneEngine.pass(it, playerId, clock())
    }

    fun breakFoul(playerId: PlayerId) = mutateMatch {
        FourteenOneEngine.breakFoul(it, playerId, clock())
    }

    fun undo() = mutateMatch {
        if (it.gameMode.isPointScoring) {
            FourteenOneEngine.undoLast(it)
        } else {
            MatchEngine.undoLast(it)
        }
    }

    fun newMatch() {
        _screen.value = AppScreen.Setup
    }

    private fun updateSettings(transform: (UserSettings) -> UserSettings) {
        val next = transform(_settings.value)
        persistSettings(next)
        _settings.value = next
    }

    private fun mutateMatch(transform: (Match) -> Match) {
        val current = _screen.value
        if (current is AppScreen.MatchBoard) {
            _screen.value = AppScreen.MatchBoard(transform(current.match))
        }
    }

    private companion object {
        fun setupFromPreferences(settings: UserSettings): SetupUiState =
            SetupUiState(
                racksToWin = settings.defaultRacksToWin,
                pointsToWin = settings.defaultPointsToWin,
                inningsLimit = settings.defaultInningsLimit,
            )
    }
}
