package com.racktrack.presentation.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Dense cloth-like noise (baize), similar to the launcher icon grain.
 * Mid-gray centered so [BlendMode.Overlay] works on every felt color.
 */
@Composable
fun rememberFeltNoiseBitmap(tileSize: Int = FELT_NOISE_TILE): ImageBitmap =
    remember(tileSize) { createFeltNoiseBitmap(tileSize) }

private fun createFeltNoiseBitmap(size: Int): ImageBitmap {
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            // Multi-octave hash noise → fine fabric stipple.
            val fine = hash01(x, y)
            val mid = hash01(x shr 1, y shr 1)
            val fiber = hash01(x * 3 + y, y * 2 - x)
            val micro = hash01(x * 7 - y * 3, y * 5 + x)
            val v = (fine * 0.40f + mid * 0.20f + fiber * 0.22f + micro * 0.18f).coerceIn(0f, 1f)
            // Wider swing around mid-gray → stronger Overlay contrast.
            val g = (72 + v * 112f).toInt().coerceIn(0, 255)
            pixels[y * size + x] = android.graphics.Color.rgb(g, g, g)
        }
    }
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
    return bitmap.asImageBitmap()
}

fun DrawScope.drawFeltClothGrain(
    noise: ImageBitmap,
    alpha: Float = FELT_CLOTH_ALPHA,
) {
    val tw = noise.width.toFloat()
    val th = noise.height.toFloat()
    if (tw <= 0f || th <= 0f) return

    var y = 0f
    while (y < size.height) {
        var x = 0f
        while (x < size.width) {
            drawImage(
                image = noise,
                topLeft = Offset(x, y),
                alpha = alpha,
                blendMode = BlendMode.Overlay,
            )
            // Extra soft multiply pass for matte baize depth.
            drawImage(
                image = noise,
                topLeft = Offset(x, y),
                alpha = alpha * MULTIPLY_PASS_FACTOR,
                blendMode = BlendMode.Multiply,
            )
            x += tw
        }
        y += th
    }
}

private fun hash01(x: Int, y: Int): Float {
    var h = x * HASH_X + y * HASH_Y
    h = (h xor (h ushr HASH_MIX_SHIFT)) * HASH_MIX
    h = h xor (h ushr HASH_FINAL_SHIFT)
    return (h ushr HASH_BYTE_SHIFT and 0xFF) / 255f
}

private const val FELT_NOISE_TILE = 256
private const val FELT_CLOTH_ALPHA = 0.82f
private const val MULTIPLY_PASS_FACTOR = 0.38f
private const val HASH_X = 374_761_393
private const val HASH_Y = 668_265_263
private const val HASH_MIX = 1_274_126_177
private const val HASH_MIX_SHIFT = 13
private const val HASH_FINAL_SHIFT = 16
private const val HASH_BYTE_SHIFT = 8
