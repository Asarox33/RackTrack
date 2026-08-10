package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType

/** Shared copy/helpers for match summary UI and PDF export. */
object MatchSummaryReport {
    fun modeLabel(mode: GameMode): String =
        when (mode) {
            GameMode.EIGHT_BALL -> "8-BALL"
            GameMode.NINE_BALL -> "9-BALL"
            GameMode.TEN_BALL -> "10-BALL"
            GameMode.FOURTEEN_ONE -> "14/1"
        }

    fun subtitle(summary: MatchSummary): String =
        if (summary.gameMode.isPointScoring) {
            val innings = summary.inningsLimit?.let {
                "${summary.innings1}/${summary.innings2} inn · lim $it"
            } ?: "${summary.innings1}/${summary.innings2} inn"
            "14/1  ·  ${summary.score1} – ${summary.score2}  ·  ${summary.pointsToWin} pts  ·  $innings"
        } else {
            "${modeLabel(summary.gameMode)}  ·  ${summary.score1} – ${summary.score2}  ·  " +
                "race to ${summary.racksToWin}"
        }

    fun formatDuration(millis: Long): String {
        val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    fun rackEndLabel(type: MatchEventType): String =
        when (type) {
            MatchEventType.PLUS_ONE -> "+1"
            MatchEventType.RUN_OUT -> "Run out"
            MatchEventType.GOLDEN_BREAK -> "Golden"
            MatchEventType.EIGHT_BALL_LOSS -> "Early 8"
            MatchEventType.THREE_FOULS_LOSS -> "3 fouls"
            else -> ""
        }

    fun inningEndLabel(type: MatchEventType?): String =
        when (type) {
            MatchEventType.PASS -> "pass"
            MatchEventType.FOUL -> "foul"
            MatchEventType.BREAK_FOUL -> "brk"
            null -> "win"
            else -> ""
        }

    fun playerStatLines(summary: MatchSummary, side: Int): List<String> {
        val fouls = if (side == 1) summary.totalFouls1 else summary.totalFouls2
        val runOuts = if (side == 1) summary.runOuts1 else summary.runOuts2
        val golden = if (side == 1) summary.goldenBreaks1 else summary.goldenBreaks2
        val dry = if (side == 1) summary.dryBreaks1 else summary.dryBreaks2
        val early8 = if (side == 1) summary.eightBallLosses1 else summary.eightBallLosses2
        val highRun = if (side == 1) summary.highRun1 else summary.highRun2
        val average = if (side == 1) summary.average1 else summary.average2
        val innings = if (side == 1) summary.innings1 else summary.innings2
        return if (summary.gameMode.isPointScoring) {
            listOf(
                "HR $highRun",
                "avg ${"%.2f".format(average)}",
                "Inn $innings",
                "Fouls $fouls",
            )
        } else {
            buildList {
                add("Run outs $runOuts")
                add("Fouls $fouls")
                if (summary.gameMode.supportsGoldenBreak) add("Golden $golden")
                if (summary.gameMode.supportsEightBallLoss) add("Early 8 $early8")
                if (summary.gameMode.supportsDryBreak) add("Dry $dry")
            }
        }
    }

    fun fileStem(summary: MatchSummary): String {
        val mode = when (summary.gameMode) {
            GameMode.EIGHT_BALL -> "8ball"
            GameMode.NINE_BALL -> "9ball"
            GameMode.TEN_BALL -> "10ball"
            GameMode.FOURTEEN_ONE -> "14-1"
        }
        val p1 = sanitize(summary.player1Name)
        val p2 = sanitize(summary.player2Name)
        val startStamp = fileStartStamp(summary.startedAtMillis)
        return "racktrack_${mode}_${p1}_vs_${p2}_$startStamp"
    }

    /** `yyyyMMdd_HHmm` from match start, for filenames. */
    fun fileStartStamp(startedAtMillis: Long): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = startedAtMillis }
        return "%04d%02d%02d_%02d%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
        )
    }

    /** Compact text form kept for unit tests / debugging. */
    fun lines(
        summary: MatchSummary,
        title: String = "MATCH SUMMARY",
        startedAtLabel: String? = null,
        endedAtLabel: String? = null,
    ): List<String> =
        buildList {
            add(title)
            if (startedAtLabel != null) add("Started  $startedAtLabel")
            if (endedAtLabel != null) add("Ended  $endedAtLabel")
            add("Duration  ${formatDuration(summary.totalDurationMillis)}")
            add("")
            val winner = summary.winnerName.ifEmpty { "DRAW" }
            add(winner.uppercase())
            if (summary.winnerName.isNotEmpty()) add("WINS")
            add(subtitle(summary))
            add("")
            add(summary.player1Name.uppercase())
            addAll(playerStatLines(summary, 1).map { "  $it" })
            add("")
            add(summary.player2Name.uppercase())
            addAll(playerStatLines(summary, 2).map { "  $it" })
            add("")
            if (summary.gameMode.isPointScoring) {
                add("REPRISES")
                add(summary.player1Name)
                summary.inningScores1.forEach {
                    add("  #${it.index}  ${it.points}  ${inningEndLabel(it.endType)}")
                }
                add(summary.player2Name)
                summary.inningScores2.forEach {
                    add("  #${it.index}  ${it.points}  ${inningEndLabel(it.endType)}")
                }
            } else {
                add("RACKS")
                summary.racks.forEach { rack ->
                    add(
                        "#${rack.index}  ${rack.winnerName}  " +
                            "${rackEndLabel(rack.endType)}  ${formatDuration(rack.durationMillis)}",
                    )
                }
            }
        }

    private fun sanitize(name: String): String =
        name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "player" }
            .take(24)
}
