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

}
