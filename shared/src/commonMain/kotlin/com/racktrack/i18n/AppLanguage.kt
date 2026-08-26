package com.racktrack.i18n

/**
 * In-app language preference. [SYSTEM] follows the device locale;
 * other values force a catalog regardless of system language.
 */
enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    FRENCH,
    GERMAN,
    SPANISH,
    ITALIAN,
    DUTCH,
    PORTUGUESE,
    ;

    /** BCP 47 language subtag, or null when following the system. */
    val languageTag: String?
        get() =
            when (this) {
                SYSTEM -> null
                ENGLISH -> "en"
                FRENCH -> "fr"
                GERMAN -> "de"
                SPANISH -> "es"
                ITALIAN -> "it"
                DUTCH -> "nl"
                PORTUGUESE -> "pt"
            }

    /**
     * Endonym for fixed locales (not translated).
     * [SYSTEM] is empty — UI uses [StringKey.LANGUAGE_SYSTEM].
     */
    val nativeLabel: String
        get() =
            when (this) {
                SYSTEM -> ""
                ENGLISH -> "English"
                FRENCH -> "Français"
                GERMAN -> "Deutsch"
                SPANISH -> "Español"
                ITALIAN -> "Italiano"
                DUTCH -> "Nederlands"
                PORTUGUESE -> "Português"
            }
}
