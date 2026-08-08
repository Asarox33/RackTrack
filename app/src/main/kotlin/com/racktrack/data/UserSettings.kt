package com.racktrack.data

import com.racktrack.appearance.FeltTone

data class UserSettings(
    val feltTone: FeltTone = FeltTone.FOREST,
    val keepScreenOn: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val defaultRacksToWin: Int = 6,
    val defaultPointsToWin: Int = 100,
    /** Null = unlimited innings. */
    val defaultInningsLimit: Int? = 30,
)
