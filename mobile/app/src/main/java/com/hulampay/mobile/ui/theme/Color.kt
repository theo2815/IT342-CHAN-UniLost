package com.hulampay.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// UniLost Color Palette
// Extracted from web themes.css — matches web design exactly
// ============================================================

// ── Slate Scale ──
val Slate050 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)   // Light mode background
val Slate200 = Color(0xFFE2E8F0)   // Borders (light mode)
val Slate300 = Color(0xFFCBD5E1)   // Borders strong (light mode)
val Slate400 = Color(0xFF94A3B8)   // Text muted / Primary lighter
val Slate500 = Color(0xFF64748B)   // Primary light
val Slate600 = Color(0xFF475569)   // PRIMARY (light mode)
val Slate700 = Color(0xFF334155)   // Primary hover / Dark bg card
val Slate800 = Color(0xFF1E293B)   // Text primary (light) / Dark elevated
val Slate900 = Color(0xFF0F172A)   // Dark mode background

// ── Sage Green (Secondary) ──
val Sage300 = Color(0xFFA3C1AD)    // Secondary light
val Sage400 = Color(0xFF84A98C)    // SECONDARY
val Sage500 = Color(0xFF52796F)    // Secondary hover

// ── Status Colors ──

// Success (FOUND items, handover completed)
val Success = Color(0xFF10B981)
val SuccessHover = Color(0xFF059669)
val SuccessBg = Color(0x1410B981)          // 8% opacity
val SuccessTextLight = Color(0xFF065F46)
val SuccessTextDark = Color(0xFF6EE7B7)

// Warning (Pending claims, flagged items)
val Warning = Color(0xFFF59E0B)
val WarningHover = Color(0xFFD97706)
val WarningBg = Color(0x14F59E0B)          // 8% opacity
val WarningTextLight = Color(0xFF92400E)
val WarningTextDark = Color(0xFFFCD34D)

// Error (LOST items, errors, destructive actions)
val ErrorRed = Color(0xFFEF4444)
val ErrorHover = Color(0xFFDC2626)
val ErrorBg = Color(0x14EF4444)            // 8% opacity
val ErrorTextLight = Color(0xFF991B1B)
val ErrorTextDark = Color(0xFFFCA5A5)

// Info (Active items, informational)
val Info = Color(0xFF3B82F6)
val InfoHover = Color(0xFF2563EB)
val InfoBg = Color(0x14003B82F6)           // 8% opacity — note: extra 00 for ARGB
val InfoTextLight = Color(0xFF1E40AF)
val InfoTextDark = Color(0xFF93C5FD)

// ── Neutrals ──
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// ── Opacity Composites (pre-computed for M3 ColorScheme) ──

// Primary tints
val Slate600_8pct = Color(0x14475569)
val Slate900_50pct = Color(0x800F172A)

// Secondary tints
val Sage400_8pct = Color(0x1484A98C)
val Sage400_10pct = Color(0x1A84A98C)

// Tertiary tints
val Info_8pct = Color(0x143B82F6)
val Info_12pct = Color(0x1F3B82F6)

// Error tints
val Error_8pct = Color(0x14EF4444)
val Error_12pct = Color(0x1FEF4444)

// Dark mode overlay
val Black_60pct = Color(0x99000000)
val Black_4pct = Color(0x0A000000)
val White_5pct = Color(0x0DFFFFFF)

// ── Additional accent (used in claims/admin) ──
val Purple = Color(0xFFA855F7)
val PurpleBg = Color(0x14A855F7)           // 8% opacity
