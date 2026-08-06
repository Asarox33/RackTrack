package com.racktrack.domain.model

/**
 * In-memory race match for the landscape MVP.
 * Awards racks via +1 / run-out; fouls are recorded without awarding a rack.
 */
data class Match(
    val player1: Player,
    val player2: Player,
    val racksToWin: Int,
    val score1: Int = 0,
    val score2: Int = 0,
    val foul1: Int = 0,
    val foul2: Int = 0,
    val currentBreakerId: PlayerId,
    val status: MatchStatus = MatchStatus.IN_PROGRESS,
    val history: List<MatchEvent> = emptyList(),
) {
    init {
        require(racksToWin > 0) { "racksToWin must be positive" }
        require(player1.id != player2.id) { "players must be distinct" }
        require(currentBreakerId == player1.id || currentBreakerId == player2.id) {
            "breaker must be one of the match players"
        }
    }

    val winner: Player?
        get() = when {
            status != MatchStatus.COMPLETED -> null
            score1 >= racksToWin -> player1
            score2 >= racksToWin -> player2
            else -> null
        }

    fun scoreFor(playerId: PlayerId): Int = when (playerId) {
        player1.id -> score1
        player2.id -> score2
        else -> error("Unknown player: $playerId")
    }

    fun foulsFor(playerId: PlayerId): Int = when (playerId) {
        player1.id -> foul1
        player2.id -> foul2
        else -> error("Unknown player: $playerId")
    }

    fun otherPlayerId(playerId: PlayerId): PlayerId = when (playerId) {
        player1.id -> player2.id
        player2.id -> player1.id
        else -> error("Unknown player: $playerId")
    }

    companion object {
        fun start(
            player1Name: String,
            player2Name: String,
            racksToWin: Int,
            initialBreakerIsPlayer1: Boolean,
        ): Match {
            val p1 = Player(PlayerId("p1"), player1Name.trim().ifEmpty { "Player 1" })
            val p2 = Player(PlayerId("p2"), player2Name.trim().ifEmpty { "Player 2" })
            return Match(
                player1 = p1,
                player2 = p2,
                racksToWin = racksToWin,
                currentBreakerId = if (initialBreakerIsPlayer1) p1.id else p2.id,
            )
        }
    }
}
