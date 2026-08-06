package com.racktrack.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FeltBlue = Color(0xFF0C3E8C)
val FeltBlueDark = Color(0xFF082A60)
val FeltBlueLight = Color(0xFF1A5BB8)
val ScoreWhite = Color(0xFFF5F8FF)
val ButtonPlus = Color(0xFF193250)
val ButtonRunOut = Color(0xFF12824B)
val ButtonFoul = Color(0xFFBE6919)
val BreakBadgeBg = Color(0xFFF5F8FF)
val BreakBadgeFg = Color(0xFF0C3E8C)

private val RackTrackColors = darkColorScheme(
    primary = ButtonRunOut,
    onPrimary = ScoreWhite,
    secondary = FeltBlueLight,
    onSecondary = ScoreWhite,
    background = FeltBlue,
    onBackground = ScoreWhite,
    surface = FeltBlueDark,
    onSurface = ScoreWhite,
    error = ButtonFoul,
    onError = ScoreWhite,
)

private val RackTrackTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        color = ScoreWhite,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        color = ScoreWhite,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = ScoreWhite,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = ScoreWhite,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = ScoreWhite,
    ),
)

@Composable
fun RackTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RackTrackColors,
        typography = RackTrackTypography,
        content = content,
    )
}
