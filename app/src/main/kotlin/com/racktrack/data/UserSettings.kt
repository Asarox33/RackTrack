package com.racktrack.data

import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.GameMode
import com.racktrack.domain.model.RulesetPack
import com.racktrack.i18n.AppLanguage
import com.racktrack.presentation.theme.AppThemeMode

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.BLUE_GLOSSY,
    val keepScreenOn: Boolean = true,
    val hapticsEnabled: Boolean = true,
    /** Default race length for 8-ball (club-style race to 5). */
    val defaultRacksEight: Int = DEFAULT_RACKS_EIGHT,
    /** Default race length for 9-ball. */
    val defaultRacksNine: Int = DEFAULT_RACKS_NINE,
    /** Default race length for 10-ball. */
    val defaultRacksTen: Int = DEFAULT_RACKS_TEN,
    val defaultPointsToWin: Int = 100,
    /** Null = unlimited innings. */
    val defaultInningsLimit: Int? = 30,
    val defaultBreakEight: BreakRule = BreakRule.ALTERNATE,
    val defaultBreakNine: BreakRule = BreakRule.ALTERNATE,
    val defaultBreakTen: BreakRule = BreakRule.ALTERNATE,
    /** In-app language; [AppLanguage.SYSTEM] follows the device. */
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    /** Scoreboard ruleset pack (stamped onto each new match). */
    val rulesetPack: RulesetPack = RulesetPack.FFB,
) {
    fun defaultRacksFor(mode: GameMode): Int =
        when (mode) {
            GameMode.EIGHT_BALL -> defaultRacksEight
            GameMode.NINE_BALL -> defaultRacksNine
            GameMode.TEN_BALL -> defaultRacksTen
            GameMode.FOURTEEN_ONE -> defaultRacksEight
        }

    fun defaultBreakFor(mode: GameMode): BreakRule =
        when (mode) {
            GameMode.EIGHT_BALL -> defaultBreakEight
            GameMode.NINE_BALL -> defaultBreakNine
            GameMode.TEN_BALL -> defaultBreakTen
            GameMode.FOURTEEN_ONE -> defaultBreakEight
        }

    companion object {
        const val DEFAULT_RACKS_EIGHT = 5
        const val DEFAULT_RACKS_NINE = 7
        const val DEFAULT_RACKS_TEN = 7
    }
}
