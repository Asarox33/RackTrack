package com.racktrack.domain.model

data class MatchEvent(
    val type: MatchEventType,
    val playerId: PlayerId,
    val atMillis: Long,
    /** Points delta for 14/1 events; unused (0) for race events. */
    val value: Int = 0,
)
