package com.racktrack.domain

import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEvent
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PlayerId
import com.racktrack.domain.model.PushOutPhase

/** Pure rules for race scoreboard modes — no Android dependencies. */
object MatchEngine {
    const val CONSECUTIVE_FOULS_TO_LOSE_RACK = 3

    internal val RACK_ENDING_TYPES = setOf(
        MatchEventType.PLUS_ONE,
        MatchEventType.RUN_OUT,
        MatchEventType.GOLDEN_BREAK,
        MatchEventType.THREE_FOULS_LOSS,
        MatchEventType.EIGHT_BALL_LOSS,
    )

    fun recordPlusOne(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.pushOutPhase == PushOutPhase.ANNOUNCED ||
            match.pushOutPhase == PushOutPhase.AWAITING_CHOICE
        ) {
            return match
        }
        return awardRack(match, playerId, MatchEventType.PLUS_ONE, nowMillis)
    }

    fun recordRunOut(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.pushOutPhase == PushOutPhase.ANNOUNCED ||
            match.pushOutPhase == PushOutPhase.AWAITING_CHOICE
        ) {
            return match
        }
        if (!canBreakAndClear(match, playerId)) return match

        val awarded = awardRack(match, playerId, MatchEventType.RUN_OUT, nowMillis)
        if (awarded === match) return match
        return when (playerId) {
            match.player1.id -> awarded.copy(runOut1 = match.runOut1 + 1)
            else -> awarded.copy(runOut2 = match.runOut2 + 1)
        }
    }

    /**
     * 9-ball — legal break that pockets the 9 (FFB art. 1.4.03: casse régulière + 9 empochée).
     * Not available in 8/10-ball (8 on break is respotted or re-racked — art. 1.3.03;
     * 10 pocketed early is respotted — art. 1.5.06).
     */
    fun recordGoldenBreak(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.pushOutPhase == PushOutPhase.ANNOUNCED ||
            match.pushOutPhase == PushOutPhase.AWAITING_CHOICE
        ) {
            return match
        }
        if (!match.gameMode.supportsGoldenBreak) return match
        if (!canBreakAndClear(match, playerId)) return match

        val awarded = awardRack(match, playerId, MatchEventType.GOLDEN_BREAK, nowMillis)
        if (awarded === match) return match
        return when (playerId) {
            match.player1.id -> awarded.copy(goldenBreak1 = match.goldenBreak1 + 1)
            else -> awarded.copy(goldenBreak2 = match.goldenBreak2 + 1)
        }
    }

    /**
     * Break with no object ball pocketed (stat only; rack continues).
     * Distinct from a break foul (use [recordFoul]).
     */
    fun recordDryBreak(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (!match.gameMode.supportsDryBreak) return match
        requireKnownPlayer(match, playerId)
        if (playerId != match.currentBreakerId) return match
        if (match.foulsFor(playerId) != 0) return match
        if (hasDryBreakThisRack(match, playerId)) return match

        return when (playerId) {
            match.player1.id -> match.copy(
                dryBreak1 = match.dryBreak1 + 1,
                // Dry is a legal break — push-out stays available (FFB 9-ball).
                history = match.history + MatchEvent(MatchEventType.DRY_BREAK, playerId, nowMillis),
            )
            else -> match.copy(
                dryBreak2 = match.dryBreak2 + 1,
                history = match.history + MatchEvent(MatchEventType.DRY_BREAK, playerId, nowMillis),
            )
        }
    }

    /**
     * 8-ball — [loserId] loses the rack under FFB art. 1.3.06
     * (early 8, 8 off table except on break, 8 with foul, wrong pocket on 8).
     */
    fun recordEightBallLoss(match: Match, loserId: PlayerId, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (!match.gameMode.supportsEightBallLoss) return match
        requireKnownPlayer(match, loserId)

        val winnerId = match.otherPlayerId(loserId)
        val awarded = awardRackToOpponent(
            match = match,
            loserId = loserId,
            winnerId = winnerId,
            type = MatchEventType.EIGHT_BALL_LOSS,
            nowMillis = nowMillis,
        )
        return when (loserId) {
            match.player1.id -> awarded.copy(eightBallLoss1 = match.eightBallLoss1 + 1)
            else -> awarded.copy(eightBallLoss2 = match.eightBallLoss2 + 1)
        }
    }

    /**
     * Clears consecutive fouls for [playerId] after a legal shot (9/10-ball only).
     * Undoable via [MatchEventType.FOULS_CLEARED] in history. No-op for 8-ball / 14.1.
     * Does not remove fouls from match totals; break actions (Golden / Dry) stay blocked
     * for the rest of the rack.
     */
    fun clearConsecutiveFouls(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        if (!match.gameMode.supportsThreeFoulRackLoss) return match
        requireKnownPlayer(match, playerId)
        if (match.foulsFor(playerId) == 0) return match
        return match.copy(
            foul1 = if (playerId == match.player1.id) 0 else match.foul1,
            foul2 = if (playerId == match.player2.id) 0 else match.foul2,
            history = match.history + MatchEvent(MatchEventType.FOULS_CLEARED, playerId, nowMillis),
        )
    }

    /**
     * Records a foul for [playerId].
     * Consecutive fouls are tracked per player; a legal rack award resets them.
     * On the 3rd consecutive foul the fouling player loses the rack (FFB 9/10-ball only).
     */
    fun recordFoul(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        requireKnownPlayer(match, playerId)
        val nextFoul1 = if (playerId == match.player1.id) match.foul1 + 1 else match.foul1
        val nextFoul2 = if (playerId == match.player2.id) match.foul2 + 1 else match.foul2
        val consecutive = if (playerId == match.player1.id) nextFoul1 else nextFoul2

        val threeFoulLossApplies =
            match.gameMode.supportsThreeFoulRackLoss &&
                consecutive >= CONSECUTIVE_FOULS_TO_LOSE_RACK

        if (!threeFoulLossApplies) {
            return match.copy(
                foul1 = nextFoul1,
                foul2 = nextFoul2,
                pushOutPhase = if (match.pushOutPhase == PushOutPhase.AVAILABLE) {
                    PushOutPhase.NONE
                } else {
                    match.pushOutPhase
                },
                history = match.history + MatchEvent(MatchEventType.FOUL, playerId, nowMillis),
            )
        }

        val winnerId = match.otherPlayerId(playerId)
        return awardRackToOpponent(
            match = match,
            loserId = playerId,
            winnerId = winnerId,
            type = MatchEventType.THREE_FOULS_LOSS,
            nowMillis = nowMillis,
        )
    }

    fun canAnnouncePushOut(match: Match, playerId: PlayerId): Boolean =
        PushOutEngine.canAnnounce(match, playerId)

    fun announcePushOut(match: Match, playerId: PlayerId, nowMillis: Long): Match =
        PushOutEngine.announce(match, playerId, nowMillis)

    fun resolvePushOutClean(match: Match, playerId: PlayerId, nowMillis: Long): Match =
        PushOutEngine.resolveClean(match, playerId, nowMillis)

    fun resolvePushOutFoul(match: Match, playerId: PlayerId, nowMillis: Long): Match =
        PushOutEngine.resolveFoul(match, playerId, nowMillis)

    fun takePushOut(match: Match, nowMillis: Long): Match =
        PushOutEngine.take(match, nowMillis)

    fun returnPushOut(match: Match, nowMillis: Long): Match =
        PushOutEngine.giveBack(match, nowMillis)

    fun undoLast(match: Match): Match {
        val last = match.history.lastOrNull() ?: return match
        val withoutLast = match.history.dropLast(1)
        PushOutEngine.undo(match, last, withoutLast)?.let { return it }
        return when (last.type) {
            MatchEventType.POINTS,
            MatchEventType.PASS,
            MatchEventType.BREAK_FOUL,
            MatchEventType.ACCEPT_ILLEGAL_OPEN,
            MatchEventType.THREE_FOUL_PENALTY,
            -> match
            MatchEventType.FOULS_CLEARED -> match.copy(
                foul1 = consecutiveFoulsFromHistory(withoutLast, match.player1.id),
                foul2 = consecutiveFoulsFromHistory(withoutLast, match.player2.id),
                history = withoutLast,
            )
            MatchEventType.PLUS_ONE,
            MatchEventType.RUN_OUT,
            MatchEventType.GOLDEN_BREAK,
            -> undoRackWin(match, last, withoutLast)
            MatchEventType.THREE_FOULS_LOSS,
            MatchEventType.EIGHT_BALL_LOSS,
            -> undoRackLoss(match, last, withoutLast)
            MatchEventType.FOUL -> {
                val revertedFouls = when (last.playerId) {
                    match.player1.id -> match.copy(foul1 = (match.foul1 - 1).coerceAtLeast(0))
                    else -> match.copy(foul2 = (match.foul2 - 1).coerceAtLeast(0))
                }
                revertedFouls.copy(
                    pushOutPhase = PushOutEngine.phaseFromHistory(match, withoutLast),
                    history = withoutLast,
                )
            }
            MatchEventType.DRY_BREAK -> {
                val reverted = when (last.playerId) {
                    match.player1.id -> match.copy(dryBreak1 = (match.dryBreak1 - 1).coerceAtLeast(0))
                    else -> match.copy(dryBreak2 = (match.dryBreak2 - 1).coerceAtLeast(0))
                }
                reverted.copy(
                    pushOutPhase = PushOutEngine.phaseFromHistory(match, withoutLast),
                    history = withoutLast,
                )
            }
            else -> match
        }
    }

    private fun undoRackWin(
        match: Match,
        last: MatchEvent,
        withoutLast: List<MatchEvent>,
    ): Match {
        val revertedScores = when (last.playerId) {
            match.player1.id -> match.copy(
                score1 = (match.score1 - 1).coerceAtLeast(0),
                runOut1 = if (last.type == MatchEventType.RUN_OUT) {
                    (match.runOut1 - 1).coerceAtLeast(0)
                } else {
                    match.runOut1
                },
                goldenBreak1 = if (last.type == MatchEventType.GOLDEN_BREAK) {
                    (match.goldenBreak1 - 1).coerceAtLeast(0)
                } else {
                    match.goldenBreak1
                },
            )
            else -> match.copy(
                score2 = (match.score2 - 1).coerceAtLeast(0),
                runOut2 = if (last.type == MatchEventType.RUN_OUT) {
                    (match.runOut2 - 1).coerceAtLeast(0)
                } else {
                    match.runOut2
                },
                goldenBreak2 = if (last.type == MatchEventType.GOLDEN_BREAK) {
                    (match.goldenBreak2 - 1).coerceAtLeast(0)
                } else {
                    match.goldenBreak2
                },
            )
        }
        return restoreAfterRackUndo(match, revertedScores, withoutLast)
    }

    private fun undoRackLoss(
        match: Match,
        last: MatchEvent,
        withoutLast: List<MatchEvent>,
    ): Match {
        val winnerId = match.otherPlayerId(last.playerId)
        val revertedScores = when (winnerId) {
            match.player1.id -> match.copy(score1 = (match.score1 - 1).coerceAtLeast(0))
            else -> match.copy(score2 = (match.score2 - 1).coerceAtLeast(0))
        }
        val withLossCounter = if (last.type == MatchEventType.EIGHT_BALL_LOSS) {
            when (last.playerId) {
                match.player1.id -> revertedScores.copy(
                    eightBallLoss1 = (match.eightBallLoss1 - 1).coerceAtLeast(0),
                )
                else -> revertedScores.copy(
                    eightBallLoss2 = (match.eightBallLoss2 - 1).coerceAtLeast(0),
                )
            }
        } else {
            revertedScores
        }
        return restoreAfterRackUndo(match, withLossCounter, withoutLast)
    }

    private fun restoreAfterRackUndo(
        match: Match,
        reverted: Match,
        withoutLast: List<MatchEvent>,
    ): Match {
        val breaker = breakerFromHistory(match, withoutLast)
        return reverted.copy(
            foul1 = consecutiveFoulsFromHistory(withoutLast, match.player1.id),
            foul2 = consecutiveFoulsFromHistory(withoutLast, match.player2.id),
            currentBreakerId = breaker,
            currentShooterId = shooterFromHistory(match, withoutLast, breaker),
            pushOutPhase = PushOutEngine.phaseFromHistory(match, withoutLast),
            status = MatchStatus.IN_PROGRESS,
            history = withoutLast,
        )
    }

    fun canBreakAndClear(match: Match, playerId: PlayerId): Boolean =
        match.status != MatchStatus.COMPLETED &&
            playerId == match.currentBreakerId &&
            !hasFoulCommittedThisRack(match, playerId) &&
            !hasDryBreakThisRack(match, playerId)

    fun canRecordDryBreak(match: Match, playerId: PlayerId): Boolean =
        match.status != MatchStatus.COMPLETED &&
            match.gameMode.supportsDryBreak &&
            playerId == match.currentBreakerId &&
            !hasFoulCommittedThisRack(match, playerId) &&
            !hasDryBreakThisRack(match, playerId)

    fun canRecordGoldenBreak(match: Match, playerId: PlayerId): Boolean =
        match.gameMode.supportsGoldenBreak && canBreakAndClear(match, playerId)

    fun canRecordEightBallLoss(match: Match, playerId: PlayerId): Boolean =
        match.status != MatchStatus.COMPLETED &&
            match.gameMode.supportsEightBallLoss &&
            (playerId == match.player1.id || playerId == match.player2.id)

    internal fun awardRackToOpponent(
        match: Match,
        loserId: PlayerId,
        winnerId: PlayerId,
        type: MatchEventType,
        nowMillis: Long,
    ): Match {
        val nextScore1 = if (winnerId == match.player1.id) match.score1 + 1 else match.score1
        val nextScore2 = if (winnerId == match.player2.id) match.score2 + 1 else match.score2
        val completed =
            nextScore1 >= match.racksToWin || nextScore2 >= match.racksToWin
        val nextBreaker = nextBreakerAfterRack(match, rackWinnerId = winnerId)
        return match.copy(
            score1 = nextScore1,
            score2 = nextScore2,
            foul1 = 0,
            foul2 = 0,
            currentBreakerId = nextBreaker,
            currentShooterId = nextBreaker,
            pushOutPhase = PushOutEngine.phaseAfterRack(
                supportsPushOut = match.gameMode.supportsPushOut,
                completed = completed,
            ),
            status = if (completed) MatchStatus.COMPLETED else MatchStatus.IN_PROGRESS,
            history = match.history + MatchEvent(type, loserId, nowMillis),
        )
    }

    private fun awardRack(
        match: Match,
        playerId: PlayerId,
        type: MatchEventType,
        nowMillis: Long,
    ): Match {
        if (match.status == MatchStatus.COMPLETED) return match
        requireKnownPlayer(match, playerId)
        val nextScore1 = if (playerId == match.player1.id) match.score1 + 1 else match.score1
        val nextScore2 = if (playerId == match.player2.id) match.score2 + 1 else match.score2
        val completed =
            nextScore1 >= match.racksToWin || nextScore2 >= match.racksToWin
        val nextBreaker = nextBreakerAfterRack(match, rackWinnerId = playerId)
        return match.copy(
            score1 = nextScore1,
            score2 = nextScore2,
            foul1 = 0,
            foul2 = 0,
            currentBreakerId = nextBreaker,
            currentShooterId = nextBreaker,
            pushOutPhase = PushOutEngine.phaseAfterRack(
                supportsPushOut = match.gameMode.supportsPushOut,
                completed = completed,
            ),
            status = if (completed) MatchStatus.COMPLETED else MatchStatus.IN_PROGRESS,
            history = match.history + MatchEvent(type, playerId, nowMillis),
        )
    }

    private fun nextBreakerAfterRack(match: Match, rackWinnerId: PlayerId): PlayerId =
        when (match.breakRule) {
            BreakRule.ALTERNATE -> match.otherPlayerId(match.currentBreakerId)
            BreakRule.WINNER -> rackWinnerId
        }

    /**
     * Rebuilds who should break after [history], from [Match.openingBreakerId] and [BreakRule].
     */
    private fun breakerFromHistory(match: Match, history: List<MatchEvent>): PlayerId {
        var breaker = match.openingBreakerId ?: match.currentBreakerId
        for (event in history) {
            if (event.type !in RACK_ENDING_TYPES) continue
            val winnerId = rackWinnerId(match, event)
            breaker = when (match.breakRule) {
                BreakRule.ALTERNATE -> match.otherPlayerId(breaker)
                BreakRule.WINNER -> winnerId
            }
        }
        return breaker
    }

    private fun rackWinnerId(match: Match, event: MatchEvent): PlayerId =
        when (event.type) {
            MatchEventType.PLUS_ONE,
            MatchEventType.RUN_OUT,
            MatchEventType.GOLDEN_BREAK,
            -> event.playerId
            MatchEventType.THREE_FOULS_LOSS,
            MatchEventType.EIGHT_BALL_LOSS,
            -> match.otherPlayerId(event.playerId)
            else -> error("Not a rack-ending event: ${event.type}")
        }

    /**
     * Consecutive fouls since the last rack-ending event for [playerId].
     * Another player's foul does not reset the counter (only a legal shot / new rack does).
     */
    private fun consecutiveFoulsFromHistory(
        history: List<MatchEvent>,
        playerId: PlayerId,
    ): Int {
        var count = 0
        for (event in history.asReversed()) {
            when (event.type) {
                MatchEventType.PLUS_ONE,
                MatchEventType.RUN_OUT,
                MatchEventType.GOLDEN_BREAK,
                MatchEventType.THREE_FOULS_LOSS,
                MatchEventType.EIGHT_BALL_LOSS,
                MatchEventType.POINTS,
                MatchEventType.PASS,
                MatchEventType.BREAK_FOUL,
                MatchEventType.ACCEPT_ILLEGAL_OPEN,
                MatchEventType.THREE_FOUL_PENALTY,
                -> return count
                MatchEventType.FOULS_CLEARED ->
                    if (event.playerId == playerId) return 0
                MatchEventType.FOUL,
                MatchEventType.PUSH_OUT_FOUL,
                -> if (event.playerId == playerId) count++
                MatchEventType.DRY_BREAK,
                MatchEventType.PUSH_OUT,
                MatchEventType.PUSH_OUT_CLEAN,
                MatchEventType.PUSH_OUT_TAKE,
                MatchEventType.PUSH_OUT_RETURN,
                -> Unit
            }
        }
        return count
    }

    private fun hasDryBreakThisRack(match: Match, playerId: PlayerId): Boolean {
        for (event in match.history.asReversed()) {
            when (event.type) {
                MatchEventType.PLUS_ONE,
                MatchEventType.RUN_OUT,
                MatchEventType.GOLDEN_BREAK,
                MatchEventType.THREE_FOULS_LOSS,
                MatchEventType.EIGHT_BALL_LOSS,
                MatchEventType.POINTS,
                MatchEventType.PASS,
                MatchEventType.BREAK_FOUL,
                MatchEventType.ACCEPT_ILLEGAL_OPEN,
                MatchEventType.THREE_FOUL_PENALTY,
                -> return false
                MatchEventType.DRY_BREAK -> if (event.playerId == playerId) return true
                MatchEventType.FOUL,
                MatchEventType.FOULS_CLEARED,
                MatchEventType.PUSH_OUT,
                MatchEventType.PUSH_OUT_CLEAN,
                MatchEventType.PUSH_OUT_FOUL,
                MatchEventType.PUSH_OUT_TAKE,
                MatchEventType.PUSH_OUT_RETURN,
                -> Unit
            }
        }
        return false
    }

    /**
     * True if [playerId] recorded a foul in the current rack.
     * [MatchEventType.FOULS_CLEARED] resets the consecutive counter for three-foul loss
     * but does **not** revive break actions (Golden / Dry / break-and-run).
     */
    private fun hasFoulCommittedThisRack(match: Match, playerId: PlayerId): Boolean {
        for (event in match.history.asReversed()) {
            when (event.type) {
                MatchEventType.PLUS_ONE,
                MatchEventType.RUN_OUT,
                MatchEventType.GOLDEN_BREAK,
                MatchEventType.THREE_FOULS_LOSS,
                MatchEventType.EIGHT_BALL_LOSS,
                MatchEventType.POINTS,
                MatchEventType.PASS,
                MatchEventType.BREAK_FOUL,
                MatchEventType.ACCEPT_ILLEGAL_OPEN,
                MatchEventType.THREE_FOUL_PENALTY,
                -> return false
                MatchEventType.FOUL,
                MatchEventType.PUSH_OUT_FOUL,
                -> if (event.playerId == playerId) return true
                MatchEventType.DRY_BREAK,
                MatchEventType.FOULS_CLEARED,
                MatchEventType.PUSH_OUT,
                MatchEventType.PUSH_OUT_CLEAN,
                MatchEventType.PUSH_OUT_TAKE,
                MatchEventType.PUSH_OUT_RETURN,
                -> Unit
            }
        }
        return false
    }

    private fun shooterFromHistory(
        match: Match,
        history: List<MatchEvent>,
        breaker: PlayerId,
    ): PlayerId {
        var currentBreaker = match.openingBreakerId ?: breaker
        var shooter = currentBreaker
        for (event in history) {
            when (event.type) {
                MatchEventType.PLUS_ONE,
                MatchEventType.RUN_OUT,
                MatchEventType.GOLDEN_BREAK,
                MatchEventType.THREE_FOULS_LOSS,
                MatchEventType.EIGHT_BALL_LOSS,
                -> {
                    val winnerId = rackWinnerId(match, event)
                    currentBreaker = when (match.breakRule) {
                        BreakRule.ALTERNATE -> match.otherPlayerId(currentBreaker)
                        BreakRule.WINNER -> winnerId
                    }
                    shooter = currentBreaker
                }
                MatchEventType.PUSH_OUT_FOUL -> shooter = match.otherPlayerId(event.playerId)
                MatchEventType.PUSH_OUT_TAKE -> shooter = event.playerId
                MatchEventType.PUSH_OUT_RETURN -> shooter = match.otherPlayerId(event.playerId)
                else -> Unit
            }
        }
        return shooter
    }

    private fun requireKnownPlayer(match: Match, playerId: PlayerId) {
        require(playerId == match.player1.id || playerId == match.player2.id) {
            "Unknown player: $playerId"
        }
    }
}
