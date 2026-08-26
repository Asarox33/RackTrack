package com.racktrack.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.racktrack.R
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.i18n.StringProvider
import com.racktrack.i18n.Strings
import com.racktrack.presentation.i18n.LocalStrings

/** Fallback only — prefer [LocalAppTheme] text/outline for UI copy. Soft off-white. */
val ScoreWhite = Color(0xFFE4EAEF)
/** Fallback aliases (Blue glossy) — prefer [LocalAppTheme].actions in UI. */
private val FallbackActions = AppThemeMode.BLUE_GLOSSY.palette.actions
val ButtonPlus = FallbackActions.plus.base
val ButtonPlusLight = FallbackActions.plus.light
val ButtonPlusDark = FallbackActions.plus.dark
val ButtonRunOut = FallbackActions.runOut.base
val ButtonRunOutLight = FallbackActions.runOut.light
val ButtonRunOutDark = FallbackActions.runOut.dark
val ButtonFoul = FallbackActions.foul.base
val ButtonFoulLight = FallbackActions.foul.light
val ButtonFoulDark = FallbackActions.foul.dark
val ButtonGolden = FallbackActions.golden.base
val ButtonGoldenLight = FallbackActions.golden.light
val ButtonGoldenDark = FallbackActions.golden.dark
val ButtonDry = FallbackActions.dry.base
val ButtonDryLight = FallbackActions.dry.light
val ButtonDryDark = FallbackActions.dry.dark
val OutlineWarm = Color(0xFFA8B4C0)
val CueBallHighlight = Color(0xFFECEFF2)
val CueBallMid = Color(0xFFD8DCE0)
val CueBallShadow = Color(0xFFB8BEC4)
val CueBallDeep = Color(0xFF8E949A)
val CueTipLight = Color(0xFFFF6B5A)
val CueTipMid = Color(0xFFD62828)
val CueTipDark = Color(0xFF8B1515)

private val BebasNeue = FontFamily(
    Font(R.font.bebas_neue_regular, FontWeight.Normal),
)

private val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
)

private val RackTrackTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 104.sp,
        letterSpacing = 2.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = 1.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 1.5.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 1.2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        letterSpacing = 0.6.sp,
    ),
)

private fun Typography.withContentColor(color: Color): Typography =
    copy(
        displayLarge = displayLarge.copy(color = color),
        headlineLarge = headlineLarge.copy(color = color),
        titleLarge = titleLarge.copy(color = color),
        labelLarge = labelLarge.copy(color = color),
        bodyLarge = bodyLarge.copy(color = color),
        bodyMedium = bodyMedium.copy(color = color),
        labelMedium = labelMedium.copy(color = color),
    )

@Composable
fun RackTrackTheme(
    themeMode: AppThemeMode = AppThemeMode.BLUE_GLOSSY,
    hapticsEnabled: Boolean = true,
    strings: StringProvider = Strings.provider,
    content: @Composable () -> Unit,
) {
    val theme = themeMode.palette
    val colors = if (theme.isDark) {
        darkColorScheme(
            primary = theme.accent,
            onPrimary = theme.onAccent,
            secondary = theme.surfaceElevated,
            onSecondary = theme.textPrimary,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            error = ButtonFoul,
            onError = ScoreWhite,
            outline = theme.outline,
        )
    } else {
        lightColorScheme(
            primary = theme.accent,
            onPrimary = theme.onAccent,
            secondary = theme.surfaceDeep,
            onSecondary = theme.textPrimary,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            error = ButtonFoul,
            onError = ScoreWhite,
            outline = theme.outline,
        )
    }
    CompositionLocalProvider(
        LocalAppTheme provides theme,
        LocalFeltPalette provides theme.asFeltPalette(),
        LocalHapticsEnabled provides hapticsEnabled,
        LocalStrings provides strings,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = RackTrackTypography.withContentColor(theme.textPrimary),
            content = content,
        )
    }
}
