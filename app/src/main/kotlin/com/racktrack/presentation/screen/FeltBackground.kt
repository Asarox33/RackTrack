package com.racktrack.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.racktrack.presentation.theme.AppThemeBackground

/** @deprecated Use [AppThemeBackground] — felt carpet is no longer the app identity. */
@Composable
fun FeltBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = AppThemeBackground(modifier = modifier, content = content)
