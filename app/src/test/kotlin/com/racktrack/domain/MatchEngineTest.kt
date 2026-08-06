package com.racktrack.domain

import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchEngineTest {
    private fun freshMatch(): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 6,
            initialBreakerIsPlayer1 = true,
        )

    @Test
    fun `given a new match, when plus one for player1, then score is 1 and break swaps`() {
        val match = freshMatch()

        val next = MatchEngine.recordPlusOne(match, match.player1.id)

        assertEquals(1, next.score1)
        assertEquals(0, next.score2)
        assertEquals(match.player2.id, next.currentBreakerId)
        assertEquals(MatchStatus.IN_PROGRESS, next.status)
    }

    @Test
    fun `given a new match, when run out for player2, then score is tagged as run out`() {
        val match = freshMatch()

        val next = MatchEngine.recordRunOut(match, match.player2.id)

        assertEquals(1, next.score2)
        assertEquals(MatchEventType.RUN_OUT, next.history.last().type)
        // Initial breaker was player1; alternate moves break to player2.
        assertEquals(match.player2.id, next.currentBreakerId)
    }

    @Test
    fun `given a foul, when recorded, then foul count increases without awarding a rack`() {
        val match = freshMatch()

        val next = MatchEngine.recordFoul(match, match.player1.id)

        assertEquals(1, next.foul1)
        assertEquals(0, next.score1)
        assertEquals(match.player1.id, next.currentBreakerId)
    }

    @Test
    fun `given player1 at 5 in race to 6, when plus one, then match is completed`() {
        var match = freshMatch()
        repeat(5) {
            match = MatchEngine.recordPlusOne(match, match.player1.id)
        }

        val next = MatchEngine.recordPlusOne(match, match.player1.id)

        assertEquals(6, next.score1)
        assertEquals(MatchStatus.COMPLETED, next.status)
        assertEquals(match.player1, next.winner)
    }

    @Test
    fun `given a plus one, when undo, then score and breaker are restored`() {
        val match = freshMatch()
        val awarded = MatchEngine.recordPlusOne(match, match.player1.id)

        val undone = MatchEngine.undoLast(awarded)

        assertEquals(0, undone.score1)
        assertEquals(match.player1.id, undone.currentBreakerId)
        assertTrue(undone.history.isEmpty())
        assertNull(undone.winner)
    }

    @Test
    fun `given a foul, when undo, then foul count is restored`() {
        val match = freshMatch()
        val fouled = MatchEngine.recordFoul(match, match.player2.id)

        val undone = MatchEngine.undoLast(fouled)

        assertEquals(0, undone.foul2)
        assertTrue(undone.history.isEmpty())
    }
}
