package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.PushOutPhase
import com.racktrack.domain.model.RulesetPack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RulesetPackTest {
    private var clock = 3_000_000L

    private fun now(): Long {
        clock += 60_000L
        return clock
    }

    private fun race(
        mode: GameMode,
        pack: RulesetPack,
    ): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 5,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = mode,
            rulesetPack = pack,
        )

    private fun fourteenOne(pack: RulesetPack): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 1,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = GameMode.FOURTEEN_ONE,
            pointsToWin = 50,
            inningsLimit = 30,
            rulesetPack = pack,
        )

    @Test
    fun `shortLabel covers every pack`() {
        assertEquals("FFB", RulesetPack.FFB.shortLabel)
        assertEquals("WPA", RulesetPack.WPA.shortLabel)
        assertEquals("APA", RulesetPack.APA.shortLabel)
        assertEquals("BCA/CSI", RulesetPack.BCA_CSI.shortLabel)
        assertEquals("Matchroom (9)", RulesetPack.MATCHROOM.shortLabel)
    }

    @Test
    fun `officialRulesUrl is pack-specific and non-blank`() {
        for (pack in RulesetPack.entries) {
            val url = pack.officialRulesUrl()
            assertTrue(url.startsWith("https://"), pack.name)
            assertTrue(url.length > 20, pack.name)
        }
        assertTrue(RulesetPack.FFB.officialRulesUrl().contains("ffbillard"))
        assertTrue(RulesetPack.WPA.officialRulesUrl().contains("wpapool"))
        assertTrue(RulesetPack.APA.officialRulesUrl().contains("poolplayers"))
        assertTrue(RulesetPack.BCA_CSI.officialRulesUrl().contains("playcsipool"))
        assertTrue(RulesetPack.MATCHROOM.officialRulesUrl().contains("matchroompool"))
    }

    @Test
    fun `non race modes never allow push-out or three-foul`() {
        for (pack in RulesetPack.entries) {
            assertFalse(pack.allowsPushOut(GameMode.EIGHT_BALL), pack.name)
            assertFalse(pack.allowsPushOut(GameMode.FOURTEEN_ONE), pack.name)
            assertFalse(pack.allowsThreeFoulRackLoss(GameMode.EIGHT_BALL), pack.name)
            assertFalse(pack.allowsThreeFoulRackLoss(GameMode.FOURTEEN_ONE), pack.name)
        }
    }

    @Test
    fun `openingIllegalPlusClassicStacks is race-mode false`() {
        for (pack in RulesetPack.entries) {
            assertFalse(pack.openingIllegalPlusClassicStacks(GameMode.NINE_BALL), pack.name)
        }
    }

    @Test
    fun `matchroom forMode keeps 9-ball and falls back to ffb elsewhere`() {
        assertEquals(RulesetPack.MATCHROOM, RulesetPack.MATCHROOM.forMode(GameMode.NINE_BALL))
        assertEquals(RulesetPack.FFB, RulesetPack.MATCHROOM.forMode(GameMode.EIGHT_BALL))
        assertEquals(RulesetPack.FFB, RulesetPack.MATCHROOM.forMode(GameMode.TEN_BALL))
        assertEquals(RulesetPack.FFB, RulesetPack.MATCHROOM.forMode(GameMode.FOURTEEN_ONE))
        assertEquals(RulesetPack.APA, RulesetPack.APA.forMode(GameMode.EIGHT_BALL))
    }

    @Test
    fun `ffb default keeps push-out and three-foul on 9-ball`() {
        val pack = RulesetPack.FFB
        assertTrue(pack.allowsPushOut(GameMode.NINE_BALL))
        assertTrue(pack.allowsThreeFoulRackLoss(GameMode.NINE_BALL))
        assertFalse(pack.eightOnBreakAwardsRack(GameMode.EIGHT_BALL))
        assertTrue(pack.openingIllegalPlusClassicStacks(GameMode.FOURTEEN_ONE))
    }

    @Test
    fun `apa disables push-out and three-foul and enables 8 on break`() {
        val pack = RulesetPack.APA
        assertFalse(pack.allowsPushOut(GameMode.NINE_BALL))
        assertFalse(pack.allowsThreeFoulRackLoss(GameMode.TEN_BALL))
        assertTrue(pack.eightOnBreakAwardsRack(GameMode.EIGHT_BALL))
        assertFalse(pack.eightOnBreakAwardsRack(GameMode.NINE_BALL))
    }

    @Test
    fun `bca csi does not stack classic foul after illegal open`() {
        assertFalse(
            RulesetPack.BCA_CSI.openingIllegalPlusClassicStacks(GameMode.FOURTEEN_ONE),
        )
        assertTrue(
            RulesetPack.WPA.openingIllegalPlusClassicStacks(GameMode.FOURTEEN_ONE),
        )
    }

    @Test
    fun `apa 9-ball starts without push-out phase`() {
        val match = race(GameMode.NINE_BALL, RulesetPack.APA)
        assertEquals(PushOutPhase.NONE, match.pushOutPhase)
        assertFalse(MatchEngine.canAnnouncePushOut(match, match.player1.id))
    }

    @Test
    fun `apa 9-ball third foul does not award rack`() {
        var match = race(GameMode.NINE_BALL, RulesetPack.APA)
        match = MatchEngine.recordFoul(match, match.player1.id, now())
        match = MatchEngine.recordFoul(match, match.player1.id, now())
        match = MatchEngine.recordFoul(match, match.player1.id, now())

        assertEquals(3, match.foul1)
        assertEquals(0, match.score2)
        assertEquals(MatchEventType.FOUL, match.history.last().type)
    }

    @Test
    fun `apa 8-ball eight on break awards rack`() {
        val match = race(GameMode.EIGHT_BALL, RulesetPack.APA)
        assertTrue(MatchEngine.canRecordEightOnBreak(match, match.player1.id))

        val next = MatchEngine.recordEightOnBreak(match, match.player1.id, now())

        assertEquals(1, next.score1)
        assertEquals(MatchEventType.EIGHT_ON_BREAK, next.history.last().type)
        assertEquals(1, MatchStats.summarize(next).racks.size)
        assertEquals(MatchEventType.EIGHT_ON_BREAK, MatchStats.summarize(next).racks.first().endType)
    }

    @Test
    fun `ffb 8-ball ignores eight on break tap`() {
        val match = race(GameMode.EIGHT_BALL, RulesetPack.FFB)
        assertFalse(MatchEngine.canRecordEightOnBreak(match, match.player1.id))
        val next = MatchEngine.recordEightOnBreak(match, match.player1.id, now())
        assertTrue(next.history.isEmpty())
        assertEquals(0, next.score1)
    }

    @Test
    fun `ffb 14-1 stacks classic foul after illegal open to -3`() {
        var match = fourteenOne(RulesetPack.FFB)
        match = FourteenOneEngine.breakFoul(match, match.player1.id, now())
        assertEquals(-2, match.score1)
        match = FourteenOneEngine.foul(match, match.player1.id, now())
        assertEquals(-3, match.score1)
        assertEquals(MatchEventType.FOUL, match.history.last().type)
    }

    @Test
    fun `bca csi 14-1 classic foul after illegal open is no-op at -2`() {
        var match = fourteenOne(RulesetPack.BCA_CSI)
        match = FourteenOneEngine.breakFoul(match, match.player1.id, now())
        assertEquals(-2, match.score1)
        val after = FourteenOneEngine.foul(match, match.player1.id, now())
        assertEquals(-2, after.score1)
        assertEquals(MatchEventType.BREAK_FOUL, after.history.last().type)
    }
}
