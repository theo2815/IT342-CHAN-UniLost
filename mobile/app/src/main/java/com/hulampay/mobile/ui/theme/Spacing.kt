package com.hulampay.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * UniLost spacing scale — consistent padding/margin values
 * mapped from web tokens.css.
 *
 * Usage: UniLostSpacing.md for 16dp standard content padding
 */
object UniLostSpacing {
    val xxs  = 2.dp    // Tiny gaps, icon-text gap
    val xs   = 4.dp    // Chip internal padding, small gaps
    val sm   = 8.dp    // Card internal padding small, icon margins
    val md   = 16.dp   // Standard content padding, card body padding
    val lg   = 24.dp   // Section spacing, card vertical padding
    val xl   = 32.dp   // Large section gaps, hero padding
    val xxl  = 48.dp   // Screen top/bottom padding
    val xxxl = 64.dp   // Hero banner heights
}

/**
 * UniLost border radius scale.
 *
 * Usage: UniLostRadius.md for 12dp standard card radius
 */
object UniLostRadius {
    val xs   = 4.dp    // Small tags, tight chips
    val sm   = 6.dp    // Buttons (small), inner elements
    val md   = 12.dp   // Cards, input fields, standard chips
    val lg   = 16.dp   // Large cards, bottom sheets, modals
    val xl   = 24.dp   // Hero cards, image cards, bottom sheet top
    val full = 9999.dp // Pill badges, avatar circles, FABs
}

/**
 * Pre-built shapes for convenience.
 */
object UniLostShapes {
    val xs   = RoundedCornerShape(UniLostRadius.xs)
    val sm   = RoundedCornerShape(UniLostRadius.sm)
    val md   = RoundedCornerShape(UniLostRadius.md)
    val lg   = RoundedCornerShape(UniLostRadius.lg)
    val xl   = RoundedCornerShape(UniLostRadius.xl)
    val full = RoundedCornerShape(UniLostRadius.full)

    /** Bottom sheet: only top corners rounded */
    val bottomSheet = RoundedCornerShape(
        topStart = UniLostRadius.xl,
        topEnd = UniLostRadius.xl,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}

/**
 * Minimum touch target size (Android accessibility guideline).
 */
object UniLostTouchTarget {
    val min = 48.dp
    val chipHeight = 36.dp
}
