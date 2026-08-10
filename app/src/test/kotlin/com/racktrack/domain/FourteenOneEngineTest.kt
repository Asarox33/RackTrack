package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FourteenOneEngineTest {
    private var clock = 2_000_000L

    private fun now(): Long {
        clock += 30_000L
        return clock
    }

    private fun fresh(
        pointsToWin: Int = 50,
        inningsLimit: Int? = 30,
    ): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 1,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = pointsToWin,
            inningsLimit = inningsLimit,
        )

    @Test
    fun `given hand, when add points, then score and run increase and fouls reset`() {
        val match = fresh()
        val fouled = FourteenOneEngine.foul(match, match.player1.id, now())
        // after foul, hand is p2 — give points to p2
        val next = FourteenOneEngine.addPoints(fouled, fouled.player2.id, 5, now())

        assertEquals(5, next.score2)
        assertEquals(5, next.currentRun)
        assertEquals(0, next.foul2)
        assertEquals(MatchEventType.POINTS, next.history.last().type)
    }

    @Test
    fun `given points then pass, then high run and innings update and hand switches`() {
        val match = fresh()
        val scored = FourteenOneEngine.addPoints(match, match.player1.id, 7, now())
        val next = FourteenOneEngine.pass(scored, match.player1.id, now())

        assertEquals(7, next.score1)
        assertEquals(7, next.highRun1)
        assertEquals(0, next.currentRun)
        assertEquals(1, next.innings1)
        assertEquals(match.player2.id, next.currentShooterId)
    }

    @Test
    fun `given foul, then score drops by 1 and hand switches`() {
        val match = fresh()
        val next = FourteenOneEngine.foul(match, match.player1.id, now())

        assertEquals(-1, next.score1)
        assertEquals(1, next.foul1)
        assertEquals(1, next.innings1)
        assertEquals(match.player2.id, next.currentShooterId)
    }

    @Test
    fun `given three consecutive fouls, then extra -15 and fouler must re-break`() {
        var match = fresh()
        // p1 fouls, p2 passes, p1 fouls, p2 passes, p1 fouls 3rd
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())

        // -1 + -1 + -1 + -15 = -18
        assertEquals(-18, match.score1)
        assertEquals(0, match.foul1)
        assertTrue(match.awaitingOpeningBreak)
        assertEquals(match.player1.id, match.currentShooterId)
        assertEquals(MatchEventType.THREE_FOUL_PENALTY, match.history.last().type)
    }

    @Test
    fun `given opening break foul, then -2 and hand switches`() {
        val match = fresh()
        assertTrue(match.awaitingOpeningBreak)

        val next = FourteenOneEngine.breakFoul(match, match.player1.id, now())

        assertEquals(-2, next.score1)
        assertEquals(match.player2.id, next.currentShooterId)
        assertEquals(false, next.awaitingOpeningBreak)
        assertEquals(MatchEventType.BREAK_FOUL, next.history.last().type)
    }

    @Test
    fun `given distance reached, when points added, then match completes`() {
        val match = fresh(pointsToWin = 10, inningsLimit = null)
        val next = FourteenOneEngine.addPoints(match, match.player1.id, 10, now())

        assertEquals(10, next.score1)
        assertEquals(MatchStatus.COMPLETED, next.status)
        assertEquals(match.player1, next.winner)
    }

    @Test
    fun `given innings exhausted with unequal scores, then match completes`() {
        var match = fresh(pointsToWin = 100, inningsLimit = 1)
        match = FourteenOneEngine.addPoints(match, match.player1.id, 3, now())
        match = FourteenOneEngine.pass(match, match.player1.id, now())
        match = FourteenOneEngine.addPoints(match, match.player2.id, 1, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())

        assertEquals(MatchStatus.COMPLETED, match.status)
        assertEquals(match.player1, match.winner)
    }

    @Test
    fun `given innings exhausted with tie, then overtime extends limit by 5`() {
        var match = fresh(pointsToWin = 100, inningsLimit = 1)
        match = FourteenOneEngine.pass(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())

        assertEquals(MatchStatus.IN_PROGRESS, match.status)
        assertEquals(6, match.inningsLimit)
        assertEquals(1, match.inningsLimitBase)
    }

    @Test
    fun `given pass, when undo, then innings and shooter restore`() {
        val match = fresh()
        val passed = FourteenOneEngine.pass(match, match.player1.id, now())
        val undone = FourteenOneEngine.undoLast(passed)

        assertEquals(0, undone.innings1)
        assertEquals(match.player1.id, undone.currentShooterId)
        assertTrue(undone.history.isEmpty())
    }

    @Test
    fun `given three foul penalty, when undo twice, then scores restore stepwise`() {
        var match = fresh()
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())

        assertEquals(-18, match.score1)
        val afterPenalty = FourteenOneEngine.undoLast(match)
        assertEquals(-3, afterPenalty.score1)
        val afterFoul = FourteenOneEngine.undoLast(afterPenalty)
        assertEquals(-2, afterFoul.score1)
    }

    @Test
    fun `given full rack, when plus fourteen, then continuous re-rack restores fifteen`() {
        val match = fresh()
        assertEquals(15, match.objectBallsOnTable)

        val next = FourteenOneEngine.addPoints(match, match.player1.id, 14, now())

        // 14 pocketed → keyball alone → 14 re-racked → 15 again
        assertEquals(15, next.objectBallsOnTable)
    }

    @Test
    fun `given three balls left, when plus thirty-two, then thirteen remain`() {
        val start = fresh().copy(objectBallsOnTable = 3)
        // 2 to keyball/re-rack + 14 + 14 + 2 = 32 → 13 left on the last rack
        val next = FourteenOneEngine.addPoints(start, start.player1.id, 32, now())

        assertEquals(13, next.objectBallsOnTable)
    }

    @Test
    fun `given two balls left, when plus one, then continuous re-rack restores fifteen`() {
        val start = fresh().copy(objectBallsOnTable = 2)
        val next = FourteenOneEngine.addPoints(start, start.player1.id, 1, now())

        assertEquals(15, next.objectBallsOnTable)
    }

    @Test
    fun `given points then pass, object balls on table are unchanged by pass`() {
        val start = fresh()
        val scored = FourteenOneEngine.addPoints(start, start.player1.id, 5, now())
        assertEquals(10, scored.objectBallsOnTable)

        val passed = FourteenOneEngine.pass(scored, scored.player1.id, now())
        assertEquals(10, passed.objectBallsOnTable)
    }

    @Test
    fun `given foul, object balls on table are unchanged`() {
        val start = fresh()
        val match = FourteenOneEngine.addPoints(start, start.player1.id, 3, now())
        val fouled = FourteenOneEngine.foul(match, match.player1.id, now())

        assertEquals(12, fouled.objectBallsOnTable)
    }

    @Test
    fun `given three foul penalty, object balls reset to fifteen for re-break`() {
        var match = fresh()
        match = FourteenOneEngine.addPoints(match, match.player1.id, 4, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        assertEquals(11, match.objectBallsOnTable)

        match = FourteenOneEngine.foul(match, match.player1.id, now())

        assertTrue(match.awaitingOpeningBreak)
        assertEquals(15, match.objectBallsOnTable)
    }

    @Test
    fun `given points, when undo, then object balls restore`() {
        val start = fresh()
        val scored = FourteenOneEngine.addPoints(start, start.player1.id, 5, now())
        assertEquals(10, scored.objectBallsOnTable)

        val undone = FourteenOneEngine.undoLast(scored)
        assertEquals(15, undone.objectBallsOnTable)
    }

    @Test
    fun `reduceObjectBalls re-racks when keyball alone remains`() {
        assertEquals(15, FourteenOneEngine.reduceObjectBalls(15, 14))
        assertEquals(14, FourteenOneEngine.reduceObjectBalls(15, 15))
        assertEquals(13, FourteenOneEngine.reduceObjectBalls(15, 16))
        assertEquals(13, FourteenOneEngine.reduceObjectBalls(3, 32))
        assertEquals(15, FourteenOneEngine.reduceObjectBalls(2, 1))
        assertEquals(15, FourteenOneEngine.reduceObjectBalls(1, 1))
    }

    @Test
    fun `pointsFromTableToRemaining covers same rack and single re-rack`() {
        assertEquals(0, FourteenOneEngine.pointsFromTableToRemaining(10, 10))
        assertEquals(3, FourteenOneEngine.pointsFromTableToRemaining(10, 7))
        assertEquals(7, FourteenOneEngine.pointsFromTableToRemaining(15, 8))
        // 6 → re-rack → 13: (6-1)+(15-13)=7
        assertEquals(7, FourteenOneEngine.pointsFromTableToRemaining(6, 13))
        assertEquals(1, FourteenOneEngine.pointsFromTableToRemaining(2, 15))
    }

    @Test
    fun `passWithRemaining adds last-rack points then switches hand`() {
        val start = fresh().copy(objectBallsOnTable = 10)
        val next = FourteenOneEngine.passWithRemaining(start, start.player1.id, 7, now())

        assertEquals(3, next.score1)
        assertEquals(7, next.objectBallsOnTable)
        assertEquals(start.player2.id, next.currentShooterId)
        assertEquals(1, next.innings1)
        assertEquals(MatchEventType.PASS, next.history.last().type)
    }

    @Test
    fun `foulWithRemaining syncs table then applies foul penalty`() {
        val start = fresh().copy(objectBallsOnTable = 6)
        val next = FourteenOneEngine.foulWithRemaining(start, start.player1.id, 4, now())

        // +2 then foul −1
        assertEquals(1, next.score1)
        assertEquals(4, next.objectBallsOnTable)
        assertEquals(start.player2.id, next.currentShooterId)
        assertEquals(MatchEventType.FOUL, next.history.last().type)
    }

    @Test
    fun `passWithRemaining after plus fourteen only syncs partial rack`() {
        var match = fresh()
        match = FourteenOneEngine.addPoints(match, match.player1.id, 14, now())
        assertEquals(15, match.objectBallsOnTable)
        assertEquals(14, match.currentRun)

        match = FourteenOneEngine.passWithRemaining(match, match.player1.id, 11, now())

        assertEquals(14 + 4, match.score1)
        assertEquals(11, match.objectBallsOnTable)
        assertEquals(match.player2.id, match.currentShooterId)
    }
}
