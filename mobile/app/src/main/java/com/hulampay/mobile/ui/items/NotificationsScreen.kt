package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockNotification
import com.hulampay.mobile.data.mock.MockNotifications
import com.hulampay.mobile.ui.theme.*

private data class NotifTypeConfig(
    val icon: ImageVector,
    val color: Color,
    val label: String
)

private val typeConfigs = mapOf(
    "CLAIM_RECEIVED" to NotifTypeConfig(Icons.Default.Notifications, Color(0xFFa855f7), "Claim Received"),
    "CLAIM_APPROVED" to NotifTypeConfig(Icons.Default.CheckCircle, Color(0xFF22c55e), "Claim Approved"),
    "CLAIM_REJECTED" to NotifTypeConfig(Icons.Default.Cancel, Color(0xFFef4444), "Claim Rejected"),
    "HANDOVER_CONFIRMED" to NotifTypeConfig(Icons.Default.Check, Color(0xFF10b981), "Handover Complete"),
    "HANDOVER_REMINDER" to NotifTypeConfig(Icons.Default.Schedule, Color(0xFFf59e0b), "Handover Reminder"),
    "ITEM_EXPIRED" to NotifTypeConfig(Icons.Default.Warning, Color(0xFF94a3b8), "Item Expired"),
    "ITEM_MATCH" to NotifTypeConfig(Icons.Default.Search, Color(0xFF3b82f6), "Possible Match")
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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notifications", fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate600
                            ) {
                                Text(
                                    "$unreadCount",
                                    color = White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            Toast.makeText(context, "All marked as read (Mock action)", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Mark all read", fontSize = 13.sp, color = Slate600)
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
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate600,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            if (filteredNotifications.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Slate400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No notifications here", fontWeight = FontWeight.Medium, color = Slate800)
                        Text(
                            "Activity on your items and claims will show up here",
                            fontSize = 14.sp,
                            color = Slate400
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) White else Slate100.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Slate600)
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Slate800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        MockNotifications.timeAgo(notification.createdAt),
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    notification.message,
                    fontSize = 13.sp,
                    color = Slate400,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    config.label,
                    fontSize = 11.sp,
                    color = config.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
