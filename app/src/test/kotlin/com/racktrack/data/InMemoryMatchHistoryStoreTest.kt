package com.racktrack.data

import com.racktrack.domain.MatchSummary
import com.racktrack.domain.model.GameMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InMemoryMatchHistoryStoreTest {
    @Test
    fun `deleteById removes match and ignores unknown id`() = runBlocking {
        val store = InMemoryMatchHistoryStore(idFactory = { "fixed-id" })
        store.saveCompleted(sampleSummary(), completedAtMillis = 1L)
        assertEquals(1, store.matches.first().size)

        assertTrue(store.deleteById("fixed-id"))
        assertEquals(0, store.matches.first().size)
        assertFalse(store.deleteById("fixed-id"))
    }

    private fun sampleSummary(): MatchSummary =
        MatchSummary(
            gameMode = GameMode.TEN_BALL,
            winnerName = "Alex",
            player1Name = "Alex",
            player2Name = "Sam",
            score1 = 1,
            score2 = 0,
            racksToWin = 1,
            pointsToWin = 0,
            inningsLimit = null,
            innings1 = 0,
            innings2 = 0,
            totalFouls1 = 0,
            totalFouls2 = 0,
            runOuts1 = 0,
            runOuts2 = 0,
            goldenBreaks1 = 0,
            goldenBreaks2 = 0,
            dryBreaks1 = 0,
            dryBreaks2 = 0,
            eightBallLosses1 = 0,
            eightBallLosses2 = 0,
            highRun1 = 0,
            highRun2 = 0,
            average1 = 0.0,
            average2 = 0.0,
            inningScores1 = emptyList(),
            inningScores2 = emptyList(),
            racks = emptyList(),
            totalDurationMillis = 1_000L,
            startedAtMillis = 1L,
            endedAtMillis = 1_001L,
        )
}
