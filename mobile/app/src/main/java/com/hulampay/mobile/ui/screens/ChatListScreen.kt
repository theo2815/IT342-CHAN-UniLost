package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ChatDto
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo

/**
 * Chat List screen — Spec Section 10.6.
 * Wired to GET /api/chats; preserves the original layout.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel(),
    badgeViewModel: NotificationBadgeViewModel = hiltViewModel(),
    chatBadgeViewModel: ChatBadgeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val unreadNotifications by badgeViewModel.unread.collectAsState()
    val unreadChats by chatBadgeViewModel.unread.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            UniLostTopBar(
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                notificationCount = unreadNotifications.toInt(),
                chatCount = unreadChats.toInt()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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

            when (val current = state) {
                UiState.Idle, UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                is UiState.Error -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(UniLostSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        current.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is UiState.Success -> {
                    val chats = current.data
                    if (chats.isEmpty()) {
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
                            items(chats, key = { it.id }) { chat ->
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
    }
}

@Composable
private fun ChatRow(chat: ChatDto, onClick: () -> Unit) {
    val unread = chat.unreadCount.toInt().coerceAtLeast(0)
    val displayName = chat.otherParticipantName.ifBlank { "Unknown" }
    val preview = chat.lastMessagePreview?.takeIf { it.isNotBlank() } ?: "No messages yet"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (unread > 0) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(name = displayName, size = 48.dp)

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        timeAgo(chat.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    preview,
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
                    if (unread > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text("$unread")
                        }
                    }
                }
            }
        }
    }
}
