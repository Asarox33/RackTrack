package com.racktrack.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.racktrack.appearance.FeltPalette
import com.racktrack.appearance.LocalFeltPalette

/**
 * Single visual world for RackTrack (Setup, History, Settings, Board, modals).
 * Three user-selectable modes — no felt/carpet as app identity.
 *
 * Locked: docs/06-roadmap-todo.md (rc/2.0.0).
 */
enum class AppThemeMode(
    val label: String,
    val shortLabel: String,
    val palette: AppThemePalette,
) {
    /** Bleu moderne glossy — Blue Night + electric cyan. */
    BLUE_GLOSSY(
        label = "Blue glossy",
        shortLabel = "Blue",
        palette = AppThemePalette(
            background = Color(0xFF0B1220),
            surface = Color(0xFF121A2B),
            surfaceElevated = Color(0xFF1A2438),
            surfaceDeep = Color(0xFF0E1524),
            accentLight = Color(0xFF4EC8F0),
            accent = Color(0xFF1AAFD4),
            accentDark = Color(0xFF0E7FA0),
            textPrimary = Color(0xFFF2F5F8),
            textSecondary = Color(0xFF9AA8BC),
            outline = Color(0xFFD8E0EC),
            isDark = true,
        ),
    ),

    /** Clair & épuré — light surfaces + teal accent. */
    LIGHT_CLEAN(
        label = "Light clean",
        shortLabel = "Light",
        palette = AppThemePalette(
            background = Color(0xFFF0F3F6),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFFFFFFF),
            surfaceDeep = Color(0xFFE2E8EE),
            accentLight = Color(0xFF3EC4B8),
            accent = Color(0xFF1A9B90),
            accentDark = Color(0xFF137A72),
            textPrimary = Color(0xFF152033),
            textSecondary = Color(0xFF5A6B7C),
            outline = Color(0xFFC5CDD6),
            isDark = false,
        ),
    ),

    /** Dark moderne néon — near-black + teal neon accents (use sparingly). */
    DARK_NEON(
        label = "Dark neon",
        shortLabel = "Neon",
        palette = AppThemePalette(
            background = Color(0xFF05070A),
            surface = Color(0xFF0C1016),
            surfaceElevated = Color(0xFF141A22),
            surfaceDeep = Color(0xFF030508),
            accentLight = Color(0xFF5CFFE8),
            accent = Color(0xFF1AD4BE),
            accentDark = Color(0xFF0E9A8A),
            textPrimary = Color(0xFFF2F5F8),
            textSecondary = Color(0xFF8A9AAB),
            outline = Color(0xFF3A4555),
            isDark = true,
        ),
    ),
}

@Immutable
data class AppThemePalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceDeep: Color,
    val accentLight: Color,
    val accent: Color,
    val accentDark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    val isDark: Boolean,
) {
    /** Bridge for screens still reading [LocalFeltPalette] during the 2.0 migration. */
    fun asFeltPalette(): FeltPalette =
        FeltPalette(
            light = surfaceElevated,
            mid = surface,
            base = background,
            dark = surfaceDeep,
            vignette = surfaceDeep,
            accentLight = accentLight,
            accent = accent,
            accentDark = accentDark,
        )
}

/** @deprecated Prefer [LocalAppTheme]; kept as alias during migration. */
typealias AppChromePalette = AppThemePalette

val LocalAppTheme = staticCompositionLocalOf { AppThemeMode.BLUE_GLOSSY.palette }

/** @deprecated Use [LocalAppTheme]. */
val LocalAppChrome = LocalAppTheme

@Composable
fun AppThemeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val theme = LocalAppTheme.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        theme.surfaceElevated.copy(alpha = if (theme.isDark) 0.55f else 0.95f),
                        theme.background,
                        theme.surfaceDeep,
                    ),
                ),
            ),
    ) {
        content()
    }
}

/** @deprecated Use [AppThemeBackground]. */
@Composable
fun AppChromeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = AppThemeBackground(modifier = modifier, content = content)

/** No-op nest — theme is global via [RackTrackTheme]. Kept so call sites compile. */
@Composable
fun AppChromeTheme(content: @Composable () -> Unit) {
    content()
}
