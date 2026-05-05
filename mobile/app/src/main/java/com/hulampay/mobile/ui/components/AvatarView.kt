package com.hulampay.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hulampay.mobile.ui.theme.*

/**
 * Avatar component matching spec Section 8.9.
 *
 * Displays user initials on a gradient circle.
 * Sizes: 32dp (chat), 40dp (list), 56dp (profile header), 80dp (profile page), 96dp (settings)
 *
 * @param firstName User's first name (used for initials)
 * @param lastName User's last name (used for initials)
 * @param size Diameter of the avatar circle
 */
@Composable
fun AvatarView(
    firstName: String,
    lastName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val initials = buildString {
        if (firstName.isNotEmpty()) append(firstName.first().uppercase())
        if (lastName.isNotEmpty()) append(lastName.first().uppercase())
    }

    val fontSize = when {
        size >= 96.dp -> 32.sp
        size >= 80.dp -> 28.sp
        size >= 56.dp -> 20.sp
        size >= 40.dp -> 16.sp
        else -> 13.sp
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Overload with a single name string (splits on space).
 */
@Composable
fun AvatarView(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val parts = name.trim().split(" ")
    AvatarView(
        firstName = parts.getOrElse(0) { "" },
        lastName = parts.getOrElse(1) { "" },
        modifier = modifier,
        size = size
    )
}
