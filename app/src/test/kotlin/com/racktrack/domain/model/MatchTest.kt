package com.racktrack.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchTest {
    @Test
    fun `start race with winner break stores opening breaker and rule`() {
        val match = Match.start(
            player1Name = "  ",
            player2Name = "",
            racksToWin = 5,
            initialBreakerIsPlayer1 = false,
            startedAtMillis = 10L,
            gameMode = GameMode.NINE_BALL,
            breakRule = BreakRule.WINNER,
        )

        assertEquals("Player 1", match.player1.name)
        assertEquals("Player 2", match.player2.name)
        assertEquals(BreakRule.WINNER, match.breakRule)
        assertEquals(match.player2.id, match.openingBreakerId)
        assertEquals(match.player2.id, match.currentBreakerId)
        assertEquals(match.player2.id, match.currentShooterId)
        assertEquals(15, match.objectBallsOnTable)
    }

    @Test
    fun `start fourteen-one forces alternate break and awaiting opening break`() {
        val match = Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 9,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 20L,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = 100,
            inningsLimit = 30,
            breakRule = BreakRule.WINNER,
        )

        assertEquals(BreakRule.ALTERNATE, match.breakRule)
        assertEquals(1, match.racksToWin)
        assertEquals(100, match.pointsToWin)
        assertEquals(30, match.inningsLimit)
        assertEquals(30, match.inningsLimitBase)
        assertTrue(match.awaitingOpeningBreak)
        assertEquals(match.player1.id, match.openingBreakerId)
    }

    @Test
    fun `winner is null while in progress`() {
        val match = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 3,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        assertNull(match.winner)
    }

    @Test
    fun `race winner follows racks when completed`() {
        val base = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 2,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        assertEquals(base.player1, base.copy(score1 = 2, status = MatchStatus.COMPLETED).winner)
        assertEquals(base.player2, base.copy(score2 = 2, status = MatchStatus.COMPLETED).winner)
        assertNull(base.copy(score1 = 1, score2 = 1, status = MatchStatus.COMPLETED).winner)
    }

    @Test
    fun `fourteen-one winner follows points when completed`() {
        val base = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 1,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = 50,
        )
        assertEquals(base.player1, base.copy(score1 = 50, score2 = 10, status = MatchStatus.COMPLETED).winner)
        assertEquals(base.player2, base.copy(score1 = 10, score2 = 50, status = MatchStatus.COMPLETED).winner)
        assertNull(base.copy(score1 = 40, score2 = 40, status = MatchStatus.COMPLETED).winner)
    }

    @Test
    fun `foulsFor and otherPlayerId reject unknown players`() {
        val match = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 3,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        val unknown = PlayerId("x")

        assertEquals(0, match.foulsFor(match.player1.id))
        assertEquals(0, match.foulsFor(match.player2.id))
        assertThrows(IllegalStateException::class.java) { match.foulsFor(unknown) }
        assertEquals(match.player2.id, match.otherPlayerId(match.player1.id))
        assertEquals(match.player1.id, match.otherPlayerId(match.player2.id))
        assertThrows(IllegalStateException::class.java) { match.otherPlayerId(unknown) }
    }

    @Test
    fun `init rejects invalid opening breaker`() {
        val match = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 3,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        assertThrows(IllegalArgumentException::class.java) {
            match.copy(openingBreakerId = PlayerId("nope"))
        }
    }

    @Test
    fun `init rejects invalid fourteen-one constraints`() {
        val base = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 1,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = 50,
            inningsLimit = 10,
        )
        assertThrows(IllegalArgumentException::class.java) { base.copy(pointsToWin = 0) }
        assertThrows(IllegalArgumentException::class.java) { base.copy(inningsLimitBase = 0) }
        assertThrows(IllegalArgumentException::class.java) { base.copy(inningsLimit = 0) }
        assertThrows(IllegalArgumentException::class.java) { base.copy(objectBallsOnTable = 0) }
        assertThrows(IllegalArgumentException::class.java) { base.copy(objectBallsOnTable = 16) }
    }

    @Test
    fun `init rejects non-positive race length`() {
        val match = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 3,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        assertThrows(IllegalArgumentException::class.java) { match.copy(racksToWin = 0) }
    }

    @Test
    fun `init rejects breaker or shooter outside the match`() {
        val match = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 3,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        assertThrows(IllegalArgumentException::class.java) {
            match.copy(currentBreakerId = PlayerId("nope"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            match.copy(currentShooterId = PlayerId("nope"))
        }
    }

    @Test
    fun `init rejects duplicate players`() {
        val match = Match.start(
            player1Name = "A",
            player2Name = "B",
            racksToWin = 3,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = 1L,
        )
        assertThrows(IllegalArgumentException::class.java) {
            match.copy(player2 = match.player1)
        }
    }
}
