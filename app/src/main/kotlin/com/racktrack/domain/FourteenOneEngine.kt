package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEvent
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PlayerId

/**
 * Pure 14/1 continuous rules (FFB arts 1.6.01–1.6.07) for the points scoreboard.
 */
object FourteenOneEngine {
    const val CONSECUTIVE_FOULS_TO_PENALTY = 3
    private const val CLASSIC_FOUL_PENALTY = -1
    private const val BREAK_FOUL_PENALTY = -2
    private const val THREE_FOUL_EXTRA_PENALTY = -15
    private const val OVERTIME_INNINGS = 5

    fun addPoints(match: Match, playerId: PlayerId, points: Int, nowMillis: Long): Match {
        if (!isActiveFourteenOne(match) || points <= 0) return match
        if (playerId != match.currentShooterId) return match

        val nextScore1 = if (playerId == match.player1.id) match.score1 + points else match.score1
        val nextScore2 = if (playerId == match.player2.id) match.score2 + points else match.score2
        val withPoints = match.copy(
            score1 = nextScore1,
            score2 = nextScore2,
            currentRun = match.currentRun + points,
            foul1 = if (playerId == match.player1.id) 0 else match.foul1,
            foul2 = if (playerId == match.player2.id) 0 else match.foul2,
            objectBallsOnTable = reduceObjectBalls(match.objectBallsOnTable, points),
            awaitingOpeningBreak = false,
            history = match.history + MatchEvent(
                MatchEventType.POINTS,
                playerId,
                nowMillis,
                value = points,
            ),
        )
        return finishIfDistanceReached(withPoints)
    }

    fun pass(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (!isActiveFourteenOne(match)) return match
        if (playerId != match.currentShooterId) return match

        // Legal end of turn resets consecutive fouls (FFB 1.6.07).
        val afterRun = commitCurrentRun(match, playerId).copy(
            foul1 = if (playerId == match.player1.id) 0 else match.foul1,
            foul2 = if (playerId == match.player2.id) 0 else match.foul2,
        )
        val ended = endInning(
            match = afterRun,
            playerId = playerId,
            nextShooterId = match.otherPlayerId(playerId),
            awaitingOpeningBreak = false,
            historyEvent = MatchEvent(MatchEventType.PASS, playerId, nowMillis),
        )
        return resolveInningsLimit(ended)
    }

    /**
     * Classic foul: −1, consecutive foul++, end inning, hand to opponent
     * (unless 3rd consecutive → extra −15 and fouler must re-break).
     */
    fun foul(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (!isActiveFourteenOne(match)) return match
        if (playerId != match.currentShooterId) return match

        val afterRun = commitCurrentRun(match, playerId)
        val nextFoul = afterRun.foulsFor(playerId) + 1
        val scored = applyScoreDelta(afterRun, playerId, CLASSIC_FOUL_PENALTY)
        val withFoulEvent = scored.copy(
            foul1 = if (playerId == match.player1.id) nextFoul else afterRun.foul1,
            foul2 = if (playerId == match.player2.id) nextFoul else afterRun.foul2,
            history = scored.history + MatchEvent(
                MatchEventType.FOUL,
                playerId,
                nowMillis,
                value = CLASSIC_FOUL_PENALTY,
            ),
        )

        return if (nextFoul >= CONSECUTIVE_FOULS_TO_PENALTY) {
            val withExtra = applyScoreDelta(withFoulEvent, playerId, THREE_FOUL_EXTRA_PENALTY)
                .copy(
                    foul1 = if (playerId == match.player1.id) 0 else withFoulEvent.foul1,
                    foul2 = if (playerId == match.player2.id) 0 else withFoulEvent.foul2,
                    history = withFoulEvent.history + MatchEvent(
                        MatchEventType.THREE_FOUL_PENALTY,
                        playerId,
                        nowMillis,
                        value = THREE_FOUL_EXTRA_PENALTY,
                    ),
                )
            val ended = endInning(
                match = withExtra,
                playerId = playerId,
                nextShooterId = playerId,
                awaitingOpeningBreak = true,
                historyEvent = null,
            )
            resolveInningsLimit(ended)
        } else {
            val ended = endInning(
                match = withFoulEvent,
                playerId = playerId,
                nextShooterId = match.otherPlayerId(playerId),
                awaitingOpeningBreak = false,
                historyEvent = null,
            )
            resolveInningsLimit(ended)
        }
    }

    /** Illegal opening break (FFB 1.6.03): −2, hand to opponent (accept table). */
    fun breakFoul(match: Match, playerId: PlayerId, nowMillis: Long): Match {
        if (!isActiveFourteenOne(match)) return match
        if (playerId != match.currentShooterId) return match

        val afterRun = commitCurrentRun(match, playerId)
        val scored = applyScoreDelta(afterRun, playerId, BREAK_FOUL_PENALTY)
        val withEvent = scored.copy(
            foul1 = if (playerId == match.player1.id) 0 else afterRun.foul1,
            foul2 = if (playerId == match.player2.id) 0 else afterRun.foul2,
            history = scored.history + MatchEvent(
                MatchEventType.BREAK_FOUL,
                playerId,
                nowMillis,
                value = BREAK_FOUL_PENALTY,
            ),
        )
        val ended = endInning(
            match = withEvent,
            playerId = playerId,
            nextShooterId = match.otherPlayerId(playerId),
            awaitingOpeningBreak = false,
            historyEvent = null,
        )
        return resolveInningsLimit(ended)
    }

    fun undoLast(match: Match): Match {
        if (match.gameMode != GameMode.FOURTEEN_ONE) return match
        val last = match.history.lastOrNull() ?: return match
        val withoutLast = match.history.dropLast(1)

        return when (last.type) {
            MatchEventType.POINTS -> {
                val points = last.value
                val reverted = when (last.playerId) {
                    match.player1.id -> match.copy(
                        score1 = match.score1 - points,
                        currentRun = (match.currentRun - points).coerceAtLeast(0),
                    )
                    else -> match.copy(
                        score2 = match.score2 - points,
                        currentRun = (match.currentRun - points).coerceAtLeast(0),
                    )
                }
                reverted.copy(
                    foul1 = consecutiveFoulsFromHistory(withoutLast, match.player1.id),
                    foul2 = consecutiveFoulsFromHistory(withoutLast, match.player2.id),
                    objectBallsOnTable = objectBallsFromHistory(withoutLast),
                    awaitingOpeningBreak = awaitingOpeningBreakFromHistory(withoutLast),
                    status = MatchStatus.IN_PROGRESS,
                    history = withoutLast,
                )
            }
            MatchEventType.PASS,
            MatchEventType.BREAK_FOUL,
            -> {
                val scoreRestored = when (last.type) {
                    MatchEventType.PASS -> match
                    else -> applyScoreDelta(match, last.playerId, -last.value)
                }
                undoEndedInning(scoreRestored, withoutLast, last.playerId)
            }
            MatchEventType.FOUL -> {
                val scoreRestored = applyScoreDelta(match, last.playerId, -last.value)
                if (match.currentShooterId == last.playerId) {
                    // After undoing THREE_FOUL_PENALTY: inning already rolled back.
                    scoreRestored.copy(
                        foul1 = consecutiveFoulsFromHistory(withoutLast, match.player1.id),
                        foul2 = consecutiveFoulsFromHistory(withoutLast, match.player2.id),
                        highRun1 = highRunFromHistory(withoutLast, match.player1.id),
                        highRun2 = highRunFromHistory(withoutLast, match.player2.id),
                        currentRun = currentRunFromHistory(withoutLast, last.playerId),
                        objectBallsOnTable = objectBallsFromHistory(withoutLast),
                        awaitingOpeningBreak = awaitingOpeningBreakFromHistory(withoutLast),
                        status = MatchStatus.IN_PROGRESS,
                        history = withoutLast,
                    )
                } else {
                    undoEndedInning(scoreRestored, withoutLast, last.playerId)
                }
            }
            MatchEventType.THREE_FOUL_PENALTY -> {
                // Reverse −15 and the inning / re-break state; FOUL remains last for next undo.
                val scoreRestored = applyScoreDelta(match, last.playerId, -last.value)
                scoreRestored.copy(
                    innings1 = if (last.playerId == match.player1.id) {
                        (match.innings1 - 1).coerceAtLeast(0)
                    } else {
                        match.innings1
                    },
                    innings2 = if (last.playerId == match.player2.id) {
                        (match.innings2 - 1).coerceAtLeast(0)
                    } else {
                        match.innings2
                    },
                    currentShooterId = last.playerId,
                    currentBreakerId = last.playerId,
                    awaitingOpeningBreak = false,
                    currentRun = 0,
                    highRun1 = highRunFromHistory(withoutLast, match.player1.id),
                    highRun2 = highRunFromHistory(withoutLast, match.player2.id),
                    foul1 = consecutiveFoulsFromHistory(withoutLast, match.player1.id),
                    foul2 = consecutiveFoulsFromHistory(withoutLast, match.player2.id),
                    objectBallsOnTable = objectBallsFromHistory(withoutLast),
                    inningsLimit = effectiveInningsLimit(match.inningsLimitBase, withoutLast, match),
                    status = MatchStatus.IN_PROGRESS,
                    history = withoutLast,
                )
            }
            else -> match
        }
    }

    private fun undoEndedInning(
        match: Match,
        withoutLast: List<MatchEvent>,
        playerId: PlayerId,
    ): Match =
        match.copy(
            innings1 = if (playerId == match.player1.id) {
                (match.innings1 - 1).coerceAtLeast(0)
            } else {
                match.innings1
            },
            innings2 = if (playerId == match.player2.id) {
                (match.innings2 - 1).coerceAtLeast(0)
            } else {
                match.innings2
            },
            currentShooterId = playerId,
            currentBreakerId = playerId,
            currentRun = currentRunFromHistory(withoutLast, playerId),
            highRun1 = highRunFromHistory(withoutLast, match.player1.id),
            highRun2 = highRunFromHistory(withoutLast, match.player2.id),
            foul1 = consecutiveFoulsFromHistory(withoutLast, match.player1.id),
            foul2 = consecutiveFoulsFromHistory(withoutLast, match.player2.id),
            awaitingOpeningBreak = awaitingOpeningBreakFromHistory(withoutLast),
            objectBallsOnTable = objectBallsFromHistory(withoutLast),
            inningsLimit = effectiveInningsLimit(match.inningsLimitBase, withoutLast, match),
            status = MatchStatus.IN_PROGRESS,
            history = withoutLast,
        )

    private fun commitCurrentRun(match: Match, playerId: PlayerId): Match {
        val run = match.currentRun
        return when (playerId) {
            match.player1.id -> match.copy(
                highRun1 = maxOf(match.highRun1, run),
                currentRun = 0,
            )
            else -> match.copy(
                highRun2 = maxOf(match.highRun2, run),
                currentRun = 0,
            )
        }
    }

    private fun endInning(
        match: Match,
        playerId: PlayerId,
        nextShooterId: PlayerId,
        awaitingOpeningBreak: Boolean,
        historyEvent: MatchEvent?,
    ): Match {
        val nextInnings1 = if (playerId == match.player1.id) match.innings1 + 1 else match.innings1
        val nextInnings2 = if (playerId == match.player2.id) match.innings2 + 1 else match.innings2
        return match.copy(
            innings1 = nextInnings1,
            innings2 = nextInnings2,
            currentShooterId = nextShooterId,
            currentBreakerId = nextShooterId,
            awaitingOpeningBreak = awaitingOpeningBreak,
            objectBallsOnTable = if (awaitingOpeningBreak) {
                Match.OBJECT_BALLS_FULL_RACK
            } else {
                match.objectBallsOnTable
            },
            currentRun = 0,
            history = if (historyEvent != null) match.history + historyEvent else match.history,
        )
    }

    /**
     * Pocket [points] object balls with FFB continuous re-rack:
     * when only the keyball remains (1), the fourteen are re-racked → 15 on table again.
     * There is no “pocket through 0” cycle — re-rack happens at 1, not after 15 points.
     */
    internal fun reduceObjectBalls(current: Int, points: Int): Int {
        var balls = current
        repeat(points) {
            balls -= 1
            if (balls <= 1) {
                balls = Match.OBJECT_BALLS_FULL_RACK
            }
        }
        return balls
    }

    private fun objectBallsFromHistory(history: List<MatchEvent>): Int {
        var balls = Match.OBJECT_BALLS_FULL_RACK
        for (event in history) {
            when (event.type) {
                MatchEventType.THREE_FOUL_PENALTY -> balls = Match.OBJECT_BALLS_FULL_RACK
                MatchEventType.POINTS -> balls = reduceObjectBalls(balls, event.value)
                else -> Unit
            }
        }
        return balls
    }

    private fun applyScoreDelta(match: Match, playerId: PlayerId, delta: Int): Match =
        when (playerId) {
            match.player1.id -> match.copy(score1 = match.score1 + delta)
            else -> match.copy(score2 = match.score2 + delta)
        }

    private fun finishIfDistanceReached(match: Match): Match {
        if (match.score1 >= match.pointsToWin || match.score2 >= match.pointsToWin) {
            return match.copy(status = MatchStatus.COMPLETED)
        }
        return match
    }

    private fun resolveInningsLimit(match: Match): Match {
        val limit = match.inningsLimit ?: return finishIfDistanceReached(match)
        if (match.innings1 < limit || match.innings2 < limit) {
            return finishIfDistanceReached(match)
        }
        return when {
            match.score1 == match.score2 -> match.copy(
                inningsLimit = limit + OVERTIME_INNINGS,
                status = MatchStatus.IN_PROGRESS,
            )
            else -> match.copy(status = MatchStatus.COMPLETED)
        }
    }

    private fun isActiveFourteenOne(match: Match): Boolean =
        match.gameMode == GameMode.FOURTEEN_ONE && match.status == MatchStatus.IN_PROGRESS

    private fun consecutiveFoulsFromHistory(
        history: List<MatchEvent>,
        playerId: PlayerId,
    ): Int {
        var count = 0
        for (event in history.asReversed()) {
            when (event.type) {
                MatchEventType.POINTS,
                MatchEventType.PASS,
                MatchEventType.BREAK_FOUL,
                MatchEventType.THREE_FOUL_PENALTY,
                -> return count
                MatchEventType.FOUL ->
                    if (event.playerId == playerId) count++ else return count
                else -> Unit
            }
        }
        return count
    }

    private fun awaitingOpeningBreakFromHistory(history: List<MatchEvent>): Boolean {
        if (history.isEmpty()) return true
        for (event in history.asReversed()) {
            when (event.type) {
                MatchEventType.THREE_FOUL_PENALTY -> return true
                MatchEventType.POINTS -> return false
                MatchEventType.PASS,
                MatchEventType.FOUL,
                MatchEventType.BREAK_FOUL,
                -> return false
                else -> Unit
            }
        }
        return true
    }

    private fun currentRunFromHistory(history: List<MatchEvent>, playerId: PlayerId): Int {
        var run = 0
        for (event in history.asReversed()) {
            when (event.type) {
                MatchEventType.POINTS ->
                    if (event.playerId == playerId) run += event.value else return run
                MatchEventType.PASS,
                MatchEventType.FOUL,
                MatchEventType.BREAK_FOUL,
                MatchEventType.THREE_FOUL_PENALTY,
                -> return run
                else -> Unit
            }
        }
        return run
    }

    private fun highRunFromHistory(history: List<MatchEvent>, playerId: PlayerId): Int {
        var best = 0
        var run = 0
        for (event in history) {
            when (event.type) {
                MatchEventType.POINTS -> if (event.playerId == playerId) run += event.value
                MatchEventType.PASS,
                MatchEventType.FOUL,
                MatchEventType.BREAK_FOUL,
                MatchEventType.THREE_FOUL_PENALTY,
                -> if (event.playerId == playerId) {
                    best = maxOf(best, run)
                    run = 0
                }
                else -> Unit
            }
        }
        return maxOf(best, run)
    }

    /** Recompute effective innings limit (base + overtime extensions) from history. */
    private fun effectiveInningsLimit(
        base: Int?,
        history: List<MatchEvent>,
        match: Match,
    ): Int? {
        if (base == null) return null
        var limit = base
        var i1 = 0
        var i2 = 0
        var score1 = 0
        var score2 = 0
        for (event in history) {
            when (event.type) {
                MatchEventType.POINTS ->
                    if (event.playerId == match.player1.id) {
                        score1 += event.value
                    } else {
                        score2 += event.value
                    }
                MatchEventType.PASS -> {
                    if (event.playerId == match.player1.id) i1++ else i2++
                    if (i1 >= limit && i2 >= limit && score1 == score2) {
                        limit += OVERTIME_INNINGS
                    }
                }
                MatchEventType.FOUL,
                MatchEventType.BREAK_FOUL,
                -> {
                    if (event.playerId == match.player1.id) {
                        score1 += event.value
                        i1++
                    } else {
                        score2 += event.value
                        i2++
                    }
                    if (i1 >= limit && i2 >= limit && score1 == score2) {
                        limit += OVERTIME_INNINGS
                    }
                }
                MatchEventType.THREE_FOUL_PENALTY ->
                    if (event.playerId == match.player1.id) {
                        score1 += event.value
                    } else {
                        score2 += event.value
                    }
                else -> Unit
            }
        }
        return limit
    }
}
