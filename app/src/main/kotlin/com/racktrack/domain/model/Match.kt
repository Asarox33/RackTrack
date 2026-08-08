package com.racktrack.domain.model

/**
 * In-memory match for race modes (8/9/10) or 14/1 continuous.
 *
 * Race: [score1]/[score2] are racks; [racksToWin] is the race length.
 * 14/1: scores are points (may be negative); [pointsToWin] + optional [inningsLimit].
 */
data class Match(
    val player1: Player,
    val player2: Player,
    val gameMode: GameMode,
    val racksToWin: Int,
    val pointsToWin: Int = 0,
    /** Setup innings cap; null = unlimited (race to points only). Never mutated by overtime. */
    val inningsLimitBase: Int? = null,
    /** Effective innings cap (base + overtime extensions). */
    val inningsLimit: Int? = null,
    val score1: Int = 0,
    val score2: Int = 0,
    val innings1: Int = 0,
    val innings2: Int = 0,
    /** Consecutive fouls for player 1 (race: current rack; 14/1: until a legal shot). */
    val foul1: Int = 0,
    /** Consecutive fouls for player 2. */
    val foul2: Int = 0,
    val runOut1: Int = 0,
    val runOut2: Int = 0,
    val goldenBreak1: Int = 0,
    val goldenBreak2: Int = 0,
    val dryBreak1: Int = 0,
    val dryBreak2: Int = 0,
    val eightBallLoss1: Int = 0,
    val eightBallLoss2: Int = 0,
    val highRun1: Int = 0,
    val highRun2: Int = 0,
    /** Points scored in the current unfinished 14/1 inning. */
    val currentRun: Int = 0,
    val currentBreakerId: PlayerId,
    /** Player with the table (14/1). For races, kept in sync with [currentBreakerId]. */
    val currentShooterId: PlayerId,
    /** True at match start and after a 14/1 three-foul penalty (must open-break). */
    val awaitingOpeningBreak: Boolean = false,
    val status: MatchStatus = MatchStatus.IN_PROGRESS,
    val startedAtMillis: Long = 0L,
    val history: List<MatchEvent> = emptyList(),
) {
    init {
        require(player1.id != player2.id) { "players must be distinct" }
        require(currentBreakerId == player1.id || currentBreakerId == player2.id) {
            "breaker must be one of the match players"
        }
        require(currentShooterId == player1.id || currentShooterId == player2.id) {
            "shooter must be one of the match players"
        }
        if (gameMode.isPointScoring) {
            require(pointsToWin > 0) { "pointsToWin must be positive for 14/1" }
            require(inningsLimitBase == null || inningsLimitBase > 0) {
                "inningsLimitBase must be positive when set"
            }
            require(inningsLimit == null || inningsLimit > 0) {
                "inningsLimit must be positive when set"
            }
        } else {
            require(racksToWin > 0) { "racksToWin must be positive" }
        }
    }

    val winner: Player?
        get() = when {
            status != MatchStatus.COMPLETED -> null
            gameMode.isPointScoring -> when {
                score1 > score2 -> player1
                score2 > score1 -> player2
                else -> null
            }
            score1 >= racksToWin -> player1
            score2 >= racksToWin -> player2
            else -> null
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
            startedAtMillis: Long,
            gameMode: GameMode = GameMode.TEN_BALL,
            pointsToWin: Int = 0,
            inningsLimit: Int? = null,
        ): Match {
            val p1 = Player(PlayerId("p1"), player1Name.trim().ifEmpty { "Player 1" })
            val p2 = Player(PlayerId("p2"), player2Name.trim().ifEmpty { "Player 2" })
            val starter = if (initialBreakerIsPlayer1) p1.id else p2.id
            return Match(
                player1 = p1,
                player2 = p2,
                gameMode = gameMode,
                racksToWin = if (gameMode.isPointScoring) 1 else racksToWin,
                pointsToWin = pointsToWin,
                inningsLimitBase = inningsLimit,
                inningsLimit = inningsLimit,
                currentBreakerId = starter,
                currentShooterId = starter,
                awaitingOpeningBreak = gameMode.isPointScoring,
                startedAtMillis = startedAtMillis,
            )
        }
    }
}
