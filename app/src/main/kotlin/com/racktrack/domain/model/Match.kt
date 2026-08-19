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
    /** Race only — who breaks after each rack. Ignored for 14/1. */
    val breakRule: BreakRule = BreakRule.ALTERNATE,
    /** First breaker at match start (used to rebuild breaker on undo). */
    val openingBreakerId: PlayerId? = null,
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
    /**
     * 14/1 — object balls still on the table (1..15).
     * Continuous re-rack restores 15 as soon as only the keyball would remain.
     */
    val objectBallsOnTable: Int = OBJECT_BALLS_FULL_RACK,
    val currentBreakerId: PlayerId,
    /** Player with the table (14/1). For races, kept in sync with [currentBreakerId]. */
    val currentShooterId: PlayerId,
    /** True at match start and after a 14/1 three-foul penalty (must open-break). */
    val awaitingOpeningBreak: Boolean = false,
    /** 9/10 push-out window / decision tree (ignored for 8-ball / 14/1). */
    val pushOutPhase: PushOutPhase = PushOutPhase.NONE,
    val status: MatchStatus = MatchStatus.IN_PROGRESS,
    val startedAtMillis: Long = 0L,
    /**
     * Completed club pauses (not FFB timeouts). Excluded from duration math only —
     * never exported to summary / PDF lines.
     */
    val pauseSpans: List<PauseSpan> = emptyList(),
    val history: List<MatchEvent> = emptyList(),
    /**
     * 14/1 solo training — one shooter, placeholder [player2], no handoff.
     * Ignored for race modes (must stay false).
     */
    val solo: Boolean = false,
) {
    init {
        require(player1.id != player2.id) { "players must be distinct" }
        require(!solo || gameMode.isPointScoring) { "solo is only valid for 14/1" }
        require(currentBreakerId == player1.id || currentBreakerId == player2.id) {
            "breaker must be one of the match players"
        }
        require(currentShooterId == player1.id || currentShooterId == player2.id) {
            "shooter must be one of the match players"
        }
        val opening = openingBreakerId
        if (opening != null) {
            require(opening == player1.id || opening == player2.id) {
                "opening breaker must be one of the match players"
            }
        }
        if (solo) {
            require(currentBreakerId == player1.id) { "solo breaker must be player1" }
            require(currentShooterId == player1.id) { "solo shooter must be player1" }
        }
        if (gameMode.isPointScoring) {
            require(pointsToWin > 0) { "pointsToWin must be positive for 14/1" }
            require(inningsLimitBase == null || inningsLimitBase > 0) {
                "inningsLimitBase must be positive when set"
            }
            require(inningsLimit == null || inningsLimit > 0) {
                "inningsLimit must be positive when set"
            }
            require(objectBallsOnTable in 1..OBJECT_BALLS_FULL_RACK) {
                "objectBallsOnTable must be 1..$OBJECT_BALLS_FULL_RACK"
            }
        } else {
            require(racksToWin > 0) { "racksToWin must be positive" }
        }
    }

    val winner: Player?
        get() = when {
            status != MatchStatus.COMPLETED -> null
            gameMode.isPointScoring -> when {
                solo -> player1
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
        /** Placeholder [player2] name for 14/1 solo training matches. */
        const val SOLO_PLAYER2_NAME = "solo"

        fun start(
            player1Name: String,
            player2Name: String,
            racksToWin: Int,
            initialBreakerIsPlayer1: Boolean,
            startedAtMillis: Long,
            gameMode: GameMode = GameMode.TEN_BALL,
            pointsToWin: Int = 0,
            inningsLimit: Int? = null,
            breakRule: BreakRule = BreakRule.ALTERNATE,
            solo: Boolean = false,
        ): Match {
            require(!solo || gameMode.isPointScoring) { "solo is only valid for 14/1" }
            val p1 = Player(PlayerId("p1"), player1Name.trim().ifEmpty { "Player 1" })
            val p2Name = if (solo) SOLO_PLAYER2_NAME else player2Name
            val p2 = Player(PlayerId("p2"), p2Name.trim().ifEmpty { "Player 2" })
            val starter = when {
                solo -> p1.id
                initialBreakerIsPlayer1 -> p1.id
                else -> p2.id
            }
            return Match(
                player1 = p1,
                player2 = p2,
                gameMode = gameMode,
                racksToWin = if (gameMode.isPointScoring) 1 else racksToWin,
                pointsToWin = pointsToWin,
                inningsLimitBase = inningsLimit,
                inningsLimit = inningsLimit,
                breakRule = if (gameMode.isPointScoring) BreakRule.ALTERNATE else breakRule,
                openingBreakerId = starter,
                objectBallsOnTable = OBJECT_BALLS_FULL_RACK,
                currentBreakerId = starter,
                currentShooterId = starter,
                awaitingOpeningBreak = gameMode.isPointScoring,
                pushOutPhase = if (gameMode.supportsPushOut) {
                    PushOutPhase.AVAILABLE
                } else {
                    PushOutPhase.NONE
                },
                startedAtMillis = startedAtMillis,
                solo = solo,
            )
        }

        const val OBJECT_BALLS_FULL_RACK = 15
    }
}
