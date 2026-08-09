package com.racktrack.presentation.viewmodel

import com.racktrack.appearance.FeltTone
import com.racktrack.data.UserSettings
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.PlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchCoordinatorTest {
    private var clock = 1_000_000L
    private val persisted = mutableListOf<UserSettings>()

    private fun coordinator(
        settings: UserSettings = UserSettings(),
    ): MatchCoordinator =
        MatchCoordinator(
            initialSettings = settings,
            persistSettings = { persisted += it },
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

}
