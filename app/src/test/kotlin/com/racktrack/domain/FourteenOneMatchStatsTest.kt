package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FourteenOneMatchStatsTest {
    private var clock = 3_000_000L

    private fun now(): Long {
        clock += 30_000L
        return clock
    }

    private fun fresh(pointsToWin: Int = 100): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 1,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = pointsToWin,
            inningsLimit = 30,
        )

    @Test
    fun `given points and pass, inning line records pocketed points`() {
        var match = fresh()
        match = FourteenOneEngine.addPoints(match, match.player1.id, 5, now())
        match = FourteenOneEngine.addPoints(match, match.player1.id, 9, now())
        match = FourteenOneEngine.pass(match, match.player1.id, now())

        val lines = MatchStats.inningScores(match.history, match.player1.id)
        assertEquals(1, lines.size)
        assertEquals(14, lines[0].points)
        assertEquals(MatchEventType.PASS, lines[0].endType)
    }

    @Test
    fun `given foul with no points, inning line is -1`() {
        val start = fresh()
        val match = FourteenOneEngine.foul(start, start.player1.id, now())
        val lines = MatchStats.inningScores(match.history, match.player1.id)
        assertEquals(1, lines.size)
        assertEquals(-1, lines[0].points)
        assertEquals(MatchEventType.FOUL, lines[0].endType)
    }

    @Test
    fun `given points then foul, inning line nets pocketed minus one`() {
        var match = fresh()
        match = FourteenOneEngine.addPoints(match, match.player1.id, 5, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        val lines = MatchStats.inningScores(match.history, match.player1.id)
        assertEquals(listOf(4), lines.map { it.points })
        assertEquals(match.score1, lines.sumOf { it.points })
    }

    @Test
    fun `given three-foul penalty, inning line includes -15`() {
        var match = fresh()
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        match = FourteenOneEngine.pass(match, match.player2.id, now())
        match = FourteenOneEngine.foul(match, match.player1.id, now())

        val lines = MatchStats.inningScores(match.history, match.player1.id)
        assertEquals(listOf(-1, -1, -16), lines.map { it.points })
        assertEquals(match.score1, lines.sumOf { it.points })
    }

    @Test
    fun `given win mid-inning, open visit is kept with null end type`() {
        var match = fresh(pointsToWin = 20)
        match = FourteenOneEngine.addPoints(match, match.player1.id, 14, now())
        match = FourteenOneEngine.addPoints(match, match.player1.id, 6, now())

        val summary = MatchStats.summarize(match)
        assertEquals(listOf(20), summary.inningScores1.map { it.points })
        assertNull(summary.inningScores1.single().endType)
        assertEquals(1, summary.innings1)
        assertEquals(20.0, summary.average1)
        assertEquals(0, summary.inningScores2.size)
    }

    @Test
    fun `summarize exposes innings racks and per-visit lines for both players`() {
        var match = fresh()
        match = FourteenOneEngine.addPoints(match, match.player1.id, 14, now())
        match = FourteenOneEngine.pass(match, match.player1.id, now())
        match = FourteenOneEngine.addPoints(match, match.player2.id, 5, now())
        match = FourteenOneEngine.foul(match, match.player2.id, now())
        match = FourteenOneEngine.addPoints(match, match.player1.id, 3, now())
        match = FourteenOneEngine.pass(match, match.player1.id, now())

        val summary = MatchStats.summarize(match)
        assertEquals(2, summary.innings1)
        assertEquals(1, summary.innings2)
        assertEquals(listOf(14, 3), summary.inningScores1.map { it.points })
        assertEquals(listOf(4), summary.inningScores2.map { it.points })
        assertEquals(MatchEventType.FOUL, summary.inningScores2.single().endType)
        assertEquals(summary.score1, summary.inningScores1.sumOf { it.points })
        assertEquals(summary.score2, summary.inningScores2.sumOf { it.points })
    }

    @Test
    fun `solo summarize zeros side-2 and copies solo flag`() {
        var match = Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 1,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = 20,
            inningsLimit = 30,
            solo = true,
        )
        match = FourteenOneEngine.addPoints(match, match.player1.id, 14, now())
        match = FourteenOneEngine.pass(match, match.player1.id, now())
        match = FourteenOneEngine.addPoints(match, match.player1.id, 6, now())

        val summary = MatchStats.summarize(match)
        assertTrue(summary.solo)
        assertEquals(listOf(14, 6), summary.inningScores1.map { it.points })
        assertEquals(0, summary.inningScores2.size)
        assertEquals(0, summary.score2)
        assertEquals(0, summary.totalFouls2)
        assertEquals("Alex", summary.winnerName)
    }
}
