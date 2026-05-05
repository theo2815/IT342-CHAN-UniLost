package com.hulampay.mobile.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Theme preference options.
 * "system" follows device setting, "light"/"dark" override it.
 */
enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

/**
 * CompositionLocal that holds the current theme preference.
 * Hoisted at the Activity level so any screen (e.g., Settings) can read/write it.
 *
 * Session-only for Phase 1. DataStore persistence added in Phase 2.
 */
val LocalThemePreference = compositionLocalOf<MutableState<ThemePreference>> {
    mutableStateOf(ThemePreference.SYSTEM)
}
