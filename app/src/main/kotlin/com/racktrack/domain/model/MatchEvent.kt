package com.racktrack.domain.model

data class MatchEvent(
    val type: MatchEventType,
    val playerId: PlayerId,
)
