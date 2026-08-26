package com.racktrack.data

import com.racktrack.domain.model.BreakRule
import com.racktrack.i18n.AppLanguage
import com.racktrack.presentation.theme.AppThemeMode

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.BLUE_GLOSSY,
    val keepScreenOn: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val defaultRacksToWin: Int = 6,
    val defaultPointsToWin: Int = 100,
    /** Null = unlimited innings. */
    val defaultInningsLimit: Int? = 30,
    val defaultBreakRule: BreakRule = BreakRule.ALTERNATE,
    /** In-app language; [AppLanguage.SYSTEM] follows the device. */
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
)
