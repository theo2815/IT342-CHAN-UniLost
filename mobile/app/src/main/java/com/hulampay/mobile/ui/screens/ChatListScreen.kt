package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

/**
 * Chat List screen — Spec Section 10.6.
 * Layout-only stub with mock data.
 */

private data class MockChat(
    val id: String,
    val otherPersonName: String,
    val lastMessage: String,
    val itemTitle: String,
    val timeAgo: String,
    val unreadCount: Int
)

private val mockChats = listOf(
    MockChat("c1", "Maria Santos", "Hi, I think I found your phone!", "Black Samsung Galaxy S24", "2m ago", 2),
    MockChat("c2", "Carlos Reyes", "Can we meet at the library?", "Blue Backpack with Laptop", "15m ago", 0),
    MockChat("c3", "Ana Garcia", "I left it at the security office.", "Student ID Card", "1h ago", 1),
    MockChat("c4", "Juan Dela Cruz", "Thanks for finding it!", "Silver Watch", "3h ago", 0),
    MockChat("c5", "Sofia Lim", "Is the charger included?", "MacBook Pro Charger", "1d ago", 0),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    Scaffold(
        topBar = {
            UniLostTopBar(
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                notificationCount = 3
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Section header
            Text(
                "Messages",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = UniLostSpacing.md,
                    vertical = UniLostSpacing.sm
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (mockChats.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Chat,
                    title = "No messages yet",
                    message = "Start a conversation by claiming an item."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    items(mockChats) { chat ->
                        ChatRow(
                            chat = chat,
                            onClick = {
                                navController.navigate("${Screen.ChatDetail.route}/${chat.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: MockChat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (chat.unreadCount > 0) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(name = chat.otherPersonName, size = 48.dp)

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chat.otherPersonName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        chat.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    chat.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Item: ${chat.itemTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (chat.unreadCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text("${chat.unreadCount}")
                        }
                    }
                }
            }
        }
    }
}
