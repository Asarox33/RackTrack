package com.racktrack.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PauseSpanTest {
    @Test
    fun `overlap clips to the query window`() {
        val span = PauseSpan(startMillis = 100L, endMillis = 200L)
        assertEquals(100L, span.overlap(0L, 300L))
        assertEquals(50L, span.overlap(150L, 300L))
        assertEquals(0L, span.overlap(200L, 300L))
        assertEquals(0L, span.overlap(0L, 50L))
    }

    @Test
    fun `pausedMillisBetween sums spans`() {
        val spans = listOf(
            PauseSpan(100L, 150L),
            PauseSpan(180L, 220L),
        )
        assertEquals(90L, spans.pausedMillisBetween(0L, 300L))
        assertEquals(40L, spans.pausedMillisBetween(180L, 300L))
    }
}
