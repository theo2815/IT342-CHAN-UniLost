package com.hulampay.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    // Primary → UniLost slate blue
    primary              = Slate600,
    onPrimary            = White,
    primaryContainer     = Slate100,
    onPrimaryContainer   = Slate800,

    // Secondary → Sage green (karma, FOUND items)
    secondary            = Sage400,
    onSecondary          = White,
    secondaryContainer   = Sage300,
    onSecondaryContainer = Sage500,

    // Tertiary → Info blue
    tertiary             = Info,
    onTertiary           = White,
    tertiaryContainer    = InfoBg,
    onTertiaryContainer  = InfoTextLight,

    // Error
    error                = Error,
    onError              = White,
    errorContainer       = ErrorBg,
    onErrorContainer     = ErrorTextLight,

    // Backgrounds & surfaces
    background           = Slate100,
    onBackground         = Slate800,
    surface              = White,
    onSurface            = Slate800,
    surfaceVariant       = Slate100,
    onSurfaceVariant     = Slate600,

    // Borders / outlines
    outline              = Slate200,
    outlineVariant       = Slate300,

    // Inverse (snackbars, tooltips)
    inverseSurface       = Slate800,
    inverseOnSurface     = White,
    inversePrimary       = Slate400,

    // Modal scrim
    scrim                = Slate900_50pct,
)

// ── Dark Color Scheme ─────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Slate400,
    onPrimary            = Slate900,
    primaryContainer     = Slate700,
    onPrimaryContainer   = Slate100,

    secondary            = Sage300,
    onSecondary          = Slate900,
    secondaryContainer   = Sage400,
    onSecondaryContainer = Sage300,

    tertiary             = Info,
    onTertiary           = White,
    tertiaryContainer    = InfoBg,
    onTertiaryContainer  = InfoTextDark,

    error                = Error,
    onError              = Slate900,
    errorContainer       = ErrorBg,
    onErrorContainer     = ErrorTextDark,

    background           = Slate900,
    onBackground         = Slate050,
    surface              = Slate800,
    onSurface            = Slate050,
    surfaceVariant       = Slate900,
    onSurfaceVariant     = Slate300,

    outline              = Slate700,
    outlineVariant       = Slate600,

    inverseSurface       = Slate100,
    inverseOnSurface     = Slate800,
    inversePrimary       = Slate600,

    scrim                = Black_60pct,
)

// ── Theme composable ──────────────────────────────────────────────────────────
@Composable
fun UniLostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: content draws behind status + nav bars
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

// Backward-compat alias — MainActivity still references MyApplicationTheme
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean    = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // always off — use UniLost brand palette
    content: @Composable () -> Unit
) = UniLostTheme(darkTheme = darkTheme, content = content)
