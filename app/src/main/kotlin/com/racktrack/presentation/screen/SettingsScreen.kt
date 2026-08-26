package com.racktrack.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.racktrack.data.UserSettings
import com.racktrack.domain.model.BreakRule
import com.racktrack.i18n.AppLanguage
import com.racktrack.i18n.StringKey
import com.racktrack.i18n.Strings
import com.racktrack.presentation.MatchFormatOptions
import com.racktrack.presentation.component.ScrollMoreHint
import com.racktrack.presentation.component.SwipeIntPicker
import com.racktrack.presentation.component.SwipeLanguagePicker
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.component.TexturedChip
import com.racktrack.presentation.theme.AppThemeBackground
import com.racktrack.presentation.theme.AppThemeMode
import com.racktrack.presentation.theme.LocalAppTheme

@Composable
fun SettingsScreen(
    settings: UserSettings,
    adsRemoved: Boolean = false,
    onRemoveAds: () -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onThemeSelected: (AppThemeMode) -> Unit,
    onAppLanguageSelected: (AppLanguage) -> Unit = {},
    onKeepScreenOnChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onDefaultRacksChange: (Int) -> Unit,
    onDefaultPointsChange: (Int) -> Unit,
    onDefaultInningsChange: (Int?) -> Unit,
    onDefaultBreakRuleChange: (BreakRule) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val chrome = LocalAppTheme.current
    val scrollState = rememberScrollState()

    BackHandler(onBack = onBack)

    AppThemeBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Text(
                text = Strings.get(StringKey.SETTINGS),
                style = MaterialTheme.typography.headlineLarge,
                color = chrome.textPrimary,
            )

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel(Strings.get(StringKey.SECTION_APPEARANCE))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppThemeMode.entries.forEach { mode ->
                    TexturedChip(
                        label = mode.displayLabel().uppercase(),
                        selected = mode == settings.themeMode,
                        onClick = { onThemeSelected(mode) },
                        modifier = Modifier.weight(1f),
                        selectedLight = mode.palette.accentLight,
                        selectedDark = mode.palette.accentDark,
                        idleLight = chrome.surfaceElevated,
                        idleDark = chrome.surfaceDeep,
                        height = 48.dp,
                        useFeltGrain = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel(Strings.get(StringKey.SECTION_LANGUAGE))
            Spacer(modifier = Modifier.height(8.dp))
            SwipeLanguagePicker(
                language = settings.appLanguage,
                onLanguageChange = onAppLanguageSelected,
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel(Strings.get(StringKey.SECTION_DEVICE))
            Spacer(modifier = Modifier.height(6.dp))
            SettingsToggleRow(
                label = Strings.get(StringKey.KEEP_SCREEN_ON),
                checked = settings.keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
            )
            SettingsToggleRow(
                label = Strings.get(StringKey.HAPTICS),
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChange,
            )

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel(Strings.get(StringKey.SECTION_ADS))
            Spacer(modifier = Modifier.height(8.dp))
            if (adsRemoved) {
                Text(
                    text = Strings.get(StringKey.ADS_REMOVED),
                    style = MaterialTheme.typography.bodyMedium,
                    color = chrome.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                TexturedActionButton(
                    label = Strings.get(StringKey.REMOVE_ADS),
                    base = chrome.accent,
                    light = chrome.accentLight,
                    dark = chrome.accentDark,
                    enabled = true,
                    onClick = onRemoveAds,
                    height = 44.dp,
                useFeltGrain = false,
            )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TexturedActionButton(
                label = Strings.get(StringKey.RESTORE_PURCHASES),
                base = chrome.surface,
                light = chrome.surfaceElevated,
                dark = chrome.surfaceDeep,
                enabled = true,
                onClick = onRestorePurchases,
                height = 44.dp,
                useFeltGrain = false,
            )

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel(Strings.get(StringKey.DEFAULT_RACE_TO))
            Spacer(modifier = Modifier.height(8.dp))
            SwipeIntPicker(
                value = settings.defaultRacksToWin,
                onValueChange = onDefaultRacksChange,
                min = MatchFormatOptions.RACE_TO_MIN,
                max = MatchFormatOptions.RACE_TO_MAX,
                valueSp = 44.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))
            SectionLabel(Strings.get(StringKey.DEFAULT_DISTANCE_14_1))
            Spacer(modifier = Modifier.height(8.dp))
            val pointsOptions = MatchFormatOptions.pointsToWin
            val pointsIndex =
                pointsOptions.indexOf(settings.defaultPointsToWin).let { if (it >= 0) it else 0 }
            SwipeIntPicker(
                value = pointsIndex,
                onValueChange = { onDefaultPointsChange(pointsOptions[it]) },
                min = 0,
                max = pointsOptions.lastIndex,
                valueLabel = { pointsOptions[it].toString() },
                valueSp = 44.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))
            SectionLabel(Strings.get(StringKey.DEFAULT_INNINGS_14_1))
            Spacer(modifier = Modifier.height(8.dp))
            val inningsOptions = MatchFormatOptions.inningsLimits
            val unlimitedIndex = inningsOptions.size
            val inningsIndex =
                settings.defaultInningsLimit?.let { limit ->
                    inningsOptions.indexOf(limit).let { if (it >= 0) it else 0 }
                } ?: unlimitedIndex
            SwipeIntPicker(
                value = inningsIndex,
                onValueChange = { index ->
                    onDefaultInningsChange(
                        if (index >= unlimitedIndex) null else inningsOptions[index],
                    )
                },
                min = 0,
                max = unlimitedIndex,
                valueLabel = { index ->
                    if (index >= unlimitedIndex) "∞" else inningsOptions[index].toString()
                },
                valueSp = 44.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))
            SectionLabel(Strings.get(StringKey.DEFAULT_BREAK_RULE))
            Spacer(modifier = Modifier.height(8.dp))
            ChipRow {
                TexturedChip(
                    label = Strings.get(StringKey.ALTERNATE),
                    selected = settings.defaultBreakRule == BreakRule.ALTERNATE,
                    onClick = { onDefaultBreakRuleChange(BreakRule.ALTERNATE) },
                    selectedLight = chrome.accentLight,
                    selectedDark = chrome.accentDark,
                    idleLight = chrome.surface,
                    idleDark = chrome.surfaceDeep,
                    height = 40.dp,
                useFeltGrain = false,
            )
                TexturedChip(
                    label = Strings.get(StringKey.WINNER),
                    selected = settings.defaultBreakRule == BreakRule.WINNER,
                    onClick = { onDefaultBreakRuleChange(BreakRule.WINNER) },
                    selectedLight = chrome.accentLight,
                    selectedDark = chrome.accentDark,
                    idleLight = chrome.surface,
                    idleDark = chrome.surfaceDeep,
                    height = 40.dp,
                useFeltGrain = false,
            )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel(Strings.get(StringKey.SECTION_RULES))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Strings.get(StringKey.FFB_RULES_LINK),
                style = MaterialTheme.typography.bodyLarge,
                color = chrome.accentLight,
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
            TexturedActionButton(
                label = Strings.get(StringKey.BACK),
                base = chrome.accent,
                light = chrome.accentLight,
                dark = chrome.accentDark,
                enabled = true,
                onClick = onBack,
                modifier = Modifier.widthIn(min = 200.dp),
                height = 52.dp,
                useFeltGrain = false,
            )
            }
            ScrollMoreHint(
                scrollState = scrollState,
                fadeColor = chrome.background,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val chrome = LocalAppTheme.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = chrome.textSecondary,
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
    val chrome = LocalAppTheme.current
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
            color = chrome.textPrimary,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = chrome.onAccent,
                checkedTrackColor = chrome.accent,
                uncheckedThumbColor = chrome.textSecondary,
                uncheckedTrackColor = chrome.surfaceElevated,
                uncheckedBorderColor = chrome.rim.copy(alpha = 0.45f),
            ),
        )
    }
}

const val FFB_RULES_URL = "https://m.ffbillard.com/ext/telechargement.php?id=32249"


