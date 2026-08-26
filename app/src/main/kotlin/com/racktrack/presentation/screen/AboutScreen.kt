package com.racktrack.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.racktrack.BuildConfig
import com.racktrack.i18n.StringKey
import com.racktrack.i18n.Strings
import com.racktrack.presentation.component.ScrollMoreHint
import com.racktrack.presentation.component.TexturedActionButton
import com.racktrack.presentation.theme.AppThemeBackground
import com.racktrack.presentation.theme.LocalAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun AboutScreen(
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
                    text = Strings.get(StringKey.SECTION_ABOUT).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.headlineLarge,
                    color = chrome.textPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                AboutPanel(
                    onOpenRepo = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.REPO_URL)),
                        )
                    },
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
private fun AboutPanel(onOpenRepo: () -> Unit) {
    val chrome = LocalAppTheme.current
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
        AboutMetaRow(label = Strings.get(StringKey.ABOUT_APP), value = Strings.get(StringKey.APP_NAME))
        AboutMetaRow(
            label = Strings.get(StringKey.ABOUT_VERSION),
            value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        )
        AboutMetaRow(label = Strings.get(StringKey.ABOUT_BUILD), value = buildKind)
        AboutMetaRow(label = Strings.get(StringKey.ABOUT_BUILT), value = builtAt)
        Text(
            text = Strings.get(StringKey.ABOUT_FONTS),
            style = MaterialTheme.typography.bodyLarge,
            color = chrome.textSecondary,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = Strings.get(StringKey.ABOUT_FLAGS),
            style = MaterialTheme.typography.bodyLarge,
            color = chrome.textSecondary,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = Strings.get(StringKey.ABOUT_GITHUB),
            style = MaterialTheme.typography.bodyLarge,
            color = chrome.accentLight,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenRepo)
                .padding(vertical = 4.dp),
        )
        Text(
            text = Strings.get(StringKey.ABOUT_BLURB),
            style = MaterialTheme.typography.bodyLarge,
            color = chrome.textSecondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AboutMetaRow(label: String, value: String) {
    val chrome = LocalAppTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = chrome.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = chrome.textPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
