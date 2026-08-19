package com.racktrack.data

import com.racktrack.domain.InningStat
import com.racktrack.domain.MatchSummary
import com.racktrack.domain.RackStat
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MatchSummaryJsonTest {
    @Test
    fun `round trip preserves race summary`() {
        val summary = MatchSummary(
            gameMode = GameMode.NINE_BALL,
            winnerName = "Alex",
            player1Name = "Alex",
            player2Name = "Sam",
            score1 = 5,
            score2 = 2,
            racksToWin = 5,
            pointsToWin = 0,
            inningsLimit = null,
            innings1 = 0,
            innings2 = 0,
            totalFouls1 = 1,
            totalFouls2 = 2,
            runOuts1 = 1,
            runOuts2 = 0,
            goldenBreaks1 = 1,
            goldenBreaks2 = 0,
            dryBreaks1 = 0,
            dryBreaks2 = 1,
            eightBallLosses1 = 0,
            eightBallLosses2 = 0,
            highRun1 = 0,
            highRun2 = 0,
            average1 = 0.0,
            average2 = 0.0,
            inningScores1 = emptyList(),
            inningScores2 = emptyList(),
            racks = listOf(
                RackStat(1, "Alex", 12_000L, MatchEventType.GOLDEN_BREAK),
                RackStat(2, "Sam", 30_000L, MatchEventType.PLUS_ONE),
            ),
            totalDurationMillis = 42_000L,
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_047_000L,
        )
        val stored = StoredMatch("id-1", 99L, summary)
        val decoded = MatchSummaryJson.decodeStoredList(
            MatchSummaryJson.encodeStoredList(listOf(stored)),
        )
        assertEquals(listOf(stored), decoded)
    }

    @Test
    fun `round trip preserves 14-1 innings and nullable end type`() {
        val summary = MatchSummary(
            gameMode = GameMode.FOURTEEN_ONE,
            winnerName = "Sam",
            player1Name = "Alex",
            player2Name = "Sam",
            score1 = 40,
            score2 = 100,
            racksToWin = 1,
            pointsToWin = 100,
            inningsLimit = 30,
            innings1 = 2,
            innings2 = 2,
            totalFouls1 = 1,
            totalFouls2 = 0,
            runOuts1 = 0,
            runOuts2 = 0,
            goldenBreaks1 = 0,
            goldenBreaks2 = 0,
            dryBreaks1 = 0,
            dryBreaks2 = 0,
            eightBallLosses1 = 0,
            eightBallLosses2 = 0,
            highRun1 = 14,
            highRun2 = 50,
            average1 = 20.0,
            average2 = 50.0,
            inningScores1 = listOf(
                InningStat(1, 14, MatchEventType.PASS),
                InningStat(2, 26, MatchEventType.FOUL),
            ),
            inningScores2 = listOf(
                InningStat(1, 50, MatchEventType.PASS),
                InningStat(2, 50, null),
            ),
            racks = emptyList(),
            totalDurationMillis = 90_000L,
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_047_000L,
        )
        val stored = StoredMatch("id-2", 100L, summary)
        val decoded = MatchSummaryJson.decodeStored(MatchSummaryJson.encodeStored(stored))
        assertEquals(stored, decoded)
    }

    @Test
    fun `round trip preserves solo flag`() {
        val summary = MatchSummary(
            gameMode = GameMode.FOURTEEN_ONE,
            winnerName = "Alex",
            player1Name = "Alex",
            player2Name = "solo",
            score1 = 50,
            score2 = 0,
            racksToWin = 1,
            pointsToWin = 50,
            inningsLimit = 20,
            innings1 = 1,
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
            highRun1 = 50,
            highRun2 = 0,
            average1 = 50.0,
            average2 = 0.0,
            inningScores1 = listOf(InningStat(1, 50, null)),
            inningScores2 = emptyList(),
            racks = emptyList(),
            totalDurationMillis = 30_000L,
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_030_000L,
            solo = true,
        )
        val decoded = MatchSummaryJson.decodeSummary(MatchSummaryJson.encodeSummary(summary))
        assertEquals(true, decoded.solo)
        assertEquals(summary, decoded)
    }

    @Test
    fun `decode without solo field defaults to false`() {
        val summary = MatchSummary(
            gameMode = GameMode.FOURTEEN_ONE,
            winnerName = "Sam",
            player1Name = "Alex",
            player2Name = "Sam",
            score1 = 40,
            score2 = 100,
            racksToWin = 1,
            pointsToWin = 100,
            inningsLimit = 30,
            innings1 = 2,
            innings2 = 2,
            totalFouls1 = 1,
            totalFouls2 = 0,
            runOuts1 = 0,
            runOuts2 = 0,
            goldenBreaks1 = 0,
            goldenBreaks2 = 0,
            dryBreaks1 = 0,
            dryBreaks2 = 0,
            eightBallLosses1 = 0,
            eightBallLosses2 = 0,
            highRun1 = 14,
            highRun2 = 50,
            average1 = 20.0,
            average2 = 50.0,
            inningScores1 = emptyList(),
            inningScores2 = emptyList(),
            racks = emptyList(),
            totalDurationMillis = 90_000L,
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_047_000L,
        )
        val obj = MatchSummaryJson.encodeSummary(summary)
        obj.remove("solo")
        obj.remove("pushOuts1")
        obj.remove("pushOuts2")
        val decoded = MatchSummaryJson.decodeSummary(obj)
        assertEquals(false, decoded.solo)
        assertEquals(0, decoded.pushOuts1)
        assertEquals(0, decoded.pushOuts2)
    }
}
