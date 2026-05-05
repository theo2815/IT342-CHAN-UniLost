package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockNotification
import com.hulampay.mobile.data.mock.MockNotifications
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

private data class NotifTypeConfig(
    val icon: ImageVector,
    val color: Color,
    val label: String
)

private val typeConfigs = mapOf(
    "CLAIM_RECEIVED" to NotifTypeConfig(Icons.Default.Notifications, Purple, "Claim Received"),
    "CLAIM_APPROVED" to NotifTypeConfig(Icons.Default.CheckCircle, Success, "Claim Approved"),
    "CLAIM_REJECTED" to NotifTypeConfig(Icons.Default.Cancel, ErrorRed, "Claim Rejected"),
    "HANDOVER_CONFIRMED" to NotifTypeConfig(Icons.Default.Check, Success, "Handover Complete"),
    "HANDOVER_REMINDER" to NotifTypeConfig(Icons.Default.Schedule, Warning, "Handover Reminder"),
    "ITEM_EXPIRED" to NotifTypeConfig(Icons.Default.Warning, Slate400, "Item Expired"),
    "ITEM_MATCH" to NotifTypeConfig(Icons.Default.Search, Info, "Possible Match")
)

private val claimTypes = listOf("CLAIM_RECEIVED", "CLAIM_APPROVED", "CLAIM_REJECTED", "HANDOVER_CONFIRMED", "HANDOVER_REMINDER")
private val itemTypes = listOf("ITEM_EXPIRED", "ITEM_MATCH")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val context = LocalContext.current
    val filters = listOf("All", "Unread", "Claims", "Items")
    var selectedFilter by remember { mutableStateOf("All") }

    val allNotifications = remember { MockNotifications.getAll() }

    val filteredNotifications = remember(selectedFilter) {
        when (selectedFilter) {
            "Unread" -> allNotifications.filter { !it.isRead }
            "Claims" -> allNotifications.filter { it.type in claimTypes }
            "Items" -> allNotifications.filter { it.type in itemTypes }
            else -> allNotifications
        }
    }

    val unreadCount = remember { MockNotifications.getUnreadCount() }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Notifications",
                onBackClick = { navController.popBackStack() },
                actions = {
                    if (unreadCount > 0) {
                        Surface(
                            shape = UniLostShapes.md,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "$unreadCount",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = UniLostSpacing.sm, vertical = UniLostSpacing.xxs)
                            )
                        }
                        TextButton(onClick = {
                            Toast.makeText(context, "All marked as read (Mock action)", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(
                                "Mark all read",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                modifier = Modifier.padding(vertical = UniLostSpacing.sm)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                filter,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = UniLostShapes.full
                    )
                }
            }

            if (filteredNotifications.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    title = "No notifications here",
                    message = "Activity on your items and claims will show up here"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = UniLostSpacing.md,
                        vertical = UniLostSpacing.sm
                    ),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    items(filteredNotifications) { notif ->
                        NotificationCard(
                            notification = notif,
                            onClick = {
                                navController.navigate(notif.linkRoute)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: MockNotification, onClick: () -> Unit) {
    val config = typeConfigs[notification.type] ?: typeConfigs["ITEM_MATCH"]!!

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = UniLostSpacing.xs, end = UniLostSpacing.sm)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // Type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(config.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    config.icon,
                    contentDescription = null,
                    tint = config.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        MockNotifications.timeAgo(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                Text(
                    config.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = config.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
