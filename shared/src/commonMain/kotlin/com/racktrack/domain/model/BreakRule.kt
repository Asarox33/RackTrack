package com.racktrack.domain.model

/** Who breaks the next race rack after a rack is awarded. */
enum class BreakRule {
    /** Always the other player than the previous rack's breaker. */
    ALTERNATE,

    /** The player who won the rack breaks the next one. */
    WINNER,
}
