package com.racktrack.i18n

/**
 * Typed UI string keys for the upcoming i18n train.
 * English defaults live in [EnStrings]; locales plug in later without reshaping modules.
 */
enum class StringKey {
    APP_NAME,
    SETUP_TAGLINE,
}

/** Resolves [StringKey] values for the active locale. */
fun interface StringProvider {
    fun get(key: StringKey): String
}

/** English defaults (scaffold). Expand as UI copy is extracted. */
object EnStrings : StringProvider {
    override fun get(key: StringKey): String =
        when (key) {
            StringKey.APP_NAME -> "RackTrack"
            StringKey.SETUP_TAGLINE -> "American pool"
        }
}

/** Process-wide provider; Android UI can swap this when locales ship. */
object Strings {
    var provider: StringProvider = EnStrings

    fun get(key: StringKey): String = provider.get(key)
}
