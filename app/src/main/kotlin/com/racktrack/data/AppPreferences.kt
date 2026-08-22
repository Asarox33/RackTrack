package com.racktrack.data

import android.content.Context
import com.racktrack.domain.model.BreakRule
import com.racktrack.presentation.theme.AppThemeMode

class AppPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): UserSettings =
        UserSettings(
            themeMode = themeMode(),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            defaultRacksToWin = prefs.getInt(KEY_DEFAULT_RACKS, DEFAULT_RACKS),
            defaultPointsToWin = prefs.getInt(KEY_DEFAULT_POINTS, DEFAULT_POINTS),
            defaultInningsLimit = prefs.getInt(KEY_DEFAULT_INNINGS, DEFAULT_INNINGS).let { value ->
                if (value <= 0) null else value
            },
            defaultBreakRule = breakRule(),
        )

    fun save(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .remove(KEY_FELT_TONE)
            .putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOn)
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putInt(KEY_DEFAULT_RACKS, settings.defaultRacksToWin)
            .putInt(KEY_DEFAULT_POINTS, settings.defaultPointsToWin)
            .putInt(KEY_DEFAULT_INNINGS, settings.defaultInningsLimit ?: 0)
            .putString(KEY_DEFAULT_BREAK_RULE, settings.defaultBreakRule.name)
            .apply()
    }

    private fun themeMode(): AppThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, null)
        if (raw != null) {
            return AppThemeMode.entries.firstOrNull { it.name == raw } ?: AppThemeMode.BLUE_GLOSSY
        }
        // Migrate former felt-cloth preference → default Blue glossy.
        return AppThemeMode.BLUE_GLOSSY
    }

    private fun breakRule(): BreakRule {
        val raw = prefs.getString(KEY_DEFAULT_BREAK_RULE, BreakRule.ALTERNATE.name)
        return BreakRule.entries.firstOrNull { it.name == raw } ?: BreakRule.ALTERNATE
    }

    private companion object {
        const val PREFS_NAME = "racktrack_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FELT_TONE = "felt_tone"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_DEFAULT_RACKS = "default_racks_to_win"
        const val KEY_DEFAULT_POINTS = "default_points_to_win"
        const val KEY_DEFAULT_INNINGS = "default_innings_limit"
        const val KEY_DEFAULT_BREAK_RULE = "default_break_rule"
        const val DEFAULT_RACKS = 6
        const val DEFAULT_POINTS = 100
        const val DEFAULT_INNINGS = 30
    }
}
