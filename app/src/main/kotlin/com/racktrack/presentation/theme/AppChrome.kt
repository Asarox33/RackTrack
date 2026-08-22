package com.racktrack.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * APP chrome tokens (Setup / History / Settings) — Blue Night + Cyan.
 * GAME boards keep [com.racktrack.appearance.FeltPalette] + FeltBackground.
 *
 * Locked art direction: docs/06-roadmap-todo.md (rc/2.0.0).
 */
@Immutable
data class AppChromePalette(
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
) {
    companion object {
        /** Default Blue Night + Cyan — accent used sparingly. */
        val Default = AppChromePalette(
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
        )
    }
}

val LocalAppChrome = staticCompositionLocalOf { AppChromePalette.Default }

/**
 * Nested under [RackTrackTheme] for APP routes only.
 * Felt tone / [LocalFeltPalette] stay available for Settings cloth swatches.
 */
@Composable
fun AppChromeTheme(content: @Composable () -> Unit) {
    val chrome = AppChromePalette.Default
    val colors = darkColorScheme(
        primary = chrome.accent,
        onPrimary = ScoreWhite,
        secondary = chrome.surfaceElevated,
        onSecondary = ScoreWhite,
        background = chrome.background,
        onBackground = chrome.textPrimary,
        surface = chrome.surface,
        onSurface = chrome.textPrimary,
        error = ButtonFoul,
        onError = ScoreWhite,
    )
    CompositionLocalProvider(LocalAppChrome provides chrome) {
        MaterialTheme(
            colorScheme = colors,
            typography = MaterialTheme.typography,
            content = content,
        )
    }
}

/** Flat night surface — no cloth grain / vignette (unlike [FeltBackground]). */
@Composable
fun AppChromeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val chrome = LocalAppChrome.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        chrome.surfaceElevated.copy(alpha = 0.55f),
                        chrome.background,
                        chrome.surfaceDeep,
                    ),
                ),
            ),
    ) {
        content()
    }
}
