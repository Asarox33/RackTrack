@file:Suppress("MagicNumber")

package com.racktrack.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.R
import com.racktrack.i18n.AppLanguage
import com.racktrack.i18n.StringKey
import com.racktrack.i18n.Strings
import com.racktrack.presentation.theme.LocalAppTheme

private const val SWIPE_STEP_DP = 48f
private const val CHEVRON_SP = 36f
private const val NEIGHBOR_SPAN = 2
private const val NEAR_SCALE = 0.72f
private const val FAR_SCALE = 0.55f
private const val NEAR_ALPHA = 0.48f
private const val FAR_ALPHA = 0.26f
private const val CENTER_WEIGHT = 1.45f
private const val SIDE_WEIGHT = 1f
private val FLAG_CENTER = 40.dp
private val FLAG_NEAR = 28.dp
private val FLAG_FAR = 22.dp

/** Same swipe UX as [SwipeIntPicker], showing Twemoji flags + native language names. */
@Composable
fun SwipeLanguagePicker(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalAppTheme.current
    val options = AppLanguage.entries
    val index = options.indexOf(language).coerceAtLeast(0)
    val performHaptic = rememberClickHaptic()
    val density = LocalDensity.current
    val stepPx = with(density) { SWIPE_STEP_DP.dp.toPx() }
    var dragAccum by remember { mutableFloatStateOf(0f) }
    val local = remember { mutableIntStateOf(index) }
    LaunchedEffect(language) {
        local.intValue = options.indexOf(language).coerceAtLeast(0)
    }
    val onChangeUpdated by rememberUpdatedState(onLanguageChange)
    val hapticUpdated by rememberUpdatedState(performHaptic)

    fun commit(nextIndex: Int) {
        val coerced = nextIndex.coerceIn(0, options.lastIndex)
        if (coerced != local.intValue) {
            local.intValue = coerced
            hapticUpdated()
            onChangeUpdated(options[coerced])
        }
    }

    fun stepBy(delta: Int) = commit(local.intValue + delta)

    val centerLabel = languageLabel(options[local.intValue])
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "Language $centerLabel. Swipe left or right to change."
            }
            .pointerInput(stepPx) {
                detectHorizontalDragGestures(
                    onDragEnd = { dragAccum = 0f },
                    onDragCancel = { dragAccum = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccum += dragAmount
                        while (dragAccum <= -stepPx) {
                            dragAccum += stepPx
                            commit(local.intValue + 1)
                        }
                        while (dragAccum >= stepPx) {
                            dragAccum -= stepPx
                            commit(local.intValue - 1)
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LanguageChevron(
                label = "‹",
                enabled = local.intValue > 0,
                onClick = { stepBy(-1) },
            )
            LanguageNeighborStrip(
                center = local.intValue,
                options = options,
                onSelect = ::commit,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )
            LanguageChevron(
                label = "›",
                enabled = local.intValue < options.lastIndex,
                onClick = { stepBy(+1) },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = centerLabel,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
            color = theme.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LanguageNeighborStrip(
    center: Int,
    options: List<AppLanguage>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (offset in -NEIGHBOR_SPAN..NEIGHBOR_SPAN) {
            val n = center + offset
            val inRange = n in options.indices
            val (scale, alpha) =
                when (kotlin.math.abs(offset)) {
                    0 -> 1f to 1f
                    1 -> NEAR_SCALE to NEAR_ALPHA
                    else -> FAR_SCALE to FAR_ALPHA
                }
            val flagSize =
                when (kotlin.math.abs(offset)) {
                    0 -> FLAG_CENTER
                    1 -> FLAG_NEAR
                    else -> FLAG_FAR
                }
            Box(
                modifier = Modifier
                    .weight(if (offset == 0) CENTER_WEIGHT else SIDE_WEIGHT)
                    .heightIn(min = 56.dp)
                    .then(
                        if (inRange && offset != 0) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelect(n) },
                            )
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (inRange) {
                    Image(
                        painter = painterResource(options[n].flagDrawableRes()),
                        contentDescription = null,
                        modifier = Modifier
                            .size(flagSize * scale.coerceAtLeast(0.85f))
                            .alpha(alpha),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageChevron(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val theme = LocalAppTheme.current
    Box(
        modifier = Modifier
            .widthIn(min = 44.dp)
            .heightIn(min = 56.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = CHEVRON_SP.sp),
            color = theme.textPrimary.copy(alpha = if (enabled) 0.85f else 0.28f),
        )
    }
}

@DrawableRes
fun AppLanguage.flagDrawableRes(): Int =
    when (this) {
        AppLanguage.SYSTEM -> R.drawable.ic_lang_system
        AppLanguage.ENGLISH -> R.drawable.ic_flag_gb
        AppLanguage.FRENCH -> R.drawable.ic_flag_fr
        AppLanguage.GERMAN -> R.drawable.ic_flag_de
        AppLanguage.SPANISH -> R.drawable.ic_flag_es
        AppLanguage.ITALIAN -> R.drawable.ic_flag_it
        AppLanguage.DUTCH -> R.drawable.ic_flag_nl
        AppLanguage.PORTUGUESE -> R.drawable.ic_flag_pt
    }

/** Endonym for fixed locales; [AppLanguage.SYSTEM] uses the translated “System” string. */
private fun languageLabel(language: AppLanguage): String =
    if (language == AppLanguage.SYSTEM) {
        Strings.get(StringKey.LANGUAGE_SYSTEM)
    } else {
        language.nativeLabel
    }
