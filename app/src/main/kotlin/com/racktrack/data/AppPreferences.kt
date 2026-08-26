package com.racktrack.data

import android.content.Context
import com.racktrack.domain.model.BreakRule
import com.racktrack.domain.model.RulesetPack
import com.racktrack.i18n.AppLanguage
import com.racktrack.presentation.theme.AppThemeMode

class AppPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): UserSettings {
        val legacyBreak = legacyBreakRule()
        return UserSettings(
            themeMode = themeMode(),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            defaultRacksEight = defaultRacks(KEY_DEFAULT_RACKS_8, UserSettings.DEFAULT_RACKS_EIGHT),
            defaultRacksNine = defaultRacks(KEY_DEFAULT_RACKS_9, UserSettings.DEFAULT_RACKS_NINE),
            defaultRacksTen = defaultRacks(KEY_DEFAULT_RACKS_10, UserSettings.DEFAULT_RACKS_TEN),
            defaultPointsToWin = prefs.getInt(KEY_DEFAULT_POINTS, DEFAULT_POINTS),
            defaultInningsLimit = prefs.getInt(KEY_DEFAULT_INNINGS, DEFAULT_INNINGS).let { value ->
                if (value <= 0) null else value
            },
            defaultBreakEight = breakRule(KEY_DEFAULT_BREAK_8, legacyBreak),
            defaultBreakNine = breakRule(KEY_DEFAULT_BREAK_9, legacyBreak),
            defaultBreakTen = breakRule(KEY_DEFAULT_BREAK_10, legacyBreak),
            appLanguage = appLanguage(),
            rulesetPack = rulesetPack(),
        )
    }

    fun save(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .remove(KEY_FELT_TONE)
            .putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOn)
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putInt(KEY_DEFAULT_RACKS_8, settings.defaultRacksEight)
            .putInt(KEY_DEFAULT_RACKS_9, settings.defaultRacksNine)
            .putInt(KEY_DEFAULT_RACKS_10, settings.defaultRacksTen)
            .remove(KEY_DEFAULT_RACKS_LEGACY)
            .putInt(KEY_DEFAULT_POINTS, settings.defaultPointsToWin)
            .putInt(KEY_DEFAULT_INNINGS, settings.defaultInningsLimit ?: 0)
            .putString(KEY_DEFAULT_BREAK_8, settings.defaultBreakEight.name)
            .putString(KEY_DEFAULT_BREAK_9, settings.defaultBreakNine.name)
            .putString(KEY_DEFAULT_BREAK_10, settings.defaultBreakTen.name)
            .remove(KEY_DEFAULT_BREAK_LEGACY)
            .putString(KEY_APP_LANGUAGE, settings.appLanguage.name)
            .putString(KEY_RULESET_PACK, settings.rulesetPack.name)
            .apply()
    }

    private fun themeMode(): AppThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, null)
        if (raw != null) {
            return AppThemeMode.entries.firstOrNull { it.name == raw } ?: AppThemeMode.BLUE_GLOSSY
        }
        return AppThemeMode.BLUE_GLOSSY
    }

    private fun appLanguage(): AppLanguage {
        val raw = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name)
        return AppLanguage.entries.firstOrNull { it.name == raw } ?: AppLanguage.SYSTEM
    }

    private fun rulesetPack(): RulesetPack {
        val raw = prefs.getString(KEY_RULESET_PACK, RulesetPack.FFB.name)
        return RulesetPack.entries.firstOrNull { it.name == raw } ?: RulesetPack.FFB
    }

    /**
     * Per-mode race default. If the mode key is missing, fall back to the legacy
     * single `default_racks_to_win` so v1 users keep their chosen length until they edit.
     */
    private fun defaultRacks(
        modeKey: String,
        factoryDefault: Int,
    ): Int {
        if (prefs.contains(modeKey)) {
            return prefs.getInt(modeKey, factoryDefault).coerceAtLeast(1)
        }
        if (prefs.contains(KEY_DEFAULT_RACKS_LEGACY)) {
            return prefs.getInt(KEY_DEFAULT_RACKS_LEGACY, factoryDefault).coerceAtLeast(1)
        }
        return factoryDefault
    }

    private fun legacyBreakRule(): BreakRule {
        val raw = prefs.getString(KEY_DEFAULT_BREAK_LEGACY, BreakRule.ALTERNATE.name)
        return BreakRule.entries.firstOrNull { it.name == raw } ?: BreakRule.ALTERNATE
    }

    private fun breakRule(
        modeKey: String,
        legacyFallback: BreakRule,
    ): BreakRule {
        if (prefs.contains(modeKey)) {
            val raw = prefs.getString(modeKey, BreakRule.ALTERNATE.name)
            return BreakRule.entries.firstOrNull { it.name == raw } ?: BreakRule.ALTERNATE
        }
        return legacyFallback
    }

    private companion object {
        const val PREFS_NAME = "racktrack_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FELT_TONE = "felt_tone"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_DEFAULT_RACKS_LEGACY = "default_racks_to_win"
        const val KEY_DEFAULT_RACKS_8 = "default_racks_eight"
        const val KEY_DEFAULT_RACKS_9 = "default_racks_nine"
        const val KEY_DEFAULT_RACKS_10 = "default_racks_ten"
        const val KEY_DEFAULT_POINTS = "default_points_to_win"
        const val KEY_DEFAULT_INNINGS = "default_innings_limit"
        const val KEY_DEFAULT_BREAK_LEGACY = "default_break_rule"
        const val KEY_DEFAULT_BREAK_8 = "default_break_eight"
        const val KEY_DEFAULT_BREAK_9 = "default_break_nine"
        const val KEY_DEFAULT_BREAK_10 = "default_break_ten"
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_RULESET_PACK = "ruleset_pack"
        const val DEFAULT_POINTS = 100
        const val DEFAULT_INNINGS = 30
    }
}
