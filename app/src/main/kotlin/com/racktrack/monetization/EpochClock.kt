package com.racktrack.monetization

fun interface EpochClock {
    fun nowMs(): Long
}

object SystemEpochClock : EpochClock {
    override fun nowMs(): Long = System.currentTimeMillis()
}
