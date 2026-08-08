package com.racktrack.presentation.component

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.racktrack.presentation.theme.LocalHapticsEnabled

/**
 * Reliable click haptic for table-side scoring.
 * [View.performHapticFeedback] alone is often a no-op for KEYBOARD_TAP on many devices,
 * so we also drive the vibrator with a short click effect.
 */
@Composable
fun rememberClickHaptic(): () -> Unit {
    val context = LocalContext.current
    val view = LocalView.current
    val enabled = LocalHapticsEnabled.current
    return remember(context, view, enabled) {
        {
            if (enabled) {
                view.isHapticFeedbackEnabled = true
                view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
                )
                context.vibrateClick()
            }
        }
    }
}

private fun Context.vibrateClick() {
    val vibrator = currentVibrator() ?: return
    if (!vibrator.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        vibrator.vibrate(
            VibrationEffect.createOneShot(CLICK_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE),
        )
    }
}

private fun Context.currentVibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        getSystemService(Vibrator::class.java)
    }

private const val CLICK_DURATION_MS = 25L
