package com.racktrack.data

import android.content.Context
import com.racktrack.appearance.FeltTone

class AppPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): UserSettings =
        UserSettings(
            feltTone = feltTone(),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            defaultRacksToWin = prefs.getInt(KEY_DEFAULT_RACKS, DEFAULT_RACKS),
            defaultPointsToWin = prefs.getInt(KEY_DEFAULT_POINTS, DEFAULT_POINTS),
            defaultInningsLimit = prefs.getInt(KEY_DEFAULT_INNINGS, DEFAULT_INNINGS).let { value ->
                if (value <= 0) null else value
            },
        )

    fun save(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_FELT_TONE, settings.feltTone.name)
            .putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOn)
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putInt(KEY_DEFAULT_RACKS, settings.defaultRacksToWin)
            .putInt(KEY_DEFAULT_POINTS, settings.defaultPointsToWin)
            .putInt(KEY_DEFAULT_INNINGS, settings.defaultInningsLimit ?: 0)
            .apply()
    }

    private fun feltTone(): FeltTone {
        val raw = prefs.getString(KEY_FELT_TONE, FeltTone.FOREST.name)
        // Former "Classic" green merged into Forest.
        if (raw == LEGACY_CLASSIC_GREEN) return FeltTone.FOREST
        return FeltTone.entries.firstOrNull { it.name == raw } ?: FeltTone.FOREST
    }

    private companion object {
        const val PREFS_NAME = "racktrack_prefs"
        const val KEY_FELT_TONE = "felt_tone"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_DEFAULT_RACKS = "default_racks_to_win"
        const val KEY_DEFAULT_POINTS = "default_points_to_win"
        const val KEY_DEFAULT_INNINGS = "default_innings_limit"
        const val LEGACY_CLASSIC_GREEN = "CLASSIC_GREEN"
        const val DEFAULT_RACKS = 6
        const val DEFAULT_POINTS = 100
        const val DEFAULT_INNINGS = 30
    }
}
