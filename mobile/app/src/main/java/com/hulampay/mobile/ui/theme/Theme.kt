package com.hulampay.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
// UniLost Light Color Scheme
// Maps UniLost semantic tokens → Material 3 color roles
// See: PHASE-1-DESIGN-SYSTEM.md Section 7
// ============================================================
private val UniLostLightColorScheme = lightColorScheme(
    // Primary role → UniLost slate blue
    primary            = Slate600,        // #475569 — buttons, FAB, active states
    onPrimary          = White,           // #ffffff — text/icon on primary
    primaryContainer   = Slate100,        // #f1f5f9 — chips, selected backgrounds
    onPrimaryContainer = Slate800,        // #1e293b — text on primary container

    // Secondary role → UniLost sage green
    secondary            = Sage400,       // #84a98c — secondary buttons, karma
    onSecondary          = White,         // #ffffff
    secondaryContainer   = Sage400_8pct,  // sage @ 8% opacity
    onSecondaryContainer = Sage500,       // #52796f

    // Tertiary → Info blue (for special accents)
    tertiary            = Info,           // #3b82f6
    onTertiary          = White,          // #ffffff
    tertiaryContainer   = Info_8pct,      // info @ 8%
    onTertiaryContainer = InfoTextLight,  // #1e40af

    // Error role → matches web
    error            = ErrorRed,          // #ef4444
    onError          = White,
    errorContainer   = Error_8pct,        // error @ 8%
    onErrorContainer = ErrorTextLight,    // #991b1b

    // Background & Surface
    background       = Slate100,          // #f1f5f9
    onBackground     = Slate800,          // #1e293b
    surface          = White,             // #ffffff — cards, sheets
    onSurface        = Slate800,          // #1e293b
    surfaceVariant   = Slate100,          // #f1f5f9 — input backgrounds
    onSurfaceVariant = Slate600,          // #475569

    // Outline
    outline          = Slate200,          // #e2e8f0 — borders, dividers
    outlineVariant   = Slate300,          // #cbd5e1 — strong borders

    // Inverse (for snackbars, tooltips)
    inverseSurface   = Slate800,          // #1e293b
    inverseOnSurface = White,             // #ffffff
    inversePrimary   = Slate400,          // #94a3b8

    // Scrim (modal overlay)
    scrim            = Slate900_50pct,    // #0f172a @ 50%
)

// ============================================================
// UniLost Dark Color Scheme
// ============================================================
private val UniLostDarkColorScheme = darkColorScheme(
    primary            = Slate400,        // #94a3b8 — lighter for dark bg
    onPrimary          = Slate900,        // #0f172a
    primaryContainer   = Slate700,        // #334155
    onPrimaryContainer = Slate100,        // #f1f5f9

    secondary            = Sage300,       // #a3c1ad
    onSecondary          = Slate900,
    secondaryContainer   = Sage400_10pct,
    onSecondaryContainer = Sage300,

    tertiary            = Info,           // #3b82f6 (same)
    onTertiary          = White,
    tertiaryContainer   = Info_12pct,
    onTertiaryContainer = InfoTextDark,   // #93c5fd

    error            = ErrorRed,          // #ef4444 (same)
    onError          = Slate900,
    errorContainer   = Error_12pct,
    onErrorContainer = ErrorTextDark,     // #fca5a5

    background       = Slate900,          // #0f172a
    onBackground     = Slate050,          // #f8fafc
    surface          = Slate800,          // #1e293b
    onSurface        = Slate050,          // #f8fafc
    surfaceVariant   = Slate900,          // #0f172a — input backgrounds
    onSurfaceVariant = Slate300,          // #cbd5e1

    outline          = Slate700,          // #334155
    outlineVariant   = Slate600,          // #475569

    inverseSurface   = Slate100,          // #f1f5f9
    inverseOnSurface = Slate800,          // #1e293b
    inversePrimary   = Slate600,          // #475569

    scrim            = Black_60pct,
)

// ============================================================
// UniLost Theme Composable
// ============================================================
@Composable
fun UniLostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // IMPORTANT: dynamicColor is always disabled.
    // UniLost uses its own brand colors regardless of device wallpaper.
    val themePreference = LocalThemePreference.current.value
    val effectiveDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> darkTheme
    }
    val colorScheme = if (effectiveDark) UniLostDarkColorScheme else UniLostLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Status bar
            window.statusBarColor = if (effectiveDark) {
                Slate900.toArgb()
            } else {
                White.toArgb()
            }

            // Navigation bar
            window.navigationBarColor = if (effectiveDark) {
                Slate900.toArgb()
            } else {
                White.toArgb()
            }

            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light status bar icons in dark mode, dark icons in light mode
            insetsController.isAppearanceLightStatusBars = !effectiveDark
            insetsController.isAppearanceLightNavigationBars = !effectiveDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UniLostTypography,
        content = content
    )
}
