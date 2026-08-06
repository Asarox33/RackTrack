package com.racktrack.domain

import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEvent
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PlayerId

/** Pure rules for the landscape race MVP — no Android dependencies. */
object MatchEngine {
    fun recordPlusOne(match: Match, playerId: PlayerId): Match =
        awardRack(match, playerId, MatchEventType.PLUS_ONE)

    fun recordRunOut(match: Match, playerId: PlayerId): Match =
        awardRack(match, playerId, MatchEventType.RUN_OUT)

    fun recordFoul(match: Match, playerId: PlayerId): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        requireKnownPlayer(match, playerId)
        val updated = when (playerId) {
            match.player1.id -> match.copy(foul1 = match.foul1 + 1)
            else -> match.copy(foul2 = match.foul2 + 1)
        }
        return updated.copy(
            history = match.history + MatchEvent(MatchEventType.FOUL, playerId),
        )
    }

    fun undoLast(match: Match): Match {
        val last = match.history.lastOrNull() ?: return match
        val withoutLast = match.history.dropLast(1)
        return when (last.type) {
            MatchEventType.PLUS_ONE, MatchEventType.RUN_OUT -> {
                val revertedScores = when (last.playerId) {
                    match.player1.id -> match.copy(
                        score1 = (match.score1 - 1).coerceAtLeast(0),
                    )
                    else -> match.copy(
                        score2 = (match.score2 - 1).coerceAtLeast(0),
                    )
                }
                // Alternate-break swaps on award; swap again to restore previous breaker.
                revertedScores.copy(
                    currentBreakerId = match.otherPlayerId(match.currentBreakerId),
                    status = MatchStatus.IN_PROGRESS,
                    history = withoutLast,
                )
            }
            MatchEventType.FOUL -> {
                val revertedFouls = when (last.playerId) {
                    match.player1.id -> match.copy(foul1 = (match.foul1 - 1).coerceAtLeast(0))
                    else -> match.copy(foul2 = (match.foul2 - 1).coerceAtLeast(0))
                }
                revertedFouls.copy(history = withoutLast)
            }
        }
    }

    private fun awardRack(match: Match, playerId: PlayerId, type: MatchEventType): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        requireKnownPlayer(match, playerId)
        val nextScore1 = if (playerId == match.player1.id) match.score1 + 1 else match.score1
        val nextScore2 = if (playerId == match.player2.id) match.score2 + 1 else match.score2
        val completed =
            nextScore1 >= match.racksToWin || nextScore2 >= match.racksToWin
        return match.copy(
            score1 = nextScore1,
            score2 = nextScore2,
            currentBreakerId = match.otherPlayerId(match.currentBreakerId),
            status = if (completed) MatchStatus.COMPLETED else MatchStatus.IN_PROGRESS,
            history = match.history + MatchEvent(type, playerId),
        )
    }

    private fun requireKnownPlayer(match: Match, playerId: PlayerId) {
        require(playerId == match.player1.id || playerId == match.player2.id) {
            "Unknown player: $playerId"
        }
    }
}
