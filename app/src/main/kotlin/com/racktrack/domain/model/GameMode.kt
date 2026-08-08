package com.racktrack.domain.model

/**
 * Official FFB American-pool modes supported by the race / points board.
 * Authority: `resources/code-sportif-americain-2026-2027.pdf`
 * — Chapter 3 (8-ball), 4 (9-ball), 5 (10-ball), 6 (14/1 continuous).
 */
enum class GameMode {
    EIGHT_BALL,
    NINE_BALL,
    TEN_BALL,
    FOURTEEN_ONE,
    ;

    val isPointScoring: Boolean
        get() = this == FOURTEEN_ONE

    /** FFB art. 1.4.03 — 9 pocketed on a legal break wins the rack. */
    val supportsGoldenBreak: Boolean
        get() = this == NINE_BALL

    /**
     * Break with no object ball pocketed (stat).
     * Relevant for 9-ball (art. 1.4.03) and 8-ball illegal/empty break (art. 1.3.03).
     */
    val supportsDryBreak: Boolean
        get() = this == NINE_BALL || this == EIGHT_BALL

    /** FFB arts 1.4.07 / 1.5.07 — not present in the 8-ball / 14.1 chapters. */
    val supportsThreeFoulRackLoss: Boolean
        get() = this == NINE_BALL || this == TEN_BALL

    /** FFB art. 1.3.06 — early 8 / 8 off table / foul on 8 / wrong pocket. */
    val supportsEightBallLoss: Boolean
        get() = this == EIGHT_BALL
}
