package com.racktrack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.racktrack.domain.MatchEngine
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.PlayerId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SetupUiState(
    val player1Name: String = "Alex",
    val player2Name: String = "Sam",
    val racksToWin: Int = 6,
    val player1BreaksFirst: Boolean = true,
)

sealed interface AppScreen {
    data object Setup : AppScreen
    data class MatchBoard(val match: Match) : AppScreen
}

class MatchViewModel : ViewModel() {
    private val _setup = MutableStateFlow(SetupUiState())
    val setup: StateFlow<SetupUiState> = _setup.asStateFlow()

    private val _screen = MutableStateFlow<AppScreen>(AppScreen.Setup)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    fun updatePlayer1Name(value: String) {
        _setup.update { it.copy(player1Name = value) }
    }

    fun updatePlayer2Name(value: String) {
        _setup.update { it.copy(player2Name = value) }
    }

    fun updateRacksToWin(value: Int) {
        _setup.update { it.copy(racksToWin = value) }
    }

    fun setPlayer1BreaksFirst(value: Boolean) {
        _setup.update { it.copy(player1BreaksFirst = value) }
    }

    fun startMatch() {
        val s = _setup.value
        val match = Match.start(
            player1Name = s.player1Name,
            player2Name = s.player2Name,
            racksToWin = s.racksToWin,
            initialBreakerIsPlayer1 = s.player1BreaksFirst,
        )
        _screen.value = AppScreen.MatchBoard(match)
    }

    fun plusOne(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordPlusOne(it, playerId)
    }

    fun runOut(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordRunOut(it, playerId)
    }

    fun foul(playerId: PlayerId) = mutateMatch {
        MatchEngine.recordFoul(it, playerId)
    }

    fun undo() = mutateMatch { MatchEngine.undoLast(it) }

    fun newMatch() {
        _screen.value = AppScreen.Setup
    }

    private fun mutateMatch(transform: (Match) -> Match) {
        val current = _screen.value
        if (current is AppScreen.MatchBoard) {
            _screen.value = AppScreen.MatchBoard(transform(current.match))
        }
    }
}
