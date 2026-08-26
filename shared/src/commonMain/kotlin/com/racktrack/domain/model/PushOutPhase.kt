package com.racktrack.domain.model

/**
 * 9/10-ball push-out decision tree (FFB) on the race scoreboard.
 * No geometry — operator judges legal break / clean vs foul.
 */
enum class PushOutPhase {
    /** Mode does not support push-out, or window closed for this rack. */
    NONE,

    /** After rack start / legal break — shooter may announce push-out. */
    AVAILABLE,

    /** Push-out announced; waiting for clean vs foul resolution. */
    ANNOUNCED,

    /** Clean push-out — opponent chooses TAKE or RETURN (give back). */
    AWAITING_CHOICE,
}
