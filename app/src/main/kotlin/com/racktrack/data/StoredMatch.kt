package com.racktrack.data

import com.racktrack.domain.MatchSummary

/** Persisted completed match — [summary] is exactly what the end-of-match modal shows. */
data class StoredMatch(
    val id: String,
    val completedAtMillis: Long,
    val summary: MatchSummary,
)
