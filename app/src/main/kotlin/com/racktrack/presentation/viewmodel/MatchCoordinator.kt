package com.racktrack.presentation.viewmodel

import com.racktrack.presentation.theme.AppThemeMode
import com.racktrack.data.UserSettings
import com.racktrack.domain.FourteenOneEngine
import com.racktrack.domain.MatchEngine
import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PauseSpan
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
    private val onMatchCompleted: (Match) -> Unit = {},
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _settings = MutableStateFlow(initialSettings)
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _setup = MutableStateFlow(setupFromPreferences(initialSettings))
    val setup: StateFlow<SetupUiState> = _setup.asStateFlow()

    private val _screen = MutableStateFlow<AppScreen>(AppScreen.Setup)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    /** Screen to restore when leaving [AppScreen.Settings] (Setup or in-progress board). */
    private var settingsReturnScreen: AppScreen = AppScreen.Setup

    /** Club break pause — freezes duration accounting; not an FFB player timeout. */
    private val _matchPaused = MutableStateFlow(false)
    val matchPaused: StateFlow<Boolean> = _matchPaused.asStateFlow()

    /** Wall time when the current open pause began; null when running. */
    private var openPauseStartedAt: Long? = null

    fun openSettings() {
        val current = _screen.value
        if (current is AppScreen.Settings) return
        settingsReturnScreen = current
        _screen.value = AppScreen.Settings
    }

    fun closeSettings() {
        _screen.value = settingsReturnScreen
        settingsReturnScreen = AppScreen.Setup
    }

    fun toggleMatchPause() {
        val board = _screen.value as? AppScreen.MatchBoard ?: return
        if (board.match.status != MatchStatus.IN_PROGRESS) return
        val wall = clock()
        val started = openPauseStartedAt
        if (started == null) {
            openPauseStartedAt = wall
            _matchPaused.value = true
        } else {
            openPauseStartedAt = null
            _matchPaused.value = false
            val span = PauseSpan(startMillis = started, endMillis = wall)
            _screen.value = AppScreen.MatchBoard(
                board.match.copy(pauseSpans = board.match.pauseSpans + span),
            )
        }
    }

    private fun clearPauseState() {
        openPauseStartedAt = null
        _matchPaused.value = false
    }

    fun setThemeMode(mode: AppThemeMode) {
        updateSettings { it.copy(themeMode = mode) }
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

    fun setDefaultBreakRule(value: BreakRule) {
        updateSettings { it.copy(defaultBreakRule = value) }
        _setup.update { it.copy(breakRule = value) }
    }

    fun updatePlayer1Name(value: String) {
        _setup.update { it.copy(player1Name = value) }
    }

    fun updatePlayer2Name(value: String) {
        _setup.update { it.copy(player2Name = value) }
    }

    fun updateGameMode(value: GameMode) {
        _setup.update {
            it.copy(
                gameMode = value,
                soloTraining = if (value.isPointScoring) it.soloTraining else false,
            )
        }
    }

    fun setSoloTraining(value: Boolean) {
        _setup.update {
            if (!it.gameMode.isPointScoring) {
                it.copy(soloTraining = false)
            } else {
                it.copy(
                    soloTraining = value,
                    player1BreaksFirst = if (value) true else it.player1BreaksFirst,
                )
            }
        }
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

    fun setBreakRule(value: BreakRule) {
        _setup.update { it.copy(breakRule = value) }
    }

    fun startMatch() {
        clearPauseState()
        val s = _setup.value
        val now = clock()
        val match = if (s.gameMode.isPointScoring) {
            Match.start(
                player1Name = s.player1Name,
                player2Name = s.player2Name,
                racksToWin = 1,
                initialBreakerIsPlayer1 = if (s.soloTraining) true else s.player1BreaksFirst,
                startedAtMillis = now,
                gameMode = s.gameMode,
                pointsToWin = s.pointsToWin,
                inningsLimit = s.inningsLimit,
                solo = s.soloTraining,
            )
        } else {
            Match.start(
                player1Name = s.player1Name,
                player2Name = s.player2Name,
                racksToWin = s.racksToWin,
                initialBreakerIsPlayer1 = s.player1BreaksFirst,
                startedAtMillis = now,
                gameMode = s.gameMode,
                breakRule = s.breakRule,
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

    fun foulWithRemaining(playerId: PlayerId, remaining: Int, priorPoints: Int = 0) = mutateMatch {
        var match = it
        if (priorPoints > 0) {
            match = FourteenOneEngine.addPoints(match, playerId, priorPoints, clock())
            if (match.status != MatchStatus.IN_PROGRESS) return@mutateMatch match
        }
        FourteenOneEngine.foulWithRemaining(match, playerId, remaining, clock())
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

    fun passWithRemaining(playerId: PlayerId, remaining: Int, priorPoints: Int = 0) = mutateMatch {
        var match = it
        if (priorPoints > 0) {
            match = FourteenOneEngine.addPoints(match, playerId, priorPoints, clock())
            if (match.status != MatchStatus.IN_PROGRESS) return@mutateMatch match
        }
        FourteenOneEngine.passWithRemaining(match, playerId, remaining, clock())
    }

    fun breakFoul(playerId: PlayerId) = mutateMatch {
        FourteenOneEngine.breakFoul(it, playerId, clock())
    }

    fun acceptIllegalOpen() = mutateMatch {
        FourteenOneEngine.acceptIllegalOpen(it, clock())
    }

    fun announcePushOut(playerId: PlayerId) = mutateMatch {
        MatchEngine.announcePushOut(it, playerId, clock())
    }

    fun resolvePushOutClean(playerId: PlayerId) = mutateMatch {
        MatchEngine.resolvePushOutClean(it, playerId, clock())
    }

    fun resolvePushOutFoul(playerId: PlayerId) = mutateMatch {
        MatchEngine.resolvePushOutFoul(it, playerId, clock())
    }

    fun takePushOut() = mutateMatch {
        MatchEngine.takePushOut(it, clock())
    }

    fun returnPushOut() = mutateMatch {
        MatchEngine.returnPushOut(it, clock())
    }

    fun undo() = mutateMatch {
        if (it.gameMode.isPointScoring) {
            FourteenOneEngine.undoLast(it)
        } else {
            MatchEngine.undoLast(it)
        }
    }

    fun newMatch() {
        clearPauseState()
        _screen.value = AppScreen.Setup
    }

    fun openHistory() {
        _screen.value = AppScreen.History
    }

    fun openHistoryDetail(matchId: String) {
        _screen.value = AppScreen.HistoryDetail(matchId)
    }

    fun closeHistoryDetail() {
        _screen.value = AppScreen.History
    }

    private fun updateSettings(transform: (UserSettings) -> UserSettings) {
        val next = transform(_settings.value)
        persistSettings(next)
        _settings.value = next
    }

    private fun mutateMatch(transform: (Match) -> Match) {
        if (_matchPaused.value) return
        val current = _screen.value
        if (current is AppScreen.MatchBoard) {
            val previous = current.match
            val next = transform(previous)
            _screen.value = AppScreen.MatchBoard(next)
            if (
                previous.status != MatchStatus.COMPLETED &&
                next.status == MatchStatus.COMPLETED
            ) {
                onMatchCompleted(next)
            }
        }
    }

    private companion object {
        fun setupFromPreferences(settings: UserSettings): SetupUiState =
            SetupUiState(
                racksToWin = settings.defaultRacksToWin,
                pointsToWin = settings.defaultPointsToWin,
                inningsLimit = settings.defaultInningsLimit,
                breakRule = settings.defaultBreakRule,
            )
    }
}
