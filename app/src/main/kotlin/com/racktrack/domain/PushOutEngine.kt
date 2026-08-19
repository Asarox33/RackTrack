package com.racktrack.domain

import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEvent
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PlayerId
import com.racktrack.domain.model.PushOutPhase

/** 9/10-ball push-out decision tree (FFB) for the race scoreboard. */
object PushOutEngine {
    fun canAnnounce(match: Match, playerId: PlayerId): Boolean =
        match.status != MatchStatus.COMPLETED &&
            match.gameMode.supportsPushOut &&
            match.pushOutPhase == PushOutPhase.AVAILABLE &&
            playerId == match.currentShooterId

    fun announce(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (!canAnnounce(match, playerId)) return match
        return match.copy(
            pushOutPhase = PushOutPhase.ANNOUNCED,
            history = match.history + MatchEvent(MatchEventType.PUSH_OUT, playerId, nowMillis),
        )
    }

    fun resolveClean(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (match.pushOutPhase != PushOutPhase.ANNOUNCED) return match
        if (playerId != match.currentShooterId) return match
        return match.copy(
            pushOutPhase = PushOutPhase.AWAITING_CHOICE,
            history = match.history + MatchEvent(MatchEventType.PUSH_OUT_CLEAN, playerId, nowMillis),
        )
    }

    /**
     * Foul on the push-out shot — opponent gets the table (BIH anywhere).
     * Counts toward consecutive fouls / three-foul rack loss.
     */
    fun resolveFoul(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (match.pushOutPhase != PushOutPhase.ANNOUNCED) return match
        if (playerId != match.currentShooterId) return match

        val nextFoul1 = if (playerId == match.player1.id) match.foul1 + 1 else match.foul1
        val nextFoul2 = if (playerId == match.player2.id) match.foul2 + 1 else match.foul2
        val consecutive = if (playerId == match.player1.id) nextFoul1 else nextFoul2
        val threeFoulLossApplies =
            match.gameMode.supportsThreeFoulRackLoss &&
                consecutive >= MatchEngine.CONSECUTIVE_FOULS_TO_LOSE_RACK

        if (threeFoulLossApplies) {
            return MatchEngine.awardRackToOpponent(
                match = match.copy(pushOutPhase = PushOutPhase.NONE),
                loserId = playerId,
                winnerId = match.otherPlayerId(playerId),
                type = MatchEventType.THREE_FOULS_LOSS,
                nowMillis = nowMillis,
            )
        }

        return match.copy(
            foul1 = nextFoul1,
            foul2 = nextFoul2,
            pushOutPhase = PushOutPhase.NONE,
            currentShooterId = match.otherPlayerId(playerId),
            history = match.history + MatchEvent(MatchEventType.PUSH_OUT_FOUL, playerId, nowMillis),
        )
    }

    fun take(match: Match, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (match.pushOutPhase != PushOutPhase.AWAITING_CHOICE) return match
        val announcer = announcerId(match) ?: return match
        val opponent = match.otherPlayerId(announcer)
        return match.copy(
            pushOutPhase = PushOutPhase.NONE,
            currentShooterId = opponent,
            history = match.history + MatchEvent(MatchEventType.PUSH_OUT_TAKE, opponent, nowMillis),
        )
    }

    fun giveBack(match: Match, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (match.pushOutPhase != PushOutPhase.AWAITING_CHOICE) return match
        val announcer = announcerId(match) ?: return match
        val opponent = match.otherPlayerId(announcer)
        return match.copy(
            pushOutPhase = PushOutPhase.NONE,
            currentShooterId = announcer,
            history = match.history + MatchEvent(MatchEventType.PUSH_OUT_RETURN, opponent, nowMillis),
        )
    }

    fun phaseAfterRack(supportsPushOut: Boolean, completed: Boolean): PushOutPhase =
        if (!completed && supportsPushOut) PushOutPhase.AVAILABLE else PushOutPhase.NONE

    fun phaseFromHistory(match: Match, history: List<MatchEvent>): PushOutPhase {
        if (!match.gameMode.supportsPushOut) return PushOutPhase.NONE
        var phase = PushOutPhase.AVAILABLE
        for (event in history) {
            if (event.type in MatchEngine.RACK_ENDING_TYPES) {
                phase = PushOutPhase.AVAILABLE
                continue
            }
            when (event.type) {
                MatchEventType.DRY_BREAK,
                MatchEventType.FOULS_CLEARED,
                -> Unit
                MatchEventType.FOUL -> phase = PushOutPhase.NONE
                MatchEventType.PUSH_OUT -> phase = PushOutPhase.ANNOUNCED
                MatchEventType.PUSH_OUT_CLEAN -> phase = PushOutPhase.AWAITING_CHOICE
                MatchEventType.PUSH_OUT_FOUL,
                MatchEventType.PUSH_OUT_TAKE,
                MatchEventType.PUSH_OUT_RETURN,
                -> phase = PushOutPhase.NONE
                else -> Unit
            }
        }
        return phase
    }

    fun undo(match: Match, last: MatchEvent, withoutLast: List<MatchEvent>): Match? =
        when (last.type) {
            MatchEventType.PUSH_OUT -> match.copy(
                pushOutPhase = PushOutPhase.AVAILABLE,
                history = withoutLast,
            )
            MatchEventType.PUSH_OUT_CLEAN -> match.copy(
                pushOutPhase = PushOutPhase.ANNOUNCED,
                history = withoutLast,
            )
            MatchEventType.PUSH_OUT_FOUL -> {
                val revertedFouls = when (last.playerId) {
                    match.player1.id -> match.copy(foul1 = (match.foul1 - 1).coerceAtLeast(0))
                    else -> match.copy(foul2 = (match.foul2 - 1).coerceAtLeast(0))
                }
                revertedFouls.copy(
                    pushOutPhase = PushOutPhase.ANNOUNCED,
                    currentShooterId = last.playerId,
                    history = withoutLast,
                )
            }
            MatchEventType.PUSH_OUT_TAKE,
            MatchEventType.PUSH_OUT_RETURN,
            -> {
                val announcer = match.otherPlayerId(last.playerId)
                match.copy(
                    pushOutPhase = PushOutPhase.AWAITING_CHOICE,
                    currentShooterId = announcer,
                    history = withoutLast,
                )
            }
            else -> null
        }

    private fun announcerId(match: Match): PlayerId? {
        for (event in match.history.asReversed()) {
            when (event.type) {
                MatchEventType.PLUS_ONE,
                MatchEventType.RUN_OUT,
                MatchEventType.GOLDEN_BREAK,
                MatchEventType.THREE_FOULS_LOSS,
                MatchEventType.EIGHT_BALL_LOSS,
                -> return null
                MatchEventType.PUSH_OUT -> return event.playerId
                else -> Unit
            }
        }
        return null
    }
}
