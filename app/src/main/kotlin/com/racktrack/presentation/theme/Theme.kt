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

val ScoreWhite = Color(0xFFF2F5F0)
val ButtonPlus = Color(0xFF1E3A4C)
val ButtonPlusLight = Color(0xFF2A5168)
val ButtonPlusDark = Color(0xFF152A38)
val ButtonRunOut = Color(0xFF1B7A45)
val ButtonRunOutLight = Color(0xFF259655)
val ButtonRunOutDark = Color(0xFF135C33)
val ButtonFoul = Color(0xFFB86A22)
val ButtonFoulLight = Color(0xFFD07F35)
val ButtonFoulDark = Color(0xFF8F5018)
val ButtonGolden = Color(0xFF9A7B2F)
val ButtonGoldenLight = Color(0xFFB8943A)
val ButtonGoldenDark = Color(0xFF735C22)
val ButtonDry = Color(0xFF3A4F5C)
val ButtonDryLight = Color(0xFF4C6676)
val ButtonDryDark = Color(0xFF2A3A45)
val OutlineWarm = Color(0xFFD8E0D4)
val CueBallHighlight = Color(0xFFFFFFFF)
val CueBallMid = Color(0xFFE8E8E8)
val CueBallShadow = Color(0xFFC8C8C8)
val CueBallDeep = Color(0xFF9A9A9A)
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

@Composable
fun RackTrackTheme(
    themeMode: AppThemeMode = AppThemeMode.BLUE_GLOSSY,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val theme = themeMode.palette
    val onPrimary = if (theme.isDark) ScoreWhite else Color(0xFFFFFFFF)
    val colors = if (theme.isDark) {
        darkColorScheme(
            primary = theme.accent,
            onPrimary = onPrimary,
            secondary = theme.surfaceElevated,
            onSecondary = theme.textPrimary,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            error = ButtonFoul,
            onError = ScoreWhite,
        )
    } else {
        lightColorScheme(
            primary = theme.accent,
            onPrimary = onPrimary,
            secondary = theme.surfaceDeep,
            onSecondary = theme.textPrimary,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            error = ButtonFoul,
            onError = ScoreWhite,
        )
    }
    CompositionLocalProvider(
        LocalAppTheme provides theme,
        LocalFeltPalette provides theme.asFeltPalette(),
        LocalHapticsEnabled provides hapticsEnabled,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = RackTrackTypography,
            content = content,
        )
    }
}
