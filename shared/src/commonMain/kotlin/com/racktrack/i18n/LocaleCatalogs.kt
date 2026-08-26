package com.racktrack.i18n

/** Picks a [StringProvider] from a BCP 47 language tag (e.g. `fr`, `pt-BR` → `pt`). */
object LocaleCatalogs {
    fun resolve(languageTag: String?): StringProvider {
        val language =
            languageTag
                ?.trim()
                ?.lowercase()
                ?.substringBefore('-')
                ?.substringBefore('_')
                .orEmpty()
        return when (language) {
            "fr" -> FrStrings
            "de" -> DeStrings
            "es" -> EsStrings
            "it" -> ItStrings
            "nl" -> NlStrings
            "pt" -> PtStrings
            else -> EnStrings
        }
    }
}
