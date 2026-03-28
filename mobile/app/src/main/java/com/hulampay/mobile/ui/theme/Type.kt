package com.hulampay.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Full UniLost type scale — mapped from the web design tokens.
 *
 * Font families:
 *   Body / UI  → Inter (system sans-serif until TTF files are bundled)
 *   Display    → Outfit (system sans-serif until TTF files are bundled)
 *
 * To upgrade to real fonts later:
 *   1. Add Inter + Outfit TTF files to res/font/
 *   2. Replace FontFamily.SansSerif below with the proper FontFamily definitions
 */

val Typography = Typography(

    // ── Display ───────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 40.sp,
        lineHeight   = 50.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 35.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 24.sp,
        lineHeight   = 30.sp,
        letterSpacing = (-0.25).sp,
    ),

    // ── Headlines ─────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 22.sp,
        lineHeight   = 27.5.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 18.sp,
        lineHeight   = 22.5.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp,
    ),

    // ── Titles ────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 15.sp,
        lineHeight   = 21.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 19.6.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.8.sp,
        letterSpacing = 0.sp,
    ),

    // ── Body ──────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 21.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp,
    ),

    // ── Labels ────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 17.5.sp,
        letterSpacing = 0.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 15.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily   = FontFamily.SansSerif,
        fontWeight   = FontWeight.Medium,
        fontSize     = 10.sp,
        lineHeight   = 12.5.sp,
        letterSpacing = 0.5.sp,
    ),
)
