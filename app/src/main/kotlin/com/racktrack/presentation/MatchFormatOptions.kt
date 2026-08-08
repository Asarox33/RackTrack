package com.racktrack.presentation

/** Shared race / 14.1 distance & innings choices for setup + settings defaults. */
object MatchFormatOptions {
    val raceToWin: List<Int> = listOf(3, 5, 6, 7, 9)
    val pointsToWin: List<Int> = listOf(50, 75, 100, 125, 150)
    val inningsLimits: List<Int> = listOf(20, 30, 40, 50)
}
