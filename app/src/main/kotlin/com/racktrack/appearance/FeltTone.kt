package com.racktrack.appearance

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FeltPalette(
    val light: Color,
    val mid: Color,
    val base: Color,
    val dark: Color,
    val vignette: Color,
    /** Selected chips / primary CTA on setup & settings. */
    val accentLight: Color,
    val accent: Color,
    val accentDark: Color,
)

enum class FeltTone(
    val label: String,
    val palette: FeltPalette,
) {
    FOREST(
        label = "Forest",
        palette = FeltPalette(
            light = Color(0xFF1F6B3A),
            mid = Color(0xFF175C30),
            base = Color(0xFF0F4A24),
            dark = Color(0xFF083018),
            vignette = Color(0xFF04140C),
            accentLight = Color(0xFF2FBF66),
            accent = Color(0xFF1B9A4A),
            accentDark = Color(0xFF136C34),
        ),
    ),
    TOURNAMENT_BLUE(
        label = "Blue",
        palette = FeltPalette(
            light = Color(0xFF2A5A7A),
            mid = Color(0xFF1E4A68),
            base = Color(0xFF163A54),
            dark = Color(0xFF0C2438),
            vignette = Color(0xFF061018),
            accentLight = Color(0xFF4A9ED4),
            accent = Color(0xFF2E7FB8),
            accentDark = Color(0xFF1E5A86),
        ),
    ),
    BURGUNDY(
        label = "Burgundy",
        palette = FeltPalette(
            light = Color(0xFF7A2E42),
            mid = Color(0xFF632436),
            base = Color(0xFF4A1A28),
            dark = Color(0xFF2E1018),
            vignette = Color(0xFF14080C),
            accentLight = Color(0xFFD45A72),
            accent = Color(0xFFB03A54),
            accentDark = Color(0xFF7E2840),
        ),
    ),
    CHARCOAL(
        label = "Charcoal",
        palette = FeltPalette(
            light = Color(0xFF3A424A),
            mid = Color(0xFF2C343C),
            base = Color(0xFF1E242A),
            dark = Color(0xFF12161A),
            vignette = Color(0xFF07090B),
            accentLight = Color(0xFF7A8A98),
            accent = Color(0xFF5A6A78),
            accentDark = Color(0xFF3E4A56),
        ),
    ),
    PINK(
        label = "Pink",
        palette = FeltPalette(
            light = Color(0xFFD46A8C),
            mid = Color(0xFFB84E74),
            base = Color(0xFF9A3A5C),
            dark = Color(0xFF6A2542),
            vignette = Color(0xFF2E1220),
            accentLight = Color(0xFFF08AAB),
            accent = Color(0xFFE05A88),
            accentDark = Color(0xFFB03E68),
        ),
    ),
    /** Warm mustard-gold cloth, in the spirit of Predator Arcadia Select Gold. */
    GOLDEN(
        label = "Golden",
        palette = FeltPalette(
            light = Color(0xFFC9A63A),
            mid = Color(0xFFB08C2E),
            base = Color(0xFF8F7024),
            dark = Color(0xFF5C4816),
            vignette = Color(0xFF2A200A),
            accentLight = Color(0xFFE8C85A),
            accent = Color(0xFFD4A82E),
            accentDark = Color(0xFF9A781C),
        ),
    ),
}

val LocalFeltPalette = staticCompositionLocalOf { FeltTone.FOREST.palette }
