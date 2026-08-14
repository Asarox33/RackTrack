package com.racktrack.data

import com.racktrack.domain.MatchSummary
import com.racktrack.domain.model.GameMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchHistoryFilterTest {
    @Test
    fun `empty name filters keep all matches for mode`() {
        val matches = listOf(
            stored("Alex", "Sam", GameMode.TEN_BALL),
            stored("Jo", "Kim", GameMode.NINE_BALL),
        )
        assertEquals(
            listOf(matches[0]),
            MatchHistoryFilter.apply(matches, "", "", GameMode.TEN_BALL),
        )
    }

    @Test
    fun `single filter matches either seat within mode`() {
        val matches = listOf(
            stored("Alex", "Sam", GameMode.TEN_BALL),
            stored("Sam", "Jo", GameMode.EIGHT_BALL),
        )
        val filtered = MatchHistoryFilter.apply(matches, "sam", "", GameMode.TEN_BALL)
        assertEquals(1, filtered.size)
        assertEquals("Alex", filtered.single().summary.player1Name)
    }

    @Test
    fun `two filters are order independent`() {
        val match = stored("Alex", "Sam")
        assertTrue(
            MatchHistoryFilter.matchesQueries(
                listOf("Alex", "Sam"),
                "sam",
                "alex",
            ),
        )
        assertTrue(
            MatchHistoryFilter.apply(listOf(match), "Sam", "Alex").isNotEmpty(),
        )
        assertFalse(
            MatchHistoryFilter.matchesQueries(
                listOf("Alex", "Sam"),
                "alex",
                "kim",
            ),
        )
    }

    @Test
    fun `solo match filters by trainer name under 14-1 mode`() {
        val solo = StoredMatch(
            id = "solo-1",
            completedAtMillis = 1L,
            summary = sampleSummary("Alex", "solo", GameMode.FOURTEEN_ONE).copy(solo = true),
        )
        val duel = stored("Alex", "Sam", GameMode.FOURTEEN_ONE)
        val filtered = MatchHistoryFilter.apply(
            matches = listOf(solo, duel),
            playerQuery1 = "alex",
            playerQuery2 = "",
            gameMode = GameMode.FOURTEEN_ONE,
        )
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.summary.solo })
    }

    private fun stored(
        player1: String,
        player2: String,
        gameMode: GameMode = GameMode.TEN_BALL,
    ): StoredMatch =
        StoredMatch(
            id = "$player1-$player2-$gameMode",
            completedAtMillis = 1L,
            summary = sampleSummary(player1, player2, gameMode),
        )

    private fun sampleSummary(
        player1: String,
        player2: String,
        gameMode: GameMode = GameMode.TEN_BALL,
    ): MatchSummary =
        MatchSummary(
            gameMode = gameMode,
            winnerName = player1,
            player1Name = player1,
            player2Name = player2,
            score1 = 6,
            score2 = 3,
            racksToWin = 6,
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
            startedAtMillis = 1_700_000_000_000L,
            endedAtMillis = 1_700_000_047_000L,
        )
}
