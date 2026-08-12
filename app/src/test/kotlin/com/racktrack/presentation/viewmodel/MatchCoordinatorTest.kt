package com.racktrack.presentation.viewmodel

import com.racktrack.appearance.FeltTone
import com.racktrack.data.UserSettings
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.MatchStatus
import com.racktrack.domain.model.PlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchCoordinatorTest {
    private var clock = 1_000_000L
    private val persisted = mutableListOf<UserSettings>()
    private val completed = mutableListOf<Match>()

    private fun coordinator(
        settings: UserSettings = UserSettings(),
    ): MatchCoordinator =
        MatchCoordinator(
            initialSettings = settings,
            persistSettings = { persisted += it },
            onMatchCompleted = { completed += it },
            clock = {
                clock += 1_000L
                clock
            },
        )

    private fun MatchCoordinator.board(): AppScreen.MatchBoard {
        val screen = screen.value
        assertInstanceOf(AppScreen.MatchBoard::class.java, screen)
        return screen as AppScreen.MatchBoard
    }

    @Test
    fun `given race setup, when startMatch, then board uses race length and mode`() {
        val c = coordinator()
        c.updatePlayer1Name("Alex")
        c.updatePlayer2Name("Sam")
        c.updateGameMode(GameMode.NINE_BALL)
        c.updateRacksToWin(5)
        c.setPlayer1BreaksFirst(false)

        c.startMatch()

        val match = c.board().match
        assertEquals("Alex", match.player1.name)
        assertEquals("Sam", match.player2.name)
        assertEquals(GameMode.NINE_BALL, match.gameMode)
        assertEquals(5, match.racksToWin)
        assertEquals(match.player2.id, match.currentBreakerId)
    }

    @Test
    fun `given 14-1 setup, when startMatch, then board uses distance and innings`() {
        val c = coordinator()
        c.updateGameMode(GameMode.FOURTEEN_ONE)
        c.updatePointsToWin(75)
        c.updateInningsLimit(20)

        c.startMatch()

        val match = c.board().match
        assertEquals(GameMode.FOURTEEN_ONE, match.gameMode)
        assertEquals(75, match.pointsToWin)
        assertEquals(20, match.inningsLimit)
        assertEquals(20, match.inningsLimitBase)
    }

    @Test
    fun `given race board, when plusOne then undo, then score returns to zero`() {
        val c = coordinator()
        c.startMatch()
        val p1 = c.board().match.player1.id

        c.plusOne(p1)
        assertEquals(1, c.board().match.score1)

        c.undo()
        assertEquals(0, c.board().match.score1)
        assertTrue(c.board().match.history.isEmpty())
    }

    @Test
    fun `given 14-1 board, when addPoints and foul, then engines are routed`() {
        val c = coordinator()
        c.updateGameMode(GameMode.FOURTEEN_ONE)
        c.startMatch()
        val shooter = c.board().match.currentShooterId

        c.addPoints(shooter, 5)
        assertEquals(5, c.board().match.score1)
        assertEquals(MatchEventType.POINTS, c.board().match.history.last().type)

        c.foul(shooter)
        assertEquals(4, c.board().match.score1)
        assertEquals(MatchEventType.FOUL, c.board().match.history.last().type)
    }

    @Test
    fun `given setup screen, when plusOne, then screen stays on setup`() {
        val c = coordinator()
        c.plusOne(PlayerId("x"))
        assertEquals(AppScreen.Setup, c.screen.value)
    }

    @Test
    fun `given defaults changed in settings, when persisted, then setup mirrors them`() {
        val c = coordinator()
        c.setDefaultRacksToWin(9)
        c.setDefaultPointsToWin(125)
        c.setDefaultInningsLimit(null)
        c.setFeltTone(FeltTone.BURGUNDY)

        assertEquals(9, c.setup.value.racksToWin)
        assertEquals(125, c.setup.value.pointsToWin)
        assertEquals(null, c.setup.value.inningsLimit)
        assertEquals(FeltTone.BURGUNDY, c.settings.value.feltTone)
        assertEquals(FeltTone.BURGUNDY, persisted.last().feltTone)
        assertEquals(null, persisted.last().defaultInningsLimit)
    }

    @Test
    fun `given active match, when newMatch, then returns to setup`() {
        val c = coordinator()
        c.startMatch()
        c.newMatch()
        assertEquals(AppScreen.Setup, c.screen.value)
    }

    @Test
    fun `given race fouls, when clearFouls, then consecutive counter resets`() {
        val c = coordinator()
        c.updateGameMode(GameMode.TEN_BALL)
        c.startMatch()
        val p1 = c.board().match.player1.id
        c.foul(p1)
        c.foul(p1)
        assertEquals(2, c.board().match.foul1)

        c.clearFouls(p1)
        assertEquals(0, c.board().match.foul1)
    }

    @Test
    fun `given race completed, when last rack awarded, then onMatchCompleted fires once`() {
        completed.clear()
        val c = coordinator()
        c.updateGameMode(GameMode.TEN_BALL)
        c.updateRacksToWin(2)
        c.updatePlayer1Name("Alex")
        c.updatePlayer2Name("Sam")
        c.startMatch()
        val p1 = c.board().match.player1.id

        c.plusOne(p1)
        assertTrue(completed.isEmpty())
        c.plusOne(p1)

        assertEquals(MatchStatus.COMPLETED, c.board().match.status)
        assertEquals(1, completed.size)
        assertEquals("Alex", completed.single().player1.name)
    }

    @Test
    fun `given history open, when openHistoryDetail then close, then returns to history list`() {
        val c = coordinator()
        c.openHistory()
        assertEquals(AppScreen.History, c.screen.value)
        c.openHistoryDetail("abc")
        assertEquals(AppScreen.HistoryDetail("abc"), c.screen.value)
        c.closeHistoryDetail()
        assertEquals(AppScreen.History, c.screen.value)
    }

    @Test
    fun `given board, when paused, scoring is ignored until resume`() {
        var now = 1_000_000L
        val c = MatchCoordinator(
            initialSettings = UserSettings(),
            persistSettings = {},
            clock = { now },
        )
        c.updatePlayer1Name("Alex")
        c.updatePlayer2Name("Sam")
        c.startMatch()
        val p1 = c.board().match.player1.id

        c.toggleMatchPause()
        assertTrue(c.matchPaused.value)
        c.plusOne(p1)
        assertEquals(0, c.board().match.score1)

        now += 60_000L
        c.toggleMatchPause()
        assertTrue(!c.matchPaused.value)
        assertEquals(1, c.board().match.pauseSpans.size)
        assertEquals(60_000L, c.board().match.pauseSpans.single().let { it.endMillis - it.startMillis })

        c.plusOne(p1)
        assertEquals(1, c.board().match.score1)
    }

    @Test
    fun `pause spans are excluded from summarized match duration`() {
        var now = 0L
        val c = MatchCoordinator(
            initialSettings = UserSettings(),
            persistSettings = {},
            clock = { now },
        )
        c.updateGameMode(GameMode.TEN_BALL)
        c.updateRacksToWin(2)
        c.updatePlayer1Name("Alex")
        c.updatePlayer2Name("Sam")
        c.startMatch()
        val p1 = c.board().match.player1.id
        val p2 = c.board().match.player2.id

        now = 10_000L
        c.plusOne(p1)
        c.toggleMatchPause()
        now = 70_000L
        c.toggleMatchPause()
        now = 80_000L
        c.plusOne(p2)

        val summary = com.racktrack.domain.MatchStats.summarize(c.board().match)
        // Playing time 0→10k + 70k→80k = 20k (60k pause excluded)
        assertEquals(20_000L, summary.totalDurationMillis)
        assertEquals(10_000L, summary.racks[0].durationMillis)
        assertEquals(10_000L, summary.racks[1].durationMillis)
    }
}
