package com.racktrack.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.racktrack.presentation.component.drawFeltClothGrain
import com.racktrack.presentation.component.rememberFeltNoiseBitmap
import com.racktrack.appearance.LocalFeltPalette

@Composable
fun FeltBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val felt = LocalFeltPalette.current
    val noise = rememberFeltNoiseBitmap()
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Soft cloth base — keep lighting subtle so grain stays readable (like the icon).
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(felt.light, felt.mid, felt.base, felt.dark),
                    center = Offset(size.width * CENTER_X, size.height * FELT_HOTSPOT_Y),
                    radius = size.maxDimension * FELT_RADIUS_FACTOR,
                ),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.White.copy(alpha = SOFT_LIGHT_ALPHA),
                    SOFT_LIGHT_FADE_STOP to Color.Transparent,
                    1f to Color.Black.copy(alpha = SOFT_SHADOW_ALPHA),
                ),
            )
            drawFeltClothGrain(noise = noise)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, felt.vignette.copy(alpha = VIGNETTE_ALPHA)),
                    center = Offset(size.width * CENTER_X, size.height * CENTER_Y),
                    radius = size.maxDimension * VIGNETTE_RADIUS_FACTOR,
                ),
            )
        }
        content()
    }
}

private const val CENTER_X = 0.5f
private const val CENTER_Y = 0.5f
private const val FELT_HOTSPOT_Y = 0.42f
private const val FELT_RADIUS_FACTOR = 0.85f
private const val SOFT_LIGHT_ALPHA = 0.03f
private const val SOFT_LIGHT_FADE_STOP = 0.42f
private const val SOFT_SHADOW_ALPHA = 0.12f
private const val VIGNETTE_ALPHA = 0.32f
private const val VIGNETTE_RADIUS_FACTOR = 0.80f
