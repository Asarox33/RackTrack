package com.racktrack.domain

import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.MatchEventType
import com.racktrack.i18n.StringKey
import com.racktrack.i18n.StringProvider
import com.racktrack.i18n.Strings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

data class PairedInningRow(
    val index: Int,
    val player1: InningStat?,
    val player2: InningStat?,
    /** Running total after this inning index (null until that player’s first visit). */
    val total1: Int?,
    val total2: Int?,
)

data class SoloInningRow(
    val inning: InningStat,
    val total: Int,
)

/** Shared copy/helpers for match summary UI and PDF export. */
object MatchSummaryReport {
    fun modeLabel(
        mode: GameMode,
        provider: StringProvider = Strings.provider,
    ): String =
        when (mode) {
            GameMode.EIGHT_BALL -> provider.get(StringKey.MODE_8_BALL)
            GameMode.NINE_BALL -> provider.get(StringKey.MODE_9_BALL)
            GameMode.TEN_BALL -> provider.get(StringKey.MODE_10_BALL)
            GameMode.FOURTEEN_ONE -> provider.get(StringKey.MODE_14_1)
        }

    /** End-of-session modal / PDF hero title. */
    fun sessionOverTitle(
        summary: MatchSummary,
        provider: StringProvider = Strings.provider,
    ): String = provider.get(if (summary.solo) StringKey.TRAINING_OVER else StringKey.MATCH_OVER)

    /** Default PDF / text report title. */
    fun sessionSummaryTitle(
        summary: MatchSummary,
        provider: StringProvider = Strings.provider,
    ): String = provider.get(if (summary.solo) StringKey.TRAINING_SUMMARY else StringKey.MATCH_SUMMARY)

    fun shareChooserLabel(
        summary: MatchSummary,
        provider: StringProvider = Strings.provider,
    ): String = provider.get(if (summary.solo) StringKey.SHARE_TRAINING else StringKey.SHARE_MATCH)

    fun opponentsLabel(
        summary: MatchSummary,
        provider: StringProvider = Strings.provider,
    ): String =
        if (summary.solo) {
            Strings.format(provider, StringKey.OPPONENTS_SOLO, summary.player1Name)
        } else {
            Strings.format(provider, StringKey.OPPONENTS_VS, summary.player1Name, summary.player2Name)
        }

    fun subtitle(
        summary: MatchSummary,
        provider: StringProvider = Strings.provider,
    ): String =
        if (summary.gameMode.isPointScoring) {
            if (summary.solo) {
                val innings =
                    summary.inningsLimit?.let {
                        Strings.format(
                            provider,
                            StringKey.SUBTITLE_INNINGS_LIMITED,
                            summary.innings1,
                            it,
                        )
                    } ?: Strings.format(provider, StringKey.SUBTITLE_INNINGS, summary.innings1)
                Strings.format(
                    provider,
                    StringKey.SUBTITLE_14_1_SOLO,
                    summary.score1,
                    summary.pointsToWin,
                    innings,
                )
            } else {
                val innings =
                    summary.inningsLimit?.let {
                        Strings.format(
                            provider,
                            StringKey.SUBTITLE_INNINGS_PAIR_LIMITED,
                            summary.innings1,
                            summary.innings2,
                            it,
                        )
                    } ?: Strings.format(
                        provider,
                        StringKey.SUBTITLE_INNINGS_PAIR,
                        summary.innings1,
                        summary.innings2,
                    )
                Strings.format(
                    provider,
                    StringKey.SUBTITLE_14_1,
                    summary.score1,
                    summary.score2,
                    summary.pointsToWin,
                    innings,
                )
            }
        } else {
            Strings.format(
                provider,
                StringKey.SUBTITLE_RACE,
                modeLabel(summary.gameMode, provider),
                summary.score1,
                summary.score2,
                summary.racksToWin,
            )
        }

    fun formatDuration(millis: Long): String {
        val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            "$hours:${pad2(minutes)}:${pad2(seconds)}"
        } else {
            "$minutes:${pad2(seconds)}"
        }
    }

    fun rackEndLabel(
        type: MatchEventType,
        provider: StringProvider = Strings.provider,
    ): String =
        when (type) {
            MatchEventType.PLUS_ONE -> "+1"
            MatchEventType.RUN_OUT -> provider.get(StringKey.RACK_END_RUN_OUT)
            MatchEventType.GOLDEN_BREAK -> provider.get(StringKey.RACK_END_GOLDEN)
            MatchEventType.EIGHT_BALL_LOSS -> provider.get(StringKey.RACK_END_EARLY_8)
            MatchEventType.THREE_FOULS_LOSS -> provider.get(StringKey.RACK_END_THREE_FOULS)
            else -> ""
        }

    fun inningEndLabel(
        type: MatchEventType?,
        provider: StringProvider = Strings.provider,
    ): String =
        when (type) {
            MatchEventType.PASS -> provider.get(StringKey.INNING_END_PASS)
            MatchEventType.FOUL -> provider.get(StringKey.INNING_END_FOUL)
            MatchEventType.BREAK_FOUL -> provider.get(StringKey.INNING_END_BRK)
            MatchEventType.ACCEPT_ILLEGAL_OPEN -> provider.get(StringKey.INNING_END_ACC)
            null -> provider.get(StringKey.INNING_END_WIN)
            else -> ""
        }

    /**
     * One row per inning index for both players (14/1 score sheet style).
     * Column order for UI/PDF: `# | End | Pts | Tot | Tot | Pts | End`
     * (player 1 left of center, player 2 right). Missing visit → null / —.
     */
    fun pairedInningRows(
        innings1: List<InningStat>,
        innings2: List<InningStat>,
    ): List<PairedInningRow> {
        val byIndex1 = innings1.associateBy { it.index }
        val byIndex2 = innings2.associateBy { it.index }
        val maxIndex =
            maxOf(
                byIndex1.keys.maxOrNull() ?: 0,
                byIndex2.keys.maxOrNull() ?: 0,
            )
        if (maxIndex <= 0) return emptyList()
        var run1 = 0
        var run2 = 0
        var seen1 = false
        var seen2 = false
        return (1..maxIndex).map { index ->
            val p1 = byIndex1[index]
            val p2 = byIndex2[index]
            if (p1 != null) {
                run1 += p1.points
                seen1 = true
            }
            if (p2 != null) {
                run2 += p2.points
                seen2 = true
            }
            PairedInningRow(
                index = index,
                player1 = p1,
                player2 = p2,
                total1 = if (seen1) run1 else null,
                total2 = if (seen2) run2 else null,
            )
        }
    }

    /** Solo 14/1 rows: `# | End | Pts | Tot`. */
    fun soloInningRows(innings: List<InningStat>): List<SoloInningRow> {
        var run = 0
        return innings.sortedBy { it.index }.map { inning ->
            run += inning.points
            SoloInningRow(inning = inning, total = run)
        }
    }

    fun playerStatLines(
        summary: MatchSummary,
        side: Int,
        provider: StringProvider = Strings.provider,
    ): List<String> {
        val fouls = if (side == 1) summary.totalFouls1 else summary.totalFouls2
        val runOuts = if (side == 1) summary.runOuts1 else summary.runOuts2
        val golden = if (side == 1) summary.goldenBreaks1 else summary.goldenBreaks2
        val dry = if (side == 1) summary.dryBreaks1 else summary.dryBreaks2
        val pushOuts = if (side == 1) summary.pushOuts1 else summary.pushOuts2
        val early8 = if (side == 1) summary.eightBallLosses1 else summary.eightBallLosses2
        val highRun = if (side == 1) summary.highRun1 else summary.highRun2
        val average = if (side == 1) summary.average1 else summary.average2
        val innings = if (side == 1) summary.innings1 else summary.innings2
        return if (summary.gameMode.isPointScoring) {
            listOf(
                Strings.format(provider, StringKey.STAT_HR, highRun),
                Strings.format(provider, StringKey.STAT_AVG, formatAverage(average)),
                Strings.format(provider, StringKey.STAT_INN, innings),
                Strings.format(provider, StringKey.STAT_FOULS, fouls),
            )
        } else {
            buildList {
                add(Strings.format(provider, StringKey.STAT_RUN_OUTS, runOuts))
                add(Strings.format(provider, StringKey.STAT_FOULS, fouls))
                if (summary.gameMode.supportsGoldenBreak) {
                    add(Strings.format(provider, StringKey.STAT_GOLDEN, golden))
                }
                if (summary.gameMode.supportsEightBallLoss) {
                    add(Strings.format(provider, StringKey.STAT_EARLY_8, early8))
                }
                if (summary.gameMode.supportsDryBreak) {
                    add(Strings.format(provider, StringKey.STAT_DRY, dry))
                }
                if (summary.gameMode.supportsPushOut) {
                    add(Strings.format(provider, StringKey.STAT_PUSH_OUTS, pushOuts))
                }
            }
        }
    }

    fun fileStem(summary: MatchSummary): String {
        val mode =
            when (summary.gameMode) {
                GameMode.EIGHT_BALL -> "8ball"
                GameMode.NINE_BALL -> "9ball"
                GameMode.TEN_BALL -> "10ball"
                GameMode.FOURTEEN_ONE -> "14-1"
            }
        val p1 = sanitize(summary.player1Name)
        val startStamp = fileStartStamp(summary.startedAtMillis)
        return if (summary.solo) {
            "racktrack_${mode}_${p1}_solo_$startStamp"
        } else {
            val p2 = sanitize(summary.player2Name)
            "racktrack_${mode}_${p1}_vs_${p2}_$startStamp"
        }
    }

    /** `yyyyMMdd_HHmm` from match start, for filenames. */
    fun fileStartStamp(startedAtMillis: Long): String {
        val local =
            Instant
                .fromEpochMilliseconds(startedAtMillis)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        return buildString {
            append(pad4(local.year))
            append(pad2(local.month.number))
            append(pad2(local.day))
            append('_')
            append(pad2(local.hour))
            append(pad2(local.minute))
        }
    }

    private fun pad2(value: Int): String = value.toString().padStart(PAD2, '0')

    private fun pad2(value: Long): String = value.toString().padStart(PAD2, '0')

    private fun pad4(value: Int): String = value.toString().padStart(PAD4, '0')

    /** Two decimal places without JVM String.format. */
    private fun formatAverage(value: Double): String {
        val scaled = kotlin.math.round(value * AVERAGE_SCALE).toLong()
        val whole = scaled / AVERAGE_SCALE_LONG
        val frac = kotlin.math.abs(scaled % AVERAGE_SCALE_LONG)
        return "$whole.${pad2(frac)}"
    }

    private const val PAD2 = 2
    private const val PAD4 = 4
    private const val AVERAGE_SCALE = 100.0
    private const val AVERAGE_SCALE_LONG = 100L

    /** Compact text form kept for unit tests / debugging. */
    fun lines(
        summary: MatchSummary,
        title: String = sessionSummaryTitle(summary),
        startedAtLabel: String? = null,
        endedAtLabel: String? = null,
        provider: StringProvider = Strings.provider,
    ): List<String> =
        buildList {
            add(title)
            if (startedAtLabel != null) {
                add("${provider.get(StringKey.STARTED)}  $startedAtLabel")
            }
            if (endedAtLabel != null) {
                add("${provider.get(StringKey.ENDED)}  $endedAtLabel")
            }
            add(
                "${provider.get(StringKey.DURATION)}  ${formatDuration(summary.totalDurationMillis)}",
            )
            add("")
            val winner =
                summary.winnerName.ifEmpty { provider.get(StringKey.DRAW) }
            add(winner.uppercase())
            if (summary.winnerName.isNotEmpty()) add(provider.get(StringKey.WINS))
            add(subtitle(summary, provider))
            add("")
            add(summary.player1Name.uppercase())
            addAll(playerStatLines(summary, 1, provider).map { "  $it" })
            if (!summary.solo) {
                add("")
                add(summary.player2Name.uppercase())
                addAll(playerStatLines(summary, 2, provider).map { "  $it" })
            }
            add("")
            if (summary.gameMode.isPointScoring) {
                add(provider.get(StringKey.INNINGS_SECTION))
                if (summary.solo) {
                    add(
                        "#  ${provider.get(StringKey.COL_END)}  " +
                            "${provider.get(StringKey.COL_PTS)}  ${provider.get(StringKey.COL_TOT)}",
                    )
                    soloInningRows(summary.inningScores1).forEach { row ->
                        add(
                            "#${row.inning.index}  ${inningEndLabel(row.inning.endType, provider)}  " +
                                "${row.inning.points}  ${row.total}",
                        )
                    }
                } else {
                    add(
                        "#  ${provider.get(StringKey.COL_END)}  " +
                            "${provider.get(StringKey.COL_PTS)}  ${provider.get(StringKey.COL_TOT)}  " +
                            "${provider.get(StringKey.COL_TOT)}  ${provider.get(StringKey.COL_PTS)}  " +
                            provider.get(StringKey.COL_END),
                    )
                    pairedInningRows(summary.inningScores1, summary.inningScores2).forEach { row ->
                        val end1 = row.player1?.let { inningEndLabel(it.endType, provider) } ?: "—"
                        val pts1 = row.player1?.points?.toString() ?: "—"
                        val tot1 = row.total1?.toString() ?: "—"
                        val tot2 = row.total2?.toString() ?: "—"
                        val pts2 = row.player2?.points?.toString() ?: "—"
                        val end2 = row.player2?.let { inningEndLabel(it.endType, provider) } ?: "—"
                        add("#${row.index}  $end1  $pts1  $tot1  $tot2  $pts2  $end2")
                    }
                }
            } else {
                add(provider.get(StringKey.RACKS_SECTION))
                summary.racks.forEach { rack ->
                    add(
                        "#${rack.index}  ${rack.winnerName}  " +
                            "${rackEndLabel(rack.endType, provider)}  " +
                            formatDuration(rack.durationMillis),
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
