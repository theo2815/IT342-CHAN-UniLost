package com.hulampay.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hulampay.mobile.ui.theme.*

/**
 * Animated shimmer loading state matching spec Section 8.10.
 * Sweeps a gradient highlight from left to right.
 */
@Composable
private fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

/**
 * A single shimmer rectangle placeholder.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    widthFraction: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(UniLostShapes.sm)
            .background(shimmerBrush())
    )
}

/**
 * Shimmer skeleton matching the feed item card layout.
 * Used in ItemFeed loading state.
 */
@Composable
fun ShimmerItemCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UniLostShapes.lg)
            .background(MaterialTheme.colorScheme.surface)
            .padding(UniLostSpacing.md)
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(UniLostShapes.md)
                .background(brush)
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

        // Title line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(18.dp)
                .clip(UniLostShapes.xs)
                .background(brush)
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

        // Tags row
        Row(horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(22.dp)
                    .clip(UniLostShapes.full)
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(22.dp)
                    .clip(UniLostShapes.full)
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

        // Location & time row
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(14.dp)
                .clip(UniLostShapes.xs)
                .background(brush)
        )
    }
}

/**
 * Shimmer skeleton matching a list row layout.
 * Used in MyItems, MyClaims, Notifications loading states.
 */
@Composable
fun ShimmerListRow(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(UniLostShapes.md)
            .background(MaterialTheme.colorScheme.surface)
            .padding(UniLostSpacing.md),
    ) {
        // Avatar / icon placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(UniLostShapes.sm)
                .background(brush)
        )

        Spacer(modifier = Modifier.width(UniLostSpacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(UniLostShapes.xs)
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.xs))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(UniLostShapes.xs)
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.xs))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(10.dp)
                    .clip(UniLostShapes.xs)
                    .background(brush)
            )
        }
    }
}

/**
 * Full screen loading state.
 * Centered spinner with message.
 */
@Composable
fun FullScreenLoading(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.md))
            androidx.compose.material3.Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
