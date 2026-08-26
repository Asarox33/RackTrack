package com.racktrack.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class LocaleCatalogsTest {
    @Test
    fun `unknown language falls back to English`() {
        assertSame(EnStrings, LocaleCatalogs.resolve(null))
        assertSame(EnStrings, LocaleCatalogs.resolve(""))
        assertSame(EnStrings, LocaleCatalogs.resolve("ja"))
        assertSame(EnStrings, LocaleCatalogs.resolve("zh-CN"))
    }

    @Test
    fun `language tags resolve to locale catalogs`() {
        assertSame(FrStrings, LocaleCatalogs.resolve("fr"))
        assertSame(FrStrings, LocaleCatalogs.resolve("fr-FR"))
        assertSame(DeStrings, LocaleCatalogs.resolve("de-DE"))
        assertSame(EsStrings, LocaleCatalogs.resolve("es"))
        assertSame(ItStrings, LocaleCatalogs.resolve("it"))
        assertSame(NlStrings, LocaleCatalogs.resolve("nl"))
        assertSame(PtStrings, LocaleCatalogs.resolve("pt-BR"))
        assertSame(EnStrings, LocaleCatalogs.resolve("en-US"))
    }

    @Test
    fun `french catalog translates key settings copy`() {
        assertEquals("RÉGLAGES", FrStrings.get(StringKey.SETTINGS))
        assertEquals("Pool américain", FrStrings.get(StringKey.SETUP_TAGLINE))
        assertEquals("RETOUR", FrStrings.get(StringKey.BACK))
        assertEquals(
            "Qui casse en premier ?",
            FrStrings.get(StringKey.WHO_BREAKS_FIRST),
        )
    }

    @Test
    fun `format replaces numbered placeholders`() {
        val previous = Strings.provider
        try {
            Strings.provider = FrStrings
            assertEquals(
                "Page 3",
                Strings.format(StringKey.PAGE_N, 3),
            )
            assertEquals(
                "Affichage 8-BALL uniquement · les filtres de nom correspondent à l’une ou l’autre place.",
                Strings.format(StringKey.HISTORY_FILTER_HINT, "8-BALL"),
            )
        } finally {
            Strings.provider = previous
        }
    }

    @Test
    fun `each locale overlay covers every StringKey`() {
        val keys = StringKey.entries.toSet()
        listOf(EnStrings, FrStrings, DeStrings, EsStrings, ItStrings, NlStrings, PtStrings)
            .forEach { provider ->
                keys.forEach { key ->
                    val value = provider.get(key)
                    assertEquals(false, value.isBlank(), "$provider missing/blank $key")
                }
            }
    }
}
