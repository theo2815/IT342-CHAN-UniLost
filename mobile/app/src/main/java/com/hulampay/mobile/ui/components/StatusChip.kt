package com.hulampay.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hulampay.mobile.ui.theme.*

/**
 * Status chip / badge component matching spec Section 8.4.
 * Small pill-shaped label showing item/claim status with a colored dot.
 *
 * Colors are auto-mapped from status string.
 */
@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val config = getStatusConfig(status)

    Surface(
        modifier = modifier,
        shape = UniLostShapes.full,
        color = config.color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(config.color)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = config.displayLabel,
                color = config.color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Overload that accepts explicit color (for backwards compatibility).
 */
@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = UniLostShapes.full,
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label.replace("_", " "),
                color = color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Status configuration ──

private data class StatusConfig(
    val color: Color,
    val displayLabel: String
)

private fun getStatusConfig(status: String): StatusConfig {
    val normalizedStatus = status.uppercase().replace(" ", "_")
    return when (normalizedStatus) {
        // Item types
        "LOST" -> StatusConfig(ErrorRed, "LOST")
        "FOUND" -> StatusConfig(Sage400, "FOUND")

        // Item statuses
        "ACTIVE" -> StatusConfig(Info, "ACTIVE")
        "CLAIMED" -> StatusConfig(Warning, "CLAIMED")
        "RETURNED", "COMPLETED", "HANDED_OVER" -> StatusConfig(Success, normalizedStatus.replace("_", " "))
        "EXPIRED", "HIDDEN", "DELETED" -> StatusConfig(Slate400, normalizedStatus.replace("_", " "))

        // Claim statuses
        "PENDING" -> StatusConfig(Warning, "PENDING")
        "ACCEPTED", "APPROVED" -> StatusConfig(Success, normalizedStatus)
        "REJECTED" -> StatusConfig(ErrorRed, "REJECTED")
        "DISPUTED" -> StatusConfig(Warning, "DISPUTED")

        // Roles
        "ADMIN" -> StatusConfig(Slate600, "ADMIN")
        "FACULTY" -> StatusConfig(Info, "FACULTY")
        "STUDENT" -> StatusConfig(Sage400, "STUDENT")
        "SUPER_ADMIN" -> StatusConfig(Warning, "SUPER ADMIN")

        else -> StatusConfig(Slate400, status.replace("_", " "))
    }
}
