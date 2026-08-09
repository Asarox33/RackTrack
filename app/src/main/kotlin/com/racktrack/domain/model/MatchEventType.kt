package com.racktrack.domain.model

enum class MatchEventType {
    PLUS_ONE,
    RUN_OUT,
    FOUL,

    /**
     * Race board — consecutive fouls cleared after a legal shot (no shot-by-shot entry).
     * Player identified by [MatchEvent.playerId].
     */
    FOULS_CLEARED,

    /** Player identified by [MatchEvent.playerId] lost the rack on a 3rd consecutive foul. */
    THREE_FOULS_LOSS,

    /**
     * 9-ball only — legal break that pockets the 9 (FFB art. 1.4.03).
     * Awards the rack to the breaker.
     */
    GOLDEN_BREAK,

    /**
     * 9-ball / 8-ball — break with no object ball pocketed (stat; does not end the rack).
     */
    DRY_BREAK,

    /**
     * 8-ball — player identified by [MatchEvent.playerId] lost the rack under FFB art. 1.3.06
     * (early 8, 8 off the table except on break, 8 pocketed with a foul, or wrong pocket on 8).
     */
    EIGHT_BALL_LOSS,

    /** 14/1 — points added this shot/burst ([MatchEvent.value] > 0). */
    POINTS,

    /** 14/1 — legal end of inning (pass / miss). */
    PASS,

    /** 14/1 — illegal opening break (FFB 1.6.03, [MatchEvent.value] typically -2). */
    BREAK_FOUL,

    /**
     * 14/1 — extra −15 after the 3rd consecutive foul (FFB 1.6.07).
     * The −1 for that foul is a separate [FOUL] event immediately before.
     */
    THREE_FOUL_PENALTY,
}
