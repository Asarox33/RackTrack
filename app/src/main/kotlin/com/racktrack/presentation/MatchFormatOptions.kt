package com.racktrack.presentation

/** Shared race / 14.1 distance & innings choices for setup + settings defaults. */
object MatchFormatOptions {
    const val RACE_TO_MIN: Int = 1
    const val RACE_TO_MAX: Int = 15
    val pointsToWin: List<Int> = listOf(50, 75, 100, 125, 150)
    val inningsLimits: List<Int> = listOf(20, 30, 40, 50)
}
