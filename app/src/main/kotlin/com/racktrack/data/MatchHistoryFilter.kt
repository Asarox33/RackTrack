package com.racktrack.data

import com.racktrack.domain.model.GameMode

/**
 * Filters stored matches by game mode and up to two player-name queries
 * (case-insensitive contains). Player filters are order-independent.
 */
object MatchHistoryFilter {
    fun apply(
        matches: List<StoredMatch>,
        playerQuery1: String,
        playerQuery2: String,
        gameMode: GameMode? = null,
    ): List<StoredMatch> {
        val q1 = playerQuery1.trim()
        val q2 = playerQuery2.trim()
        return matches.filter { stored ->
            if (gameMode != null && stored.summary.gameMode != gameMode) return@filter false
            if (q1.isEmpty() && q2.isEmpty()) return@filter true
            val names = listOf(
                stored.summary.player1Name,
                stored.summary.player2Name,
            )
            matchesQueries(names, q1, q2)
        }
    }

    internal fun matchesQueries(
        playerNames: List<String>,
        query1: String,
        query2: String,
    ): Boolean {
        val q1 = query1.trim()
        val q2 = query2.trim()
        val has1 = q1.isNotEmpty()
        val has2 = q2.isNotEmpty()
        fun contains(query: String): Boolean =
            playerNames.any { it.contains(query, ignoreCase = true) }

        return when {
            has1 && has2 -> {
                // Both names must appear among the two players (order free).
                if (q1.equals(q2, ignoreCase = true)) {
                    contains(q1)
                } else {
                    val aMatches1 = playerNames[0].contains(q1, ignoreCase = true)
                    val aMatches2 = playerNames[0].contains(q2, ignoreCase = true)
                    val bMatches1 = playerNames[1].contains(q1, ignoreCase = true)
                    val bMatches2 = playerNames[1].contains(q2, ignoreCase = true)
                    (aMatches1 && bMatches2) || (aMatches2 && bMatches1)
                }
            }
            has1 -> contains(q1)
            has2 -> contains(q2)
            else -> true
        }
    }
}
