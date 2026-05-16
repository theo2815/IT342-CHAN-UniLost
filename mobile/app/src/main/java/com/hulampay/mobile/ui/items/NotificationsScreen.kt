package com.hulampay.mobile.ui.items

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.NotificationDto
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo

private data class NotifTypeConfig(
    val icon: ImageVector,
    val color: Color,
    val label: String,
)

private val typeConfigs = mapOf(
    "CLAIM_RECEIVED"        to NotifTypeConfig(Icons.Default.Notifications, Purple, "Claim Received"),
    "CLAIM_ACCEPTED"        to NotifTypeConfig(Icons.Default.CheckCircle,   Success, "Claim Accepted"),
    "CLAIM_REJECTED"        to NotifTypeConfig(Icons.Default.Cancel,        ErrorRed, "Claim Rejected"),
    "NEW_MESSAGE"           to NotifTypeConfig(Icons.Default.Forum,         Info, "New Message"),
    "ITEM_FLAGGED"          to NotifTypeConfig(Icons.Default.Warning,       Warning, "Item Flagged"),
    "ITEM_MARKED_RETURNED"  to NotifTypeConfig(Icons.Default.LocalShipping, Warning, "Item Returned"),
    "ITEM_RETURNED"         to NotifTypeConfig(Icons.Default.Verified,      Success, "Return Confirmed"),
    "HANDOVER_DISPUTED"     to NotifTypeConfig(Icons.Default.ReportProblem, Warning, "Handover Disputed"),
    "ITEM_FLAG_THRESHOLD"   to NotifTypeConfig(Icons.Default.Flag,          Warning, "Flag Threshold"),
    "ITEM_REPORTED"         to NotifTypeConfig(Icons.Default.Flag,          Warning, "Item Reported"),
    "ITEM_HIDDEN"           to NotifTypeConfig(Icons.Default.VisibilityOff, Warning, "Item Hidden"),
    "ITEM_DELETED"          to NotifTypeConfig(Icons.Default.DeleteForever, ErrorRed, "Item Removed"),
    "ITEM_RESTORED"         to NotifTypeConfig(Icons.Default.Restore,       Success, "Item Restored"),
    "REPORT_DISMISSED"      to NotifTypeConfig(Icons.Default.TaskAlt,       Info, "Report Dismissed"),
    "APPEAL_SUBMITTED"      to NotifTypeConfig(Icons.Default.ReportProblem, Warning, "Appeal Submitted"),
    "APPEAL_APPROVED"       to NotifTypeConfig(Icons.Default.CheckCircle,   Success, "Appeal Approved"),
    "APPEAL_REJECTED"       to NotifTypeConfig(Icons.Default.Cancel,        ErrorRed, "Appeal Rejected"),
)

private val claimTypes = setOf(
    "CLAIM_RECEIVED", "CLAIM_ACCEPTED", "CLAIM_REJECTED",
    "ITEM_MARKED_RETURNED", "ITEM_RETURNED", "HANDOVER_DISPUTED",
)
private val itemTypes = setOf(
    "ITEM_FLAGGED", "NEW_MESSAGE", "ITEM_FLAG_THRESHOLD",
    "ITEM_REPORTED", "ITEM_HIDDEN", "ITEM_DELETED", "ITEM_RESTORED",
    "REPORT_DISMISSED", "APPEAL_SUBMITTED", "APPEAL_APPROVED", "APPEAL_REJECTED",
)

private fun routeFor(notif: NotificationDto): String? {
    val link = notif.linkId?.takeIf { it.isNotBlank() } ?: return null
    return when (notif.type) {
        "CLAIM_RECEIVED", "CLAIM_ACCEPTED", "CLAIM_REJECTED" ->
            "claim_detail_screen/$link"
        "ITEM_MARKED_RETURNED", "ITEM_RETURNED", "HANDOVER_DISPUTED", "NEW_MESSAGE" ->
            "chat_detail_screen/$link"
        "ITEM_FLAGGED", "ITEM_FLAG_THRESHOLD",
        "ITEM_REPORTED", "ITEM_HIDDEN", "ITEM_RESTORED",
        "REPORT_DISMISSED", "APPEAL_SUBMITTED", "APPEAL_APPROVED", "APPEAL_REJECTED" ->
            "item_detail_screen/$link"
        // ITEM_DELETED has no detail page to land on — clicking just marks-as-read.
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val filters = listOf("All", "Unread", "Claims", "Items")
    var selectedFilter by remember { mutableStateOf("All") }

    val state by viewModel.state.collectAsState()
    val allNotifications: List<NotificationDto> =
        (state as? UiState.Success)?.data.orEmpty()

    val filteredNotifications = remember(state, selectedFilter) {
        when (selectedFilter) {
            "Unread" -> allNotifications.filter { !it.read }
            "Claims" -> allNotifications.filter { it.type in claimTypes }
            "Items"  -> allNotifications.filter { it.type in itemTypes }
            else     -> allNotifications
        }
    }

    val unreadCount = allNotifications.count { !it.read }

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
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
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

            when (val s = state) {
                is UiState.Loading, UiState.Idle -> {
                    FullScreenLoading("Loading notifications...")
                }
                is UiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.ErrorOutline,
                        title = "Couldn't load notifications",
                        message = s.message,
                        actionLabel = "Retry",
                        onAction = { viewModel.load() }
                    )
                }
                is UiState.Success -> {
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
                            items(filteredNotifications, key = { it.id }) { notif ->
                                NotificationCard(
                                    notification = notif,
                                    onClick = {
                                        if (!notif.read) viewModel.markAsRead(notif.id)
                                        routeFor(notif)?.let { navController.navigate(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationDto, onClick: () -> Unit) {
    val config = typeConfigs[notification.type]
        ?: NotifTypeConfig(Icons.Default.Notifications, Slate400, notification.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.read) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(if (!notification.read) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.read) {
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
                        fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        timeAgo(notification.createdAt),
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
