package com.racktrack.presentation.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.racktrack.i18n.EnStrings
import com.racktrack.i18n.StringProvider

/** Active [StringProvider] for Compose; mirrors [com.racktrack.i18n.Strings.provider]. */
val LocalStrings = staticCompositionLocalOf<StringProvider> { EnStrings }
