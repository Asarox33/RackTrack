package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.Match
import com.racktrack.domain.model.MatchEvent
import com.racktrack.domain.model.MatchEventType
import com.racktrack.domain.model.PauseSpan
import com.racktrack.domain.model.PlayerId
import com.racktrack.domain.model.pausedMillisBetween

data class RackStat(
    val index: Int,
    val winnerName: String,
    val durationMillis: Long,
    val endType: MatchEventType,
)

/**
 * One finished (or match-winning open) 14/1 visit.
 * [points] is the net for that visit (pocketed + foul penalties) so lines sum to the match score.
 */
data class InningStat(
    val index: Int,
    val points: Int,
    val endType: MatchEventType?,
)

data class MatchSummary(
    val gameMode: GameMode,
    val winnerName: String,
    val player1Name: String,
    val player2Name: String,
    val score1: Int,
    val score2: Int,
    val racksToWin: Int,
    val pointsToWin: Int,
    val inningsLimit: Int?,
    val innings1: Int,
    val innings2: Int,
    val totalFouls1: Int,
    val totalFouls2: Int,
    val runOuts1: Int,
    val runOuts2: Int,
    val goldenBreaks1: Int,
    val goldenBreaks2: Int,
    val dryBreaks1: Int,
    val dryBreaks2: Int,
    val eightBallLosses1: Int,
    val eightBallLosses2: Int,
    val highRun1: Int,
    val highRun2: Int,
    /** FFB 1.2.20 — points / innings played (0 if no innings). */
    val average1: Double,
    val average2: Double,
    val inningScores1: List<InningStat>,
    val inningScores2: List<InningStat>,
    val racks: List<RackStat>,
    val totalDurationMillis: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    /** 14/1 solo training snapshot; false for races and duel 14/1. */
    val solo: Boolean = false,
)

object MatchStats {
    fun summarize(match: Match): MatchSummary {
        val racks = if (match.gameMode.isPointScoring) emptyList() else rackStats(match)
        val inningScores1 =
            if (match.gameMode.isPointScoring) inningScores(match.history, match.player1.id) else emptyList()
        val inningScores2 =
            if (match.gameMode.isPointScoring && !match.solo) {
                inningScores(match.history, match.player2.id)
            } else {
                emptyList()
            }
        // Include an open winning visit so Inn / avg match the INNINGS table.
        val inningsPlayed1 =
            if (match.gameMode.isPointScoring) inningScores1.size else match.innings1
        val inningsPlayed2 =
            if (match.gameMode.isPointScoring) inningScores2.size else match.innings2
        val endMillis = match.history.lastOrNull()?.atMillis ?: match.startedAtMillis
        val startedAt = match.startedAtMillis
        return MatchSummary(
            gameMode = match.gameMode,
            winnerName = match.winner?.name.orEmpty(),
            player1Name = match.player1.name,
            player2Name = match.player2.name,
            score1 = match.score1,
            score2 = if (match.solo) 0 else match.score2,
            racksToWin = match.racksToWin,
            pointsToWin = match.pointsToWin,
            inningsLimit = match.inningsLimit,
            innings1 = inningsPlayed1,
            innings2 = inningsPlayed2,
            totalFouls1 = totalFouls(match.history, match.player1.id),
            totalFouls2 = if (match.solo) 0 else totalFouls(match.history, match.player2.id),
            runOuts1 = match.runOut1,
            runOuts2 = if (match.solo) 0 else match.runOut2,
            goldenBreaks1 = match.goldenBreak1,
            goldenBreaks2 = if (match.solo) 0 else match.goldenBreak2,
            dryBreaks1 = match.dryBreak1,
            dryBreaks2 = if (match.solo) 0 else match.dryBreak2,
            eightBallLosses1 = match.eightBallLoss1,
            eightBallLosses2 = if (match.solo) 0 else match.eightBallLoss2,
            highRun1 = match.highRun1,
            highRun2 = if (match.solo) 0 else match.highRun2,
            average1 = average(match.score1, inningsPlayed1),
            average2 = if (match.solo) 0.0 else average(match.score2, inningsPlayed2),
            inningScores1 = inningScores1,
            inningScores2 = inningScores2,
            racks = racks,
            totalDurationMillis = playingDuration(startedAt, endMillis, match.pauseSpans),
            startedAtMillis = startedAt,
            endedAtMillis = endMillis.coerceAtLeast(startedAt),
            solo = match.solo,
        )
    }

    private fun playingDuration(from: Long, to: Long, pauses: List<PauseSpan>): Long =
        ((to - from) - pauses.pausedMillisBetween(from, to)).coerceAtLeast(0L)

    /**
     * Net points per visit for [playerId], in order (pocketed + FOUL/BREAK/−15).
     * A visit ends on PASS / FOUL / BREAK_FOUL; an open visit at match end is kept.
     */
    fun inningScores(history: List<MatchEvent>, playerId: PlayerId): List<InningStat> {
        val result = mutableListOf<InningStat>()
        var points = 0
        var open = false
        for (event in history) {
            if (event.playerId != playerId) continue
            when (event.type) {
                MatchEventType.POINTS -> {
                    points += event.value
                    open = true
                }
                MatchEventType.PASS -> {
                    result += InningStat(
                        index = result.size + 1,
                        points = points,
                        endType = MatchEventType.PASS,
                    )
                    points = 0
                    open = false
                }
                MatchEventType.FOUL,
                MatchEventType.BREAK_FOUL,
                -> {
                    points += event.value
                    result += InningStat(
                        index = result.size + 1,
                        points = points,
                        endType = event.type,
                    )
                    points = 0
                    open = false
                }
                MatchEventType.THREE_FOUL_PENALTY -> {
                    // Extra −15 is part of the same visit as the 3rd FOUL just closed.
                    if (result.isNotEmpty()) {
                        val last = result.removeAt(result.lastIndex)
                        result += last.copy(points = last.points + event.value)
                    }
                }
                else -> Unit
            }
        }
        if (open) {
            result += InningStat(
                index = result.size + 1,
                points = points,
                endType = null,
            )
        }
        return result
    }

    private fun totalFouls(history: List<MatchEvent>, playerId: PlayerId): Int =
        history.count { event ->
            when (event.type) {
                MatchEventType.FOUL,
                MatchEventType.BREAK_FOUL,
                -> event.playerId == playerId
                MatchEventType.THREE_FOULS_LOSS -> event.playerId == playerId
                else -> false
            }
        }

    private fun rackStats(match: Match): List<RackStat> {
        val ending = match.history.filter { it.type.isRackEnding() }
        var segmentStart = match.startedAtMillis
        return ending.mapIndexed { index, event ->
            val winnerId = rackWinnerId(match, event)
            val winnerName = when (winnerId) {
                match.player1.id -> match.player1.name
                else -> match.player2.name
            }
            val duration = playingDuration(segmentStart, event.atMillis, match.pauseSpans)
            segmentStart = event.atMillis
            RackStat(
                index = index + 1,
                winnerName = winnerName,
                durationMillis = duration,
                endType = event.type,
            )
        }
    }

    private fun average(points: Int, innings: Int): Double =
        if (innings <= 0) 0.0 else points.toDouble() / innings.toDouble()

    private fun rackWinnerId(match: Match, event: MatchEvent): PlayerId =
        when (event.type) {
            MatchEventType.PLUS_ONE,
            MatchEventType.RUN_OUT,
            MatchEventType.GOLDEN_BREAK,
            -> event.playerId
            MatchEventType.THREE_FOULS_LOSS,
            MatchEventType.EIGHT_BALL_LOSS,
            -> match.otherPlayerId(event.playerId)
            MatchEventType.FOUL,
            MatchEventType.FOULS_CLEARED,
            MatchEventType.DRY_BREAK,
            MatchEventType.POINTS,
            MatchEventType.PASS,
            MatchEventType.BREAK_FOUL,
            MatchEventType.THREE_FOUL_PENALTY,
            -> error("${event.type} is not a rack-ending event")
        }

    private fun MatchEventType.isRackEnding(): Boolean =
        this == MatchEventType.PLUS_ONE ||
            this == MatchEventType.RUN_OUT ||
            this == MatchEventType.GOLDEN_BREAK ||
            this == MatchEventType.THREE_FOULS_LOSS ||
            this == MatchEventType.EIGHT_BALL_LOSS
}
