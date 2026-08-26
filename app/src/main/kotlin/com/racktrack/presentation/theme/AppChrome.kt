package com.racktrack.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import com.racktrack.appearance.FeltPalette
import com.racktrack.i18n.StringKey
import com.racktrack.i18n.Strings
import kotlin.math.min

/**
 * Single visual world — B/C/D DNA (filigree + textured détourage / warm light / a11y dark).
 * DS v1 freeze 2026-08-25 — docs/06-roadmap-todo.md (rc/2.0.0).
 */
enum class AppThemeMode(
    val labelKey: StringKey,
    val palette: AppThemePalette,
) {
    /** DNA B — night blue + soft filigree + club action tones. */
    BLUE_GLOSSY(
        labelKey = StringKey.THEME_BLUE_GLOSSY,
        palette = AppThemePalette(
            background = Color(0xFF101A28),
            surface = Color(0xFF172436),
            surfaceElevated = Color(0xFF213148),
            surfaceDeep = Color(0xFF0A121C),
            accentLight = Color(0xFF4EB8D4),
            accent = Color(0xFF2A8FB0),
            accentDark = Color(0xFF1C6E8A),
            textPrimary = Color(0xFFE6ECF4),
            textSecondary = Color(0xFFA0AEBE),
            outline = Color(0xFF4E6880),
            rim = Color(0xFF4AA8C4),
            onAccent = Color(0xFFE4ECF2),
            ballFill = Color(0xFFD4DCE4),
            iconStroke = Color(0xFFB0BCC8),
            filigreeAlpha = 0.07f,
            isDark = true,
            actions = ActionTones(
                plus = ActionTone(Color(0xFF246A86), Color(0xFF348AAC), Color(0xFF1A5068)),
                runOut = ActionTone(Color(0xFF2A6E52), Color(0xFF388A68), Color(0xFF1E5240)),
                foul = ActionTone(Color(0xFF8C4E32), Color(0xFFA86442), Color(0xFF6A3A26)),
                golden = ActionTone(Color(0xFF8A6A32), Color(0xFFA48440), Color(0xFF6A5026)),
                dry = ActionTone(Color(0xFF2E4254), Color(0xFF3E586C), Color(0xFF203040)),
            ),
        ),
    ),

    /** DNA C — warm clear light (ivory / parchment, not cold blue-grey). */
    LIGHT_CLEAN(
        labelKey = StringKey.THEME_WARM_LIGHT,
        palette = AppThemePalette(
            background = Color(0xFFE8E2D8),
            surface = Color(0xFFF0EBE3),
            surfaceElevated = Color(0xFFF7F3EC),
            surfaceDeep = Color(0xFFD4CBC0),
            accentLight = Color(0xFF4A9A82),
            accent = Color(0xFF2E7A64),
            accentDark = Color(0xFF225C4A),
            textPrimary = Color(0xFF1C1814),
            textSecondary = Color(0xFF4A433C),
            outline = Color(0xFF8A7E70),
            rim = Color(0xFFA89068),
            onAccent = Color(0xFFF4EFE8),
            ballFill = Color(0xFFE4DDD4),
            iconStroke = Color(0xFF2E2822),
            filigreeAlpha = 0.11f,
            isDark = false,
            actions = ActionTones(
                plus = ActionTone(Color(0xFF2A6E5C), Color(0xFF3A8A72), Color(0xFF1E5244)),
                runOut = ActionTone(Color(0xFF2E6A48), Color(0xFF3E8658), Color(0xFF224E36)),
                foul = ActionTone(Color(0xFF8E5230), Color(0xFFA86840), Color(0xFF6A3C24)),
                golden = ActionTone(Color(0xFF8A6E3A), Color(0xFFA48848), Color(0xFF68522A)),
                dry = ActionTone(Color(0xFF5A5048), Color(0xFF6E645A), Color(0xFF443C36)),
            ),
        ),
    ),

    /** DNA D — dark accessible, minimal ornament, strong contrast. */
    DARK_NEON(
        labelKey = StringKey.THEME_DARK_TEAL,
        palette = AppThemePalette(
            background = Color(0xFF0E1216),
            surface = Color(0xFF161C22),
            surfaceElevated = Color(0xFF1E262E),
            surfaceDeep = Color(0xFF080A0C),
            accentLight = Color(0xFF4EC4B4),
            accent = Color(0xFF2AA898),
            accentDark = Color(0xFF1E7E72),
            textPrimary = Color(0xFFE6EAEF),
            textSecondary = Color(0xFF9AA4AE),
            outline = Color(0xFF4A6068),
            rim = Color(0xFF3EA898),
            onAccent = Color(0xFFE2ECEA),
            ballFill = Color(0xFFD0D8DC),
            iconStroke = Color(0xFFAEB6BC),
            filigreeAlpha = 0.065f,
            isDark = true,
            actions = ActionTones(
                plus = ActionTone(Color(0xFF227A72), Color(0xFF32968E), Color(0xFF185A54)),
                runOut = ActionTone(Color(0xFF266A4C), Color(0xFF328660), Color(0xFF1A4E38)),
                foul = ActionTone(Color(0xFF8A5634), Color(0xFFA26E44), Color(0xFF684028)),
                golden = ActionTone(Color(0xFF7A6438), Color(0xFF947C48), Color(0xFF5A4A2A)),
                dry = ActionTone(Color(0xFF34404C), Color(0xFF465460), Color(0xFF242E36)),
            ),
        ),
    ),
    ;

    fun displayLabel(): String = Strings.get(labelKey)
}

/** Semantic board / modal action fill (kept distinct, tuned per theme). */
@Immutable
data class ActionTone(
    val base: Color,
    val light: Color,
    val dark: Color,
)

@Immutable
data class ActionTones(
    val plus: ActionTone,
    val runOut: ActionTone,
    val foul: ActionTone,
    val golden: ActionTone,
    val dry: ActionTone,
)

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
    /** Soft structural edge (cards, fields). */
    val outline: Color,
    /** Chrome accent rim (chips / selected), not board action borders. */
    val rim: Color,
    /** Label on filled controls — soft off-white, never pure white. */
    val onAccent: Color,
    /** Soft ball shell for run-out / foul icons. */
    val ballFill: Color,
    /** Icon outline readable on this theme’s background. */
    val iconStroke: Color,
    /** Subtle geometric overlay (DNA B); 0 = off (Light). */
    val filigreeAlpha: Float,
    val isDark: Boolean,
    val actions: ActionTones,
) {
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

    /** Readable label on a filled control using [fill] as the dominant color. */
    fun contentOn(fill: Color): Color =
        if (fill.luminance() > 0.45f) textPrimary else onAccent
}

typealias AppChromePalette = AppThemePalette

val LocalAppTheme = staticCompositionLocalOf { AppThemeMode.BLUE_GLOSSY.palette }

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
                        theme.surface,
                        theme.background,
                        theme.surfaceDeep,
                    ),
                ),
            ),
    ) {
        if (theme.filigreeAlpha > 0.001f) {
            val filigreeColor = if (theme.isDark) {
                theme.rim.copy(alpha = theme.filigreeAlpha)
            } else {
                // Darker stroke so the lattice reads on light grey-blue.
                theme.outline.copy(alpha = theme.filigreeAlpha)
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawFiligree(
                    color = filigreeColor,
                    strokeWidth = min(
                        size.minDimension * if (theme.isDark) 0.0018f else 0.0024f,
                        if (theme.isDark) 1.6f else 2.0f,
                    ),
                )
            }
        }
        content()
    }
}

/** Soft diamond lattice (mock B filigree) — decorative only. */
private fun DrawScope.drawFiligree(color: Color, strokeWidth: Float) {
    val step = size.minDimension * 0.085f
    if (step < 8f) return
    val cols = (size.width / step).toInt() + 2
    val rows = (size.height / step).toInt() + 2
    val half = step * 0.42f
    for (row in 0..rows) {
        for (col in 0..cols) {
            val cx = col * step + if (row % 2 == 0) 0f else step * 0.5f
            val cy = row * step
            val diamond = Path().apply {
                moveTo(cx, cy - half)
                lineTo(cx + half, cy)
                lineTo(cx, cy + half)
                lineTo(cx - half, cy)
                close()
            }
            drawPath(diamond, color = color, style = Stroke(width = strokeWidth))
        }
    }
    // Soft center glow wash
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = color.alpha * 0.55f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            radius = size.minDimension * 0.55f,
        ),
        radius = size.minDimension * 0.55f,
        center = Offset(size.width * 0.5f, size.height * 0.42f),
    )
}

@Composable
fun AppChromeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = AppThemeBackground(modifier = modifier, content = content)

@Composable
fun AppChromeTheme(content: @Composable () -> Unit) {
    content()
}
