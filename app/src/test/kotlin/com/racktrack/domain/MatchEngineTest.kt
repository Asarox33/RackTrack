package com.racktrack.domain

import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchEngineTest {
    private var clock = 1_000_000L

    private fun now(): Long {
        clock += 60_000L
        return clock
    }

    private fun freshMatch(gameMode: GameMode = GameMode.TEN_BALL): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 6,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = gameMode,
        )

    @Test
    fun `given a new match, when plus one for player1, then score is 1 and break swaps`() {
        val match = freshMatch()

        val next = MatchEngine.recordPlusOne(match, match.player1.id, now())

        assertEquals(1, next.score1)
        assertEquals(0, next.score2)
        assertEquals(match.player2.id, next.currentBreakerId)
        assertEquals(MatchStatus.IN_PROGRESS, next.status)
    }

    @Test
    fun `given breaker has the cue, when run out, then score and run-out count increase`() {
        val match = freshMatch()

        val next = MatchEngine.recordRunOut(match, match.player1.id, now())

        assertEquals(1, next.score1)
        assertEquals(1, next.runOut1)
        assertEquals(0, next.runOut2)
        assertEquals(MatchEventType.RUN_OUT, next.history.last().type)
        assertEquals(match.player2.id, next.currentBreakerId)
    }

    @Test
    fun `given player without the break, when run out, then match is unchanged`() {
        val match = freshMatch()

        val next = MatchEngine.recordRunOut(match, match.player2.id, now())

        assertEquals(0, next.score2)
        assertEquals(0, next.runOut2)
        assertTrue(next.history.isEmpty())
        assertEquals(match.player1.id, next.currentBreakerId)
    }

    @Test
    fun `given breaker already fouled, when run out, then match is unchanged`() {
        val match = freshMatch()
        val fouled = MatchEngine.recordFoul(match, match.player1.id, now())

        val next = MatchEngine.recordRunOut(fouled, match.player1.id, now())

        assertEquals(0, next.score1)
        assertEquals(0, next.runOut1)
        assertEquals(1, next.foul1)
        assertEquals(MatchEventType.FOUL, next.history.last().type)
    }

    @Test
    fun `given a plus one, when recorded, then run-out count stays zero`() {
        val match = freshMatch()

        val next = MatchEngine.recordPlusOne(match, match.player1.id, now())

        assertEquals(1, next.score1)
        assertEquals(0, next.runOut1)
        assertEquals(0, next.runOut2)
    }

    @Test
    fun `given a run out, when undo, then score and run-out count are restored`() {
        val match = freshMatch()
        val awarded = MatchEngine.recordRunOut(match, match.player1.id, now())

        val undone = MatchEngine.undoLast(awarded)

        assertEquals(0, undone.score1)
        assertEquals(0, undone.runOut1)
        assertEquals(match.player1.id, undone.currentBreakerId)
        assertTrue(undone.history.isEmpty())
    }

    @Test
    fun `given a foul, when recorded, then consecutive foul count increases without awarding a rack`() {
        val match = freshMatch()

        val next = MatchEngine.recordFoul(match, match.player1.id, now())

        assertEquals(1, next.foul1)
        assertEquals(0, next.score1)
        assertEquals(match.player1.id, next.currentBreakerId)
    }

    @Test
    fun `given two consecutive fouls, when a third foul is recorded, then opponent wins the rack`() {
        val match = freshMatch()
        val twice = MatchEngine.recordFoul(
            MatchEngine.recordFoul(match, match.player1.id, now()),
            match.player1.id,
            now(),
        )

        val next = MatchEngine.recordFoul(twice, match.player1.id, now())

        assertEquals(0, next.score1)
        assertEquals(1, next.score2)
        assertEquals(0, next.foul1)
        assertEquals(0, next.foul2)
        assertEquals(MatchEventType.THREE_FOULS_LOSS, next.history.last().type)
        assertEquals(match.player1.id, next.history.last().playerId)
        assertEquals(match.player2.id, next.currentBreakerId)
    }

    @Test
    fun `given fouls by both players, consecutive counters stay independent`() {
        val match = freshMatch()
        val after = MatchEngine.recordFoul(
            MatchEngine.recordFoul(
                MatchEngine.recordFoul(match, match.player1.id, now()),
                match.player2.id,
                now(),
            ),
            match.player1.id,
            now(),
        )

        assertEquals(2, after.foul1)
        assertEquals(1, after.foul2)
        assertEquals(0, after.score1)
        assertEquals(0, after.score2)
    }

    @Test
    fun `given consecutive fouls, when a rack is awarded, then foul counters reset`() {
        val start = freshMatch()
        val fouled = MatchEngine.recordFoul(
            MatchEngine.recordFoul(start, start.player1.id, now()),
            start.player1.id,
            now(),
        )

        val next = MatchEngine.recordPlusOne(fouled, start.player1.id, now())

        assertEquals(0, next.foul1)
        assertEquals(0, next.foul2)
        assertEquals(1, next.score1)
    }

    @Test
    fun `given a three-foul rack loss, when undo, then scores and foul warning state are restored`() {
        val match = freshMatch()
        var current = match
        repeat(3) {
            current = MatchEngine.recordFoul(current, match.player1.id, now())
        }

        val undone = MatchEngine.undoLast(current)

        assertEquals(0, undone.score2)
        assertEquals(2, undone.foul1)
        assertEquals(match.player1.id, undone.currentBreakerId)
        assertEquals(MatchStatus.IN_PROGRESS, undone.status)
    }

    @Test
    fun `given player1 at 5 in race to 6, when plus one, then match is completed`() {
        var match = freshMatch()
        repeat(5) {
            match = MatchEngine.recordPlusOne(match, match.player1.id, now())
        }

        val next = MatchEngine.recordPlusOne(match, match.player1.id, now())

        assertEquals(6, next.score1)
        assertEquals(MatchStatus.COMPLETED, next.status)
        assertEquals(match.player1, next.winner)
    }

    @Test
    fun `given a plus one, when undo, then score and breaker are restored`() {
        val match = freshMatch()
        val awarded = MatchEngine.recordPlusOne(match, match.player1.id, now())

        val undone = MatchEngine.undoLast(awarded)

        assertEquals(0, undone.score1)
        assertEquals(match.player1.id, undone.currentBreakerId)
        assertTrue(undone.history.isEmpty())
        assertNull(undone.winner)
    }

    @Test
    fun `given a foul, when undo, then foul count is restored`() {
        val match = freshMatch()
        val fouled = MatchEngine.recordFoul(match, match.player2.id, now())

        val undone = MatchEngine.undoLast(fouled)

        assertEquals(0, undone.foul2)
        assertTrue(undone.history.isEmpty())
    }

    @Test
    fun `given completed race, summarize exposes fouls run-outs and rack durations`() {
        val start = Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 2,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
        )
        var match = MatchEngine.recordRunOut(start, start.player1.id, now())
        match = MatchEngine.recordFoul(match, start.player2.id, now())
        match = MatchEngine.recordPlusOne(match, start.player2.id, now())
        match = MatchEngine.recordPlusOne(match, start.player1.id, now())

        val summary = MatchStats.summarize(match)

        assertEquals("Alex", summary.winnerName)
        assertEquals(2, summary.score1)
        assertEquals(1, summary.score2)
        assertEquals(0, summary.totalFouls1)
        assertEquals(1, summary.totalFouls2)
        assertEquals(1, summary.runOuts1)
        assertEquals(3, summary.racks.size)
        assertTrue(summary.totalDurationMillis > 0L)
        assertTrue(summary.racks.all { it.durationMillis > 0L })
    }

    @Test
    fun `given 9-ball breaker, when golden break, then rack and counter increase`() {
        val match = freshMatch(GameMode.NINE_BALL)

        val next = MatchEngine.recordGoldenBreak(match, match.player1.id, now())

        assertEquals(1, next.score1)
        assertEquals(1, next.goldenBreak1)
        assertEquals(MatchEventType.GOLDEN_BREAK, next.history.last().type)
        assertEquals(match.player2.id, next.currentBreakerId)
    }

    @Test
    fun `given 10-ball match, when golden break, then match is unchanged`() {
        val match = freshMatch(GameMode.TEN_BALL)

        val next = MatchEngine.recordGoldenBreak(match, match.player1.id, now())

        assertEquals(0, next.score1)
        assertEquals(0, next.goldenBreak1)
        assertTrue(next.history.isEmpty())
    }

    @Test
    fun `given 9-ball breaker, when dry break, then counter increases without awarding a rack`() {
        val match = freshMatch(GameMode.NINE_BALL)

        val next = MatchEngine.recordDryBreak(match, match.player1.id, now())

        assertEquals(0, next.score1)
        assertEquals(1, next.dryBreak1)
        assertEquals(match.player1.id, next.currentBreakerId)
        assertEquals(MatchEventType.DRY_BREAK, next.history.last().type)
    }

    @Test
    fun `given dry break this rack, when run out or golden, then match is unchanged`() {
        val match = freshMatch(GameMode.NINE_BALL)
        val dry = MatchEngine.recordDryBreak(match, match.player1.id, now())

        val afterRunOut = MatchEngine.recordRunOut(dry, match.player1.id, now())
        val afterGolden = MatchEngine.recordGoldenBreak(dry, match.player1.id, now())

        assertEquals(0, afterRunOut.score1)
        assertEquals(0, afterRunOut.runOut1)
        assertEquals(0, afterGolden.score1)
        assertEquals(0, afterGolden.goldenBreak1)
        assertEquals(1, afterRunOut.dryBreak1)
    }

    @Test
    fun `given dry break, when undo, then dry-break count is restored`() {
        val match = freshMatch(GameMode.NINE_BALL)
        val dry = MatchEngine.recordDryBreak(match, match.player1.id, now())

        val undone = MatchEngine.undoLast(dry)

        assertEquals(0, undone.dryBreak1)
        assertTrue(undone.history.isEmpty())
    }

    @Test
    fun `given golden break, when undo, then score and golden count are restored`() {
        val match = freshMatch(GameMode.NINE_BALL)
        val golden = MatchEngine.recordGoldenBreak(match, match.player1.id, now())

        val undone = MatchEngine.undoLast(golden)

        assertEquals(0, undone.score1)
        assertEquals(0, undone.goldenBreak1)
        assertEquals(match.player1.id, undone.currentBreakerId)
    }

    @Test
    fun `given 8-ball, when three fouls, then opponent does not win the rack`() {
        val match = freshMatch(GameMode.EIGHT_BALL)
        var current = match
        repeat(3) {
            current = MatchEngine.recordFoul(current, match.player1.id, now())
        }

        assertEquals(0, current.score1)
        assertEquals(0, current.score2)
        assertEquals(3, current.foul1)
        assertEquals(MatchEventType.FOUL, current.history.last().type)
    }

    @Test
    fun `given 8-ball, when early 8, then opponent wins the rack`() {
        val match = freshMatch(GameMode.EIGHT_BALL)

        val next = MatchEngine.recordEightBallLoss(match, match.player1.id, now())

        assertEquals(0, next.score1)
        assertEquals(1, next.score2)
        assertEquals(1, next.eightBallLoss1)
        assertEquals(MatchEventType.EIGHT_BALL_LOSS, next.history.last().type)
        assertEquals(match.player2.id, next.currentBreakerId)
    }

    @Test
    fun `given 10-ball, when early 8, then match is unchanged`() {
        val match = freshMatch(GameMode.TEN_BALL)

        val next = MatchEngine.recordEightBallLoss(match, match.player1.id, now())

        assertEquals(0, next.score2)
        assertEquals(0, next.eightBallLoss1)
        assertTrue(next.history.isEmpty())
    }

    @Test
    fun `given 8-ball breaker, when dry break, then counter increases without awarding a rack`() {
        val match = freshMatch(GameMode.EIGHT_BALL)

        val next = MatchEngine.recordDryBreak(match, match.player1.id, now())

        assertEquals(0, next.score1)
        assertEquals(1, next.dryBreak1)
        assertEquals(MatchEventType.DRY_BREAK, next.history.last().type)
    }

    @Test
    fun `given early 8, when undo, then scores and loss counter are restored`() {
        val match = freshMatch(GameMode.EIGHT_BALL)
        val lost = MatchEngine.recordEightBallLoss(match, match.player1.id, now())

        val undone = MatchEngine.undoLast(lost)

        assertEquals(0, undone.score2)
        assertEquals(0, undone.eightBallLoss1)
        assertEquals(match.player1.id, undone.currentBreakerId)
        assertTrue(undone.history.isEmpty())
    }

    @Test
    fun `given 9-ball, canRecord helpers match UI enablement rules`() {
        val match = freshMatch(GameMode.NINE_BALL)
        val p1 = match.player1.id
        val p2 = match.player2.id

        assertTrue(MatchEngine.canRecordGoldenBreak(match, p1))
        assertTrue(MatchEngine.canRecordDryBreak(match, p1))
        assertFalse(MatchEngine.canRecordGoldenBreak(match, p2))
        assertFalse(MatchEngine.canRecordDryBreak(match, p2))
        assertFalse(MatchEngine.canRecordEightBallLoss(match, p1))
    }

    @Test
    fun `given 8-ball, canRecordEightBallLoss is true for both players`() {
        val match = freshMatch(GameMode.EIGHT_BALL)

        assertTrue(MatchEngine.canRecordEightBallLoss(match, match.player1.id))
        assertTrue(MatchEngine.canRecordEightBallLoss(match, match.player2.id))
        assertTrue(MatchEngine.canRecordDryBreak(match, match.player1.id))
        assertFalse(MatchEngine.canRecordGoldenBreak(match, match.player1.id))
    }

    @Test
    fun `given player2 has the break, when run out golden dry and early 8, then player2 counters update`() {
        fun start(mode: GameMode, player1Breaks: Boolean): Match =
            Match.start(
                player1Name = "Alex",
                player2Name = "Sam",
                racksToWin = 6,
                initialBreakerIsPlayer1 = player1Breaks,
                startedAtMillis = clock,
                gameMode = mode,
            )

        val nine = start(GameMode.NINE_BALL, player1Breaks = false)
        val runOut = MatchEngine.recordRunOut(nine, nine.player2.id, now())
        assertEquals(1, runOut.score2)
        assertEquals(1, runOut.runOut2)

        val goldenBase = start(GameMode.NINE_BALL, player1Breaks = false)
        val afterGolden = MatchEngine.recordGoldenBreak(goldenBase, goldenBase.player2.id, now())
        assertEquals(1, afterGolden.goldenBreak2)

        val dryBase = start(GameMode.NINE_BALL, player1Breaks = false)
        val afterDry = MatchEngine.recordDryBreak(dryBase, dryBase.player2.id, now())
        assertEquals(1, afterDry.dryBreak2)

        val eight = start(GameMode.EIGHT_BALL, player1Breaks = true)
        val afterEarly8 = MatchEngine.recordEightBallLoss(eight, eight.player2.id, now())
        assertEquals(1, afterEarly8.score1)
        assertEquals(1, afterEarly8.eightBallLoss2)

        val undone = MatchEngine.undoLast(afterEarly8)
        assertEquals(0, undone.eightBallLoss2)
        assertEquals(0, undone.score1)
    }

    @Test
    fun `given dry-break guards, when completed fouled or duplicate, then match is unchanged`() {
        val match = freshMatch(GameMode.NINE_BALL)
        val dry = MatchEngine.recordDryBreak(match, match.player1.id, now())
        val duplicate = MatchEngine.recordDryBreak(dry, match.player1.id, now())
        assertEquals(1, duplicate.dryBreak1)
        assertEquals(1, duplicate.history.size)

        val fouled = MatchEngine.recordFoul(match, match.player1.id, now())
        val afterFoul = MatchEngine.recordDryBreak(fouled, match.player1.id, now())
        assertEquals(0, afterFoul.dryBreak1)

        var race = freshMatch(GameMode.NINE_BALL)
        repeat(6) {
            race = MatchEngine.recordPlusOne(race, race.player1.id, now())
        }
        assertEquals(MatchStatus.COMPLETED, race.status)
        assertFalse(MatchEngine.canRecordDryBreak(race, race.player1.id))
        assertFalse(MatchEngine.canRecordEightBallLoss(race, race.player1.id))
        val afterCompleted = MatchEngine.recordDryBreak(race, race.player1.id, now())
        assertEquals(race.history.size, afterCompleted.history.size)
    }

    @Test
    fun `given empty history, when undo, then match is unchanged`() {
        val match = freshMatch()
        val undone = MatchEngine.undoLast(match)
        assertTrue(undone.history.isEmpty())
        assertEquals(0, undone.score1)
    }

    @Test
    fun `given two fouls on 10-ball, when clearConsecutiveFouls, then counter resets and undo restores`() {
        val match = freshMatch(GameMode.TEN_BALL)
        val fouled = MatchEngine.recordFoul(
            MatchEngine.recordFoul(match, match.player1.id, now()),
            match.player1.id,
            now(),
        )
        assertEquals(2, fouled.foul1)

        val cleared = MatchEngine.clearConsecutiveFouls(fouled, match.player1.id, now())
        assertEquals(0, cleared.foul1)
        assertEquals(MatchEventType.FOULS_CLEARED, cleared.history.last().type)

        val undone = MatchEngine.undoLast(cleared)
        assertEquals(2, undone.foul1)
    }

    @Test
    fun `given 8-ball fouls, when clearConsecutiveFouls, then match is unchanged`() {
        val match = freshMatch(GameMode.EIGHT_BALL)
        val fouled = MatchEngine.recordFoul(match, match.player1.id, now())
        val cleared = MatchEngine.clearConsecutiveFouls(fouled, match.player1.id, now())
        assertEquals(1, cleared.foul1)
        assertEquals(fouled.history.size, cleared.history.size)
    }

    @Test
    fun `given cleared fouls then two more, third foul still loses the rack`() {
        val match = freshMatch(GameMode.NINE_BALL)
        var current = MatchEngine.recordFoul(match, match.player1.id, now())
        current = MatchEngine.clearConsecutiveFouls(current, match.player1.id, now())
        current = MatchEngine.recordFoul(current, match.player1.id, now())
        current = MatchEngine.recordFoul(current, match.player1.id, now())
        current = MatchEngine.recordFoul(current, match.player1.id, now())

        assertEquals(1, current.score2)
        assertEquals(MatchEventType.THREE_FOULS_LOSS, current.history.last().type)
    }

    @Test
    fun `given alternate break, when rack awarded, then breaker always flips`() {
        val match = freshMatch().copy(breakRule = BreakRule.ALTERNATE)
        val afterP1 = MatchEngine.recordPlusOne(match, match.player1.id, now())
        assertEquals(match.player2.id, afterP1.currentBreakerId)

        val afterP2 = MatchEngine.recordPlusOne(afterP1, afterP1.player2.id, now())
        assertEquals(match.player1.id, afterP2.currentBreakerId)

        // Non-breaker wins: still flips from previous breaker
        val afterP1Again = MatchEngine.recordPlusOne(afterP2, afterP2.player1.id, now())
        assertEquals(match.player2.id, afterP1Again.currentBreakerId)
    }

    @Test
    fun `given winner break, when rack awarded, then winner keeps the break`() {
        val match = freshMatch().copy(breakRule = BreakRule.WINNER)
        val afterP1 = MatchEngine.recordPlusOne(match, match.player1.id, now())
        assertEquals(match.player1.id, afterP1.currentBreakerId)

        val afterP2 = MatchEngine.recordPlusOne(afterP1, afterP1.player2.id, now())
        assertEquals(match.player2.id, afterP2.currentBreakerId)
    }

    @Test
    fun `given winner break and non-breaker wins, then winner takes the next break`() {
        val match = freshMatch().copy(breakRule = BreakRule.WINNER)
        // p2 wins while p1 was breaker
        val next = MatchEngine.recordPlusOne(match, match.player2.id, now())
        assertEquals(match.player2.id, next.currentBreakerId)
    }

    @Test
    fun `given winner break, when undo rack, then breaker is rebuilt from history`() {
        val match = freshMatch().copy(breakRule = BreakRule.WINNER)
        val awarded = MatchEngine.recordPlusOne(match, match.player1.id, now())
        assertEquals(match.player1.id, awarded.currentBreakerId)

        val undone = MatchEngine.undoLast(awarded)
        assertEquals(match.player1.id, undone.currentBreakerId)
        assertEquals(0, undone.score1)
    }

    @Test
    fun `given winner break after opponent win, when undo, then opening breaker restored`() {
        val match = freshMatch().copy(breakRule = BreakRule.WINNER)
        val awarded = MatchEngine.recordPlusOne(match, match.player2.id, now())
        assertEquals(match.player2.id, awarded.currentBreakerId)

        val undone = MatchEngine.undoLast(awarded)
        assertEquals(match.player1.id, undone.currentBreakerId)
    }
}
