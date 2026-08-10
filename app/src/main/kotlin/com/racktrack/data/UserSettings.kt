package com.racktrack.data

import com.racktrack.appearance.FeltTone
import com.racktrack.domain.model.BreakRule

data class UserSettings(
    val feltTone: FeltTone = FeltTone.FOREST,
    val keepScreenOn: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val defaultRacksToWin: Int = 6,
    val defaultPointsToWin: Int = 100,
    /** Null = unlimited innings. */
    val defaultInningsLimit: Int? = 30,
    val defaultBreakRule: BreakRule = BreakRule.ALTERNATE,
)
