package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchSummaryReportTest {
    @Test
    fun `race report includes every rack line`() {
        val summary = MatchSummary(
            gameMode = GameMode.TEN_BALL,
            winnerName = "Alex",
            player1Name = "Alex",
            player2Name = "Sam",
            score1 = 2,
            score2 = 1,
            racksToWin = 2,
            pointsToWin = 0,
            inningsLimit = null,
            innings1 = 0,
            innings2 = 0,
            totalFouls1 = 0,
            totalFouls2 = 1,
            runOuts1 = 1,
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
            racks = listOf(
                RackStat(1, "Alex", 12_000L, MatchEventType.RUN_OUT),
                RackStat(2, "Sam", 20_000L, MatchEventType.PLUS_ONE),
                RackStat(3, "Alex", 15_000L, MatchEventType.PLUS_ONE),
            ),
            totalDurationMillis = 47_000L,
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_047_000L,
        )
        val text = MatchSummaryReport.lines(
            summary = summary,
            startedAtLabel = "9 August 2026, 17:00",
            endedAtLabel = "9 August 2026, 17:47",
        ).joinToString("\n")
        assertTrue(text.contains("Started  9 August 2026, 17:00"))
        assertTrue(text.contains("Ended  9 August 2026, 17:47"))
        assertTrue(text.contains("Duration"))
        assertTrue(text.contains("RACKS"))
        assertTrue(text.contains("#1  Alex"))
        assertTrue(text.contains("#2  Sam"))
        assertTrue(text.contains("#3  Alex"))
        assertTrue(text.contains("Run out"))
        assertTrue(
            MatchSummaryReport.fileStem(summary).contains(
                MatchSummaryReport.fileStartStamp(summary.startedAtMillis),
            ),
        )
    }

    @Test
    fun `14-1 report includes both players innings`() {
        val summary = MatchSummary(
            gameMode = GameMode.FOURTEEN_ONE,
            winnerName = "Sam",
            player1Name = "Alex",
            player2Name = "Sam",
            score1 = 20,
            score2 = 100,
            racksToWin = 1,
            pointsToWin = 100,
            inningsLimit = 30,
            innings1 = 2,
            innings2 = 1,
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
            highRun2 = 100,
            average1 = 10.0,
            average2 = 100.0,
            inningScores1 = listOf(
                InningStat(1, 14, MatchEventType.PASS),
                InningStat(2, 6, MatchEventType.FOUL),
            ),
            inningScores2 = listOf(
                InningStat(1, 100, null),
            ),
            racks = emptyList(),
            totalDurationMillis = 60_000L,
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_047_000L,
        )
        val text = MatchSummaryReport.lines(summary).joinToString("\n")
        assertTrue(text.contains("INNINGS"))
        assertTrue(text.contains("#1  14  pass  100  win"))
        assertTrue(text.contains("#2  6  foul  —  —"))
        val paired = MatchSummaryReport.pairedInningRows(
            summary.inningScores1,
            summary.inningScores2,
        )
        assertTrue(paired.size == 2)
        assertTrue(paired[0].player1?.points == 14)
        assertTrue(paired[0].player2?.points == 100)
        assertTrue(paired[1].player2 == null)
    }
}
