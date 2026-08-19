package com.racktrack.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racktrack.BuildConfig
import com.racktrack.data.UserSettings
import com.racktrack.domain.model.BreakRule
import com.racktrack.presentation.MatchFormatOptions
import com.racktrack.presentation.component.IntStepperModal
import com.racktrack.presentation.component.ScrollMoreHint
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.TexturedChip
import com.racktrack.appearance.FeltTone
import com.racktrack.appearance.LocalFeltPalette
import com.racktrack.presentation.theme.OutlineWarm
import com.racktrack.presentation.theme.ScoreWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun SettingsSheet(
    settings: UserSettings,
    adsRemoved: Boolean = false,
    onRemoveAds: () -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onFeltSelected: (FeltTone) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onDefaultRacksChange: (Int) -> Unit,
    onDefaultPointsChange: (Int) -> Unit,
    onDefaultInningsChange: (Int?) -> Unit,
    onDefaultBreakRuleChange: (BreakRule) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val felt = LocalFeltPalette.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .clickable(onClick = onDismiss)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(PANEL_WIDTH)
                .fillMaxHeight(PANEL_MAX_HEIGHT)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(felt.dark.copy(alpha = PANEL_TOP_ALPHA), felt.vignette),
                    ),
                )
                .border(2.dp, OutlineWarm.copy(alpha = PANEL_BORDER_ALPHA), RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge,
                color = ScoreWhite.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel("Cloth color")
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SWATCH_ROW_GAP),
            ) {
                FeltTone.entries.chunked(SWATCHES_PER_ROW).forEach { rowTones ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SWATCH_GAP),
                        verticalAlignment = Alignment.Top,
                    ) {
                        rowTones.forEach { tone ->
                            FeltSwatch(
                                tone = tone,
                                selected = tone == settings.feltTone,
                                onClick = { onFeltSelected(tone) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // Keep columns aligned when the last row is short.
                        repeat(SWATCHES_PER_ROW - rowTones.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Device")
            Spacer(modifier = Modifier.height(6.dp))
            SettingsToggleRow(
                label = "Keep screen on",
                checked = settings.keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
            )
            SettingsToggleRow(
                label = "Haptics",
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChange,
            )

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel("Ads")
            Spacer(modifier = Modifier.height(8.dp))
            if (adsRemoved) {
                Text(
                    text = "Ads removed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ScoreWhite.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                TexturedActionButton(
                    label = "REMOVE ADS",
                    base = felt.accent,
                    light = felt.accentLight,
                    dark = felt.accentDark,
                    enabled = true,
                    onClick = onRemoveAds,
                    height = 44.dp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TexturedActionButton(
                label = "RESTORE PURCHASES",
                base = felt.mid,
                light = felt.accentLight,
                dark = felt.dark,
                enabled = true,
                onClick = onRestorePurchases,
                height = 44.dp,
            )

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel("Default race to")
            Spacer(modifier = Modifier.height(8.dp))
            var raceEditorOpen by remember { mutableStateOf(false) }
            TexturedChip(
                label = settings.defaultRacksToWin.toString(),
                selected = true,
                onClick = { raceEditorOpen = true },
                modifier = Modifier.widthIn(min = 72.dp),
                selectedLight = felt.accentLight,
                selectedDark = felt.accentDark,
                idleLight = felt.mid,
                idleDark = felt.dark,
                height = 40.dp,
            )
            if (raceEditorOpen) {
                IntStepperModal(
                    title = "DEFAULT RACE TO",
                    valueLabel = { it.toString() },
                    initial = settings.defaultRacksToWin,
                    min = MatchFormatOptions.RACE_TO_MIN,
                    max = MatchFormatOptions.RACE_TO_MAX,
                    onDismiss = { raceEditorOpen = false },
                    onConfirm = { n ->
                        onDefaultRacksChange(n)
                        raceEditorOpen = false
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionLabel("Default distance (14/1)")
            Spacer(modifier = Modifier.height(8.dp))
            ChipRow {
                MatchFormatOptions.pointsToWin.forEach { n ->
                    TexturedChip(
                        label = n.toString(),
                        selected = settings.defaultPointsToWin == n,
                        onClick = { onDefaultPointsChange(n) },
                        selectedLight = felt.accentLight,
                        selectedDark = felt.accentDark,
                        idleLight = felt.mid,
                        idleDark = felt.dark,
                        height = 40.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionLabel("Default innings (14/1)")
            Spacer(modifier = Modifier.height(8.dp))
            ChipRow {
                MatchFormatOptions.inningsLimits.forEach { n ->
                    TexturedChip(
                        label = n.toString(),
                        selected = settings.defaultInningsLimit == n,
                        onClick = { onDefaultInningsChange(n) },
                        selectedLight = felt.accentLight,
                        selectedDark = felt.accentDark,
                        idleLight = felt.mid,
                        idleDark = felt.dark,
                        height = 40.dp,
                    )
                }
                TexturedChip(
                    label = "∞",
                    selected = settings.defaultInningsLimit == null,
                    onClick = { onDefaultInningsChange(null) },
                    selectedLight = felt.accentLight,
                    selectedDark = felt.accentDark,
                    idleLight = felt.mid,
                    idleDark = felt.dark,
                    height = 40.dp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionLabel("Default break rule (8/9/10)")
            Spacer(modifier = Modifier.height(8.dp))
            ChipRow {
                TexturedChip(
                    label = "ALTERNATE",
                    selected = settings.defaultBreakRule == BreakRule.ALTERNATE,
                    onClick = { onDefaultBreakRuleChange(BreakRule.ALTERNATE) },
                    selectedLight = felt.accentLight,
                    selectedDark = felt.accentDark,
                    idleLight = felt.mid,
                    idleDark = felt.dark,
                    height = 40.dp,
                )
                TexturedChip(
                    label = "WINNER",
                    selected = settings.defaultBreakRule == BreakRule.WINNER,
                    onClick = { onDefaultBreakRuleChange(BreakRule.WINNER) },
                    selectedLight = felt.accentLight,
                    selectedDark = felt.accentDark,
                    idleLight = felt.mid,
                    idleDark = felt.dark,
                    height = 40.dp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Rules")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "FFB American pool rules 2026–2027",
                style = MaterialTheme.typography.bodyLarge,
                color = felt.accentLight,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(FFB_RULES_URL)),
                        )
                    }
                    .padding(vertical = 6.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("About")
            Spacer(modifier = Modifier.height(8.dp))
            AboutPanel(onOpenRepo = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.REPO_URL)),
                )
            })

            Spacer(modifier = Modifier.height(16.dp))
            TexturedActionButton(
                label = "CLOSE",
                base = felt.accent,
                light = felt.accentLight,
                dark = felt.accentDark,
                enabled = true,
                onClick = onDismiss,
                modifier = Modifier.widthIn(min = 180.dp),
                height = 48.dp,
            )
            }
            ScrollMoreHint(
                scrollState = scrollState,
                fadeColor = felt.vignette,
            )
        }
    }
}

@Composable
private fun AboutPanel(onOpenRepo: () -> Unit) {
    val felt = LocalFeltPalette.current
    val buildKind = if (BuildConfig.DEBUG) "debug" else "release"
    val builtAt = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(BuildConfig.BUILD_EPOCH_MS))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AboutMetaRow(label = "App", value = "RackTrack")
        AboutMetaRow(
            label = "Version",
            value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        )
        AboutMetaRow(label = "Build", value = buildKind)
        AboutMetaRow(label = "Built", value = builtAt)
        Text(
            text = "Fonts · Bebas Neue & Outfit (SIL OFL 1.1)",
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "GitHub · Asarox33/RackTrack",
            style = MaterialTheme.typography.bodyLarge,
            color = felt.accentLight,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenRepo)
                .padding(vertical = 4.dp),
        )
        Text(
            text = "One device scores the table — no remote play. Match history stays on this phone in v1.",
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AboutMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite.copy(alpha = 0.55f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = OutlineWarm,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val felt = LocalFeltPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = ScoreWhite,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ScoreWhite,
                checkedTrackColor = felt.accentLight,
                uncheckedThumbColor = ScoreWhite.copy(alpha = 0.85f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
                uncheckedBorderColor = OutlineWarm.copy(alpha = 0.4f),
            ),
        )
    }
}

@Composable
private fun FeltSwatch(
    tone: FeltTone,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
    ) {
        Box(
            modifier = Modifier
                .size(SWATCH_CIRCLE)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(tone.palette.light, tone.palette.base, tone.palette.dark),
                    ),
                )
                .border(
                    width = if (selected) 3.dp else 1.5.dp,
                    color = if (selected) ScoreWhite else OutlineWarm.copy(alpha = 0.45f),
                    shape = CircleShape,
                ),
        )
        Text(
            text = tone.label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = if (selected) ScoreWhite else ScoreWhite.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

const val FFB_RULES_URL = "https://m.ffbillard.com/ext/telechargement.php?id=32249"

private const val SCRIM_ALPHA = 0.55f
private const val PANEL_WIDTH = 0.88f
private const val PANEL_MAX_HEIGHT = 0.92f
private const val PANEL_TOP_ALPHA = 0.97f
private const val PANEL_BORDER_ALPHA = 0.55f
private const val SWATCHES_PER_ROW = 3
private val SWATCH_CIRCLE = 40.dp
private val SWATCH_GAP = 8.dp
private val SWATCH_ROW_GAP = 12.dp
