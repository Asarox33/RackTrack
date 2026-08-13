@file:Suppress("MagicNumber")

package com.racktrack.presentation.share

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.racktrack.domain.MatchSummary
import com.racktrack.domain.MatchSummaryReport
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Styled multi-page A4 PDF of a full [MatchSummary]
 * (felt header, player cards, tables — not a viewport screenshot).
 */
object MatchSummaryPdfWriter {
    fun writeToFile(
        summary: MatchSummary,
        output: File,
        title: String = "MATCH SUMMARY",
        accentArgb: Int = DEFAULT_ACCENT,
    ): File {
        val document = PdfDocument()
        val theme = Theme(accentArgb)
        val session = Session(document, theme)
        session.drawFullReport(summary, title)
        FileOutputStream(output).use { stream ->
            document.writeTo(stream)
        }
        document.close()
        return output
    }

    private class Theme(accent: Int) {
        val feltDark = darken(accent, 0.55f)
        val feltMid = darken(accent, 0.25f)
        val accent = accent
        val paper = 0xFFF7F3EA.toInt()
        val ink = 0xFF1A1F1C.toInt()
        val muted = 0xFF5C6560.toInt()
        val card = 0xFFFFFFFF.toInt()
        val rowAlt = 0xFFECE7DC.toInt()
        val line = 0xFFD0C8B8.toInt()
        val onFelt = 0xFFF5F7F4.toInt()
        val winner = 0xFFE8C96A.toInt()
    }

    private class Session(
        private val document: PdfDocument,
        private val theme: Theme,
    ) {
        private var pageNumber = 0
        private var page: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y = 0f

        private val brandPaint = paint(theme.onFelt, 11f, bold = true)
        private val titlePaint = paint(theme.onFelt, 22f, bold = true)
        private val winnerPaint = paint(theme.winner, 28f, bold = true)
        private val onFeltBody = paint(theme.onFelt, 12f)
        private val onFeltMuted = paint(0xFFC5D0C8.toInt(), 10f)
        private val sectionPaint = paint(theme.accent, 13f, bold = true)
        private val bodyPaint = paint(theme.ink, 11f)
        private val mutedPaint = paint(theme.muted, 10f)
        private val namePaint = paint(theme.ink, 13f, bold = true)
        private val footerPaint = paint(theme.muted, 9f)

        fun drawFullReport(summary: MatchSummary, title: String) {
            startPage(fullHeader = true)
            drawHero(summary, title)
            ensureSpace(PLAYER_CARD_HEIGHT + 24f)
            drawPlayerCards(summary)
            y += 18f
            if (summary.gameMode.isPointScoring) {
                drawSectionTitle("INNINGS")
                drawInningsTable(summary)
            } else {
                drawSectionTitle("RACKS")
                drawRacksTable(summary)
            }
            y += 20f
            ensureSpace(28f)
            canvas!!.drawText("Shared from RackTrack", MARGIN_LEFT, y, footerPaint)
            finishPage()
        }

        private fun drawHero(summary: MatchSummary, title: String) {
            val c = canvas!!
            val headerBottom = HEADER_HEIGHT
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), headerBottom, paint(theme.feltDark))
            // Accent stripe
            c.drawRect(0f, headerBottom - 4f, PAGE_WIDTH.toFloat(), headerBottom, paint(theme.accent))

            var hy = 32f
            c.drawText("RACKTRACK", MARGIN_LEFT, hy, brandPaint)
            hy += 26f
            c.drawText(title.uppercase(Locale.getDefault()), MARGIN_LEFT, hy, titlePaint)
            hy += 18f
            c.drawText("Started  ${formatDateTime(summary.startedAtMillis)}", MARGIN_LEFT, hy, onFeltMuted)
            hy += 14f
            c.drawText("Ended  ${formatDateTime(summary.endedAtMillis)}", MARGIN_LEFT, hy, onFeltMuted)
            hy += 14f
            c.drawText(
                "Duration  ${MatchSummaryReport.formatDuration(summary.totalDurationMillis)}",
                MARGIN_LEFT,
                hy,
                onFeltMuted,
            )
            hy += 24f
            val winner = summary.winnerName.ifEmpty { "DRAW" }.uppercase(Locale.getDefault())
            c.drawText(winner, MARGIN_LEFT, hy, winnerPaint)
            if (summary.winnerName.isNotEmpty()) {
                hy += 16f
                c.drawText("WINS", MARGIN_LEFT, hy, onFeltBody)
            }
            hy += 18f
            c.drawText(MatchSummaryReport.subtitle(summary), MARGIN_LEFT, hy, onFeltBody)
            y = headerBottom + 28f
        }

        private fun formatDateTime(millis: Long): String =
            DateFormat
                .getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault())
                .format(Date(millis))

        private fun drawContinuedHeader() {
            val c = canvas!!
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), CONTINUED_HEADER, paint(theme.feltMid))
            c.drawRect(
                0f,
                CONTINUED_HEADER - 3f,
                PAGE_WIDTH.toFloat(),
                CONTINUED_HEADER,
                paint(theme.accent),
            )
            c.drawText("RACKTRACK  ·  continued", MARGIN_LEFT, 28f, brandPaint)
            y = CONTINUED_HEADER + 24f
        }

        private fun drawPlayerCards(summary: MatchSummary) {
            val gap = 12f
            val cardWidth = (CONTENT_WIDTH - gap) / 2f
            val left = MARGIN_LEFT
            val right = MARGIN_LEFT + cardWidth + gap
            drawOnePlayerCard(summary, side = 1, left, y, cardWidth)
            drawOnePlayerCard(summary, side = 2, right, y, cardWidth)
            y += PLAYER_CARD_HEIGHT
        }

        private fun drawOnePlayerCard(
            summary: MatchSummary,
            side: Int,
            x: Float,
            top: Float,
            width: Float,
        ) {
            val c = canvas!!
            val rect = RectF(x, top, x + width, top + PLAYER_CARD_HEIGHT)
            c.drawRoundRect(rect, 10f, 10f, paint(theme.card))
            c.drawRoundRect(rect, 10f, 10f, stroke(theme.line, 1.2f))
            c.drawRect(x, top, x + 5f, top + PLAYER_CARD_HEIGHT, paint(theme.accent))

            val name = if (side == 1) summary.player1Name else summary.player2Name
            var cy = top + 22f
            c.drawText(name.uppercase(Locale.getDefault()), x + 16f, cy, namePaint)
            cy += 6f
            c.drawLine(x + 16f, cy, x + width - 16f, cy, stroke(theme.line, 1f))
            cy += 18f
            MatchSummaryReport.playerStatLines(summary, side).forEach { line ->
                c.drawText(line, x + 16f, cy, bodyPaint)
                cy += 15f
            }
        }

        private fun drawSectionTitle(label: String) {
            ensureSpace(36f)
            val c = canvas!!
            c.drawText(label, MARGIN_LEFT, y, sectionPaint)
            y += 6f
            c.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + 80f, y, stroke(theme.accent, 2f))
            y += 16f
        }

        private fun drawRacksTable(summary: MatchSummary) {
            if (summary.racks.isEmpty()) {
                ensureSpace(20f)
                canvas!!.drawText("No rack timings recorded", MARGIN_LEFT, y, mutedPaint)
                y += 16f
                return
            }
            drawTableHeader(listOf("#", "Winner", "End", "Time"), floatArrayOf(0.10f, 0.42f, 0.28f, 0.20f))
            summary.racks.forEachIndexed { index, rack ->
                ensureSpace(ROW_HEIGHT + 4f)
                drawTableRow(
                    cells = listOf(
                        "#${rack.index}",
                        rack.winnerName,
                        MatchSummaryReport.rackEndLabel(rack.endType),
                        MatchSummaryReport.formatDuration(rack.durationMillis),
                    ),
                    weights = floatArrayOf(0.10f, 0.42f, 0.28f, 0.20f),
                    alt = index % 2 == 1,
                )
            }
        }

        private fun drawInningsTable(summary: MatchSummary) {
            val rows = MatchSummaryReport.pairedInningRows(
                summary.inningScores1,
                summary.inningScores2,
            )
            if (rows.isEmpty()) {
                ensureSpace(20f)
                canvas!!.drawText("No innings recorded", MARGIN_LEFT, y, mutedPaint)
                y += 16f
                return
            }
            val name1 = truncateHeaderName(summary.player1Name)
            val name2 = truncateHeaderName(summary.player2Name)
            val weights = INNINGS_WEIGHTS
            drawTableHeader(
                cells = listOf("#", name1, "End", name2, "End"),
                weights = weights,
                separatorAfterColumns = INNINGS_SEPARATOR_AFTER,
            )
            rows.forEachIndexed { index, row ->
                ensureSpace(ROW_HEIGHT + 4f)
                drawTableRow(
                    cells = listOf(
                        "#${row.index}",
                        row.player1?.points?.toString() ?: "—",
                        row.player1?.let { MatchSummaryReport.inningEndLabel(it.endType) } ?: "—",
                        row.player2?.points?.toString() ?: "—",
                        row.player2?.let { MatchSummaryReport.inningEndLabel(it.endType) } ?: "—",
                    ),
                    weights = weights,
                    alt = index % 2 == 1,
                    separatorAfterColumns = INNINGS_SEPARATOR_AFTER,
                )
            }
        }

        private fun truncateHeaderName(name: String): String {
            val trimmed = name.trim().ifEmpty { "Player" }
            return if (trimmed.length <= INNINGS_NAME_MAX) {
                trimmed
            } else {
                trimmed.take(INNINGS_NAME_MAX - 1) + "…"
            }
        }

        private fun drawTableHeader(
            cells: List<String>,
            weights: FloatArray,
            separatorAfterColumns: IntArray = intArrayOf(),
        ) {
            ensureSpace(ROW_HEIGHT + 4f)
            val c = canvas!!
            val top = y
            c.drawRect(MARGIN_LEFT, top, MARGIN_LEFT + CONTENT_WIDTH, top + ROW_HEIGHT, paint(theme.feltMid))
            var x = MARGIN_LEFT + 10f
            cells.forEachIndexed { i, cell ->
                c.drawText(cell, x, top + 15f, paint(theme.onFelt, 10f, bold = true))
                x += CONTENT_WIDTH * weights[i]
            }
            drawColumnSeparators(top, ROW_HEIGHT, weights, separatorAfterColumns, header = true)
            y = top + ROW_HEIGHT
        }

        private fun drawTableRow(
            cells: List<String>,
            weights: FloatArray,
            alt: Boolean,
            separatorAfterColumns: IntArray = intArrayOf(),
        ) {
            val c = canvas!!
            val top = y
            val bg = if (alt) theme.rowAlt else theme.card
            c.drawRect(MARGIN_LEFT, top, MARGIN_LEFT + CONTENT_WIDTH, top + ROW_HEIGHT, paint(bg))
            c.drawRect(
                MARGIN_LEFT,
                top + ROW_HEIGHT - 0.5f,
                MARGIN_LEFT + CONTENT_WIDTH,
                top + ROW_HEIGHT,
                paint(theme.line),
            )
            var x = MARGIN_LEFT + 10f
            cells.forEachIndexed { i, cell ->
                c.drawText(cell, x, top + 15f, bodyPaint)
                x += CONTENT_WIDTH * weights[i]
            }
            drawColumnSeparators(top, ROW_HEIGHT, weights, separatorAfterColumns, header = false)
            y = top + ROW_HEIGHT
        }

        /** Vertical rules after the given 0-based column indices (inclusive). */
        private fun drawColumnSeparators(
            top: Float,
            height: Float,
            weights: FloatArray,
            afterColumns: IntArray,
            header: Boolean,
        ) {
            if (afterColumns.isEmpty()) return
            val c = canvas!!
            val line = stroke(
                color = if (header) 0x66F5F7F4 else theme.line,
                width = if (header) 1.2f else 1f,
            )
            for (after in afterColumns) {
                var edge = MARGIN_LEFT
                for (i in 0..after) {
                    edge += CONTENT_WIDTH * weights[i]
                }
                c.drawLine(edge, top + 2f, edge, top + height - 2f, line)
            }
        }

        private fun ensureSpace(needed: Float) {
            if (y + needed <= PAGE_HEIGHT - MARGIN_BOTTOM) return
            finishPage()
            startPage(fullHeader = false)
        }

        private fun startPage(fullHeader: Boolean) {
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(info)
            canvas = page!!.canvas
            canvas!!.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint(theme.paper))
            if (fullHeader) {
                y = 0f
            } else {
                drawContinuedHeader()
            }
        }

        private fun finishPage() {
            val c = canvas!!
            c.drawText(
                "Page $pageNumber",
                PAGE_WIDTH - MARGIN_LEFT - 48f,
                PAGE_HEIGHT - 24f,
                footerPaint,
            )
            document.finishPage(page!!)
            page = null
            canvas = null
        }
    }

    private fun paint(color: Int, size: Float = 12f, bold: Boolean = false): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create(
                Typeface.SANS_SERIF,
                if (bold) Typeface.BOLD else Typeface.NORMAL,
            )
        }

    private fun stroke(color: Int, width: Float): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = width
        }

    private fun darken(argb: Int, amount: Float): Int {
        val a = argb ushr 24 and 0xFF
        val r = ((argb shr 16 and 0xFF) * (1f - amount)).toInt().coerceIn(0, 255)
        val g = ((argb shr 8 and 0xFF) * (1f - amount)).toInt().coerceIn(0, 255)
        val b = ((argb and 0xFF) * (1f - amount)).toInt().coerceIn(0, 255)
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 40f
    private const val MARGIN_BOTTOM = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    private const val HEADER_HEIGHT = 210f
    private const val CONTINUED_HEADER = 44f
    private const val PLAYER_CARD_HEIGHT = 118f
    private const val ROW_HEIGHT = 22f
    private const val DEFAULT_ACCENT = 0xFF1B9A4A.toInt()
    private const val INNINGS_NAME_MAX = 12
    private val INNINGS_WEIGHTS = floatArrayOf(0.10f, 0.22f, 0.18f, 0.22f, 0.28f)
    /** After `#` and after player-1 End (columns 0 and 2). */
    private val INNINGS_SEPARATOR_AFTER = intArrayOf(0, 2)
}
