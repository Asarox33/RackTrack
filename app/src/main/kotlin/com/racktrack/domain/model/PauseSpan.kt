package com.racktrack.domain.model

/**
 * Wall-clock span when the match clock was paused (bathroom / smoke break).
 * Used only to exclude time from duration math — never shown in stats / PDF.
 */
data class PauseSpan(
    val startMillis: Long,
    val endMillis: Long,
) {
    init {
        require(endMillis >= startMillis) { "pause end must be >= start" }
    }

    /** Overlap of this span with [[from], [to]] in millis. */
    fun overlap(from: Long, to: Long): Long {
        if (to <= from) return 0L
        val start = maxOf(from, startMillis)
        val end = minOf(to, endMillis)
        return (end - start).coerceAtLeast(0L)
    }
}

fun List<PauseSpan>.pausedMillisBetween(from: Long, to: Long): Long =
    sumOf { it.overlap(from, to) }
