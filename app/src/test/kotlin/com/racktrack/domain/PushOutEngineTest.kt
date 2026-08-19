package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEvent
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PushOutPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PushOutEngineTest {
    private var clock = 2_000_000L

    private fun now(): Long {
        clock += 1_000L
        return clock
    }

    private fun fresh(gameMode: GameMode = GameMode.NINE_BALL): Match =
        Match.start(
            player1Name = "Alex",
            player2Name = "Sam",
            racksToWin = 6,
            initialBreakerIsPlayer1 = true,
            startedAtMillis = clock,
            gameMode = gameMode,
        )

    private fun announced(match: Match = fresh()): Match =
        PushOutEngine.announce(match, match.player1.id, now())

    private fun awaitingChoice(match: Match = fresh()): Match {
        val a = announced(match)
        return PushOutEngine.resolveClean(a, a.player1.id, now())
    }

    @Test
    fun `canAnnounce is true only for shooter when available`() {
        val match = fresh()
        assertTrue(PushOutEngine.canAnnounce(match, match.player1.id))
        assertFalse(PushOutEngine.canAnnounce(match, match.player2.id))
        assertFalse(PushOutEngine.canAnnounce(fresh(GameMode.EIGHT_BALL), match.player1.id))
        assertFalse(
            PushOutEngine.canAnnounce(
                match.copy(status = MatchStatus.COMPLETED),
                match.player1.id,
            ),
        )
        assertFalse(
            PushOutEngine.canAnnounce(
                match.copy(pushOutPhase = PushOutPhase.NONE),
                match.player1.id,
            ),
        )
    }

    @Test
    fun `announce is no-op when not allowed`() {
        val match = fresh()
        val blocked = PushOutEngine.announce(match, match.player2.id, now())
        assertSame(match, blocked)
    }

    @Test
    fun `resolveClean rejects wrong phase player or completed`() {
        val match = fresh()
        assertSame(match, PushOutEngine.resolveClean(match, match.player1.id, now()))

        val announced = announced()
        assertSame(
            announced,
            PushOutEngine.resolveClean(announced, announced.player2.id, now()),
        )
        val completed = announced.copy(status = MatchStatus.COMPLETED)
        assertSame(
            completed,
            PushOutEngine.resolveClean(completed, announced.player1.id, now()),
        )
    }

    @Test
    fun `resolveFoul by player2 increments foul2 and switches shooter`() {
        var match = fresh()
        match = MatchEngine.recordPlusOne(match, match.player1.id, now())
        // player2 breaks next rack with push-out available
        assertEquals(match.player2.id, match.currentShooterId)
        assertEquals(PushOutPhase.AVAILABLE, match.pushOutPhase)
        match = PushOutEngine.announce(match, match.player2.id, now())
        match = PushOutEngine.resolveFoul(match, match.player2.id, now())

        assertEquals(1, match.foul2)
        assertEquals(0, match.foul1)
        assertEquals(match.player1.id, match.currentShooterId)
        assertEquals(PushOutPhase.NONE, match.pushOutPhase)
    }

    @Test
    fun `resolveFoul on third consecutive foul awards the rack`() {
        var match = fresh()
        match = MatchEngine.recordFoul(match, match.player1.id, now())
        match = MatchEngine.recordFoul(match, match.player1.id, now())
        // foul clears push-out; re-open window for the test path via announce after forcing phase
        match = match.copy(pushOutPhase = PushOutPhase.ANNOUNCED)
        match = PushOutEngine.resolveFoul(match, match.player1.id, now())

        assertEquals(1, match.score2)
        assertEquals(0, match.foul1)
        assertEquals(MatchEventType.THREE_FOULS_LOSS, match.history.last().type)
        assertEquals(PushOutPhase.AVAILABLE, match.pushOutPhase)
    }

    @Test
    fun `resolveFoul is no-op when not announced`() {
        val match = fresh()
        assertSame(match, PushOutEngine.resolveFoul(match, match.player1.id, now()))
        val announced = announced()
        assertSame(
            announced,
            PushOutEngine.resolveFoul(announced, announced.player2.id, now()),
        )
        val completed = announced.copy(status = MatchStatus.COMPLETED)
        assertSame(
            completed,
            PushOutEngine.resolveFoul(completed, announced.player1.id, now()),
        )
    }

    @Test
    fun `take and giveBack reject completed or wrong phase`() {
        val match = fresh()
        assertSame(match, PushOutEngine.take(match, now()))
        assertSame(match, PushOutEngine.giveBack(match, now()))

        val choice = awaitingChoice()
        val completed = choice.copy(status = MatchStatus.COMPLETED)
        assertSame(completed, PushOutEngine.take(completed, now()))
        assertSame(completed, PushOutEngine.giveBack(completed, now()))
    }

    @Test
    fun `take and giveBack are no-op when announcer cannot be found`() {
        val choice = awaitingChoice().copy(
            // strip PUSH_OUT so announcerId walks into rack-ending / empty
            history = listOf(
                MatchEvent(MatchEventType.PUSH_OUT_CLEAN, fresh().player1.id, now()),
            ),
            pushOutPhase = PushOutPhase.AWAITING_CHOICE,
        )
        assertSame(choice, PushOutEngine.take(choice, now()))
        assertSame(choice, PushOutEngine.giveBack(choice, now()))
    }

    @Test
    fun `phaseAfterRack covers support and completed flags`() {
        assertEquals(PushOutPhase.AVAILABLE, PushOutEngine.phaseAfterRack(true, false))
        assertEquals(PushOutPhase.NONE, PushOutEngine.phaseAfterRack(true, true))
        assertEquals(PushOutPhase.NONE, PushOutEngine.phaseAfterRack(false, false))
    }

    @Test
    fun `phaseFromHistory walks dry foul clear announce clean and rack reset`() {
        val eight = fresh(GameMode.EIGHT_BALL)
        assertEquals(PushOutPhase.NONE, PushOutEngine.phaseFromHistory(eight, emptyList()))

        val nine = fresh()
        val p1 = nine.player1.id
        val t = now()
        assertEquals(PushOutPhase.AVAILABLE, PushOutEngine.phaseFromHistory(nine, emptyList()))

        val withDry = listOf(MatchEvent(MatchEventType.DRY_BREAK, p1, t))
        assertEquals(PushOutPhase.AVAILABLE, PushOutEngine.phaseFromHistory(nine, withDry))

        val withFoul = withDry + MatchEvent(MatchEventType.FOUL, p1, t + 1)
        assertEquals(PushOutPhase.NONE, PushOutEngine.phaseFromHistory(nine, withFoul))

        val cleared = withFoul + MatchEvent(MatchEventType.FOULS_CLEARED, p1, t + 2)
        assertEquals(PushOutPhase.NONE, PushOutEngine.phaseFromHistory(nine, cleared))

        val afterRack = listOf(
            MatchEvent(MatchEventType.PLUS_ONE, p1, t),
            MatchEvent(MatchEventType.PUSH_OUT, nine.player2.id, t + 1),
            MatchEvent(MatchEventType.PUSH_OUT_CLEAN, nine.player2.id, t + 2),
        )
        assertEquals(
            PushOutPhase.AWAITING_CHOICE,
            PushOutEngine.phaseFromHistory(nine, afterRack),
        )

        val afterTake = afterRack + MatchEvent(
            MatchEventType.PUSH_OUT_TAKE,
            nine.player1.id,
            t + 3,
        )
        assertEquals(PushOutPhase.NONE, PushOutEngine.phaseFromHistory(nine, afterTake))
    }

    @Test
    fun `undo covers each push-out event including player2 foul and unrelated`() {
        var match = awaitingChoice()
        match = PushOutEngine.take(match, now())
        val afterTake = match
        val undoneTake = PushOutEngine.undo(
            afterTake,
            afterTake.history.last(),
            afterTake.history.dropLast(1),
        )!!
        assertEquals(PushOutPhase.AWAITING_CHOICE, undoneTake.pushOutPhase)
        assertEquals(afterTake.player1.id, undoneTake.currentShooterId)

        match = PushOutEngine.giveBack(awaitingChoice(), now())
        val undoneReturn = PushOutEngine.undo(
            match,
            match.history.last(),
            match.history.dropLast(1),
        )!!
        assertEquals(PushOutPhase.AWAITING_CHOICE, undoneReturn.pushOutPhase)

        match = announced()
        match = PushOutEngine.resolveClean(match, match.player1.id, now())
        val undoneClean = PushOutEngine.undo(
            match,
            match.history.last(),
            match.history.dropLast(1),
        )!!
        assertEquals(PushOutPhase.ANNOUNCED, undoneClean.pushOutPhase)

        match = announced()
        val undoneAnnounce = PushOutEngine.undo(
            match,
            match.history.last(),
            match.history.dropLast(1),
        )!!
        assertEquals(PushOutPhase.AVAILABLE, undoneAnnounce.pushOutPhase)

        // player2 foul undo
        match = fresh()
        match = MatchEngine.recordPlusOne(match, match.player1.id, now())
        match = PushOutEngine.announce(match, match.player2.id, now())
        match = PushOutEngine.resolveFoul(match, match.player2.id, now())
        val undoneFoul = PushOutEngine.undo(
            match,
            match.history.last(),
            match.history.dropLast(1),
        )!!
        assertEquals(0, undoneFoul.foul2)
        assertEquals(PushOutPhase.ANNOUNCED, undoneFoul.pushOutPhase)
        assertEquals(match.player2.id, undoneFoul.currentShooterId)

        assertNull(
            PushOutEngine.undo(
                match,
                MatchEvent(MatchEventType.PLUS_ONE, match.player1.id, now()),
                match.history,
            ),
        )
    }

    @Test
    fun `announcerId returns null after a later rack-ending event`() {
        val match = fresh()
        val history = listOf(
            MatchEvent(MatchEventType.PUSH_OUT, match.player1.id, now()),
            MatchEvent(MatchEventType.PLUS_ONE, match.player1.id, now()),
        )
        val broken = match.copy(
            history = history,
            pushOutPhase = PushOutPhase.AWAITING_CHOICE,
        )
        assertSame(broken, PushOutEngine.take(broken, now()))
    }

    @Test
    fun `summarize counts announced push-outs per player`() {
        val match = fresh()
        val afterAnnounce = PushOutEngine.announce(match, match.player1.id, now())
        val afterClean = PushOutEngine.resolveClean(afterAnnounce, afterAnnounce.player1.id, now())
        val afterTake = PushOutEngine.take(afterClean, now())
        val summary = MatchStats.summarize(afterTake)
        assertEquals(1, summary.pushOuts1)
        assertEquals(0, summary.pushOuts2)
        assertTrue(
            MatchSummaryReport.playerStatLines(summary, 1).any { it == "Push outs 1" },
        )
    }
}
