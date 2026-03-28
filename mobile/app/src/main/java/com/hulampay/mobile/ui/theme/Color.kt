package com.hulampay.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// ── Slate Scale ───────────────────────────────────────────────────────────────
val Slate050 = Color(0xFFf8fafc)
val Slate100 = Color(0xFFf1f5f9)  // Light mode background
val Slate200 = Color(0xFFe2e8f0)  // Borders (light mode)
val Slate300 = Color(0xFFcbd5e1)  // Borders strong (light mode)
val Slate400 = Color(0xFF94a3b8)  // Text muted / Primary lighter
val Slate500 = Color(0xFF64748b)  // Primary light
val Slate600 = Color(0xFF475569)  // PRIMARY (light mode)
val Slate700 = Color(0xFF334155)  // Primary hover / Dark card bg
val Slate800 = Color(0xFF1e293b)  // Text primary (light) / Dark card elevated
val Slate900 = Color(0xFF0f172a)  // Dark mode background

// ── Sage Green (Secondary) ────────────────────────────────────────────────────
val Sage300 = Color(0xFFa3c1ad)   // Secondary light
val Sage400 = Color(0xFF84a98c)   // SECONDARY
val Sage500 = Color(0xFF52796f)   // Secondary hover
val Sage    = Sage400             // Backward-compat alias

// ── Success ───────────────────────────────────────────────────────────────────
val Success         = Color(0xFF10b981)
val SuccessHover    = Color(0xFF059669)
val SuccessBg       = Color(0x1A10b981)   // ~10 % opacity
val SuccessTextLight = Color(0xFF065f46)
val SuccessTextDark  = Color(0xFF6ee7b7)

// ── Warning ───────────────────────────────────────────────────────────────────
val Warning         = Color(0xFFf59e0b)
val WarningHover    = Color(0xFFd97706)
val WarningBg       = Color(0x1Af59e0b)
val WarningTextLight = Color(0xFF92400e)
val WarningTextDark  = Color(0xFFfcd34d)

// ── Error ─────────────────────────────────────────────────────────────────────
val ErrorRed        = Color(0xFFef4444)
val Error           = ErrorRed
val ErrorHover      = Color(0xFFdc2626)
val ErrorBg         = Color(0x1Aef4444)
val ErrorTextLight  = Color(0xFF991b1b)
val ErrorTextDark   = Color(0xFFfca5a5)

// ── Info ──────────────────────────────────────────────────────────────────────
val Info            = Color(0xFF3b82f6)
val InfoHover       = Color(0xFF2563eb)
val InfoBg          = Color(0x1A3b82f6)
val InfoTextLight   = Color(0xFF1e40af)
val InfoTextDark    = Color(0xFF93c5fd)

// ── Neutrals ──────────────────────────────────────────────────────────────────
val White       = Color(0xFFFFFFFF)
val Black       = Color(0xFF000000)

// ── Scrim overlays ────────────────────────────────────────────────────────────
val Slate900_50pct = Color(0x800f172a)  // Modal scrim — light mode
val Black_60pct    = Color(0x99000000)  // Modal scrim — dark mode
