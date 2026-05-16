package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 *
 * Pass 3 additions (parity with website Messages.jsx sidebar):
 *  - All / Unread filter tabs at the top
 *  - Per-row status dot on the avatar (active / pending / completed / closed)
 *  - Per-row item status chip showing the underlying item state
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

    var filter by rememberSaveable { mutableStateOf(ChatListFilter.ALL) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            UniLostTopBar(
                onLogoClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                notificationCount = unreadNotifications.toInt(),
                chatCount = unreadChats.toInt(),
                chatActive = true
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
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

            ChatListFilterTabs(
                selected = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(
                    horizontal = UniLostSpacing.md,
                    vertical = UniLostSpacing.xs,
                ),
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
                    val filtered = when (filter) {
                        ChatListFilter.ALL -> chats
                        ChatListFilter.UNREAD -> chats.filter { it.unreadCount > 0 }
                    }
                    if (filtered.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Chat,
                            title = if (filter == ChatListFilter.UNREAD) "No unread messages" else "No messages yet",
                            message = if (filter == ChatListFilter.UNREAD) {
                                "All caught up — switch to All to see every conversation."
                            } else {
                                "Start a conversation by claiming an item."
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            items(filtered, key = { it.id }) { chat ->
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

private enum class ChatListFilter { ALL, UNREAD }

@Composable
private fun ChatListFilterTabs(
    selected: ChatListFilter,
    onSelect: (ChatListFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = UniLostShapes.full,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
            FilterTab(
                label = "All",
                isSelected = selected == ChatListFilter.ALL,
                onClick = { onSelect(ChatListFilter.ALL) },
                modifier = Modifier.weight(1f),
            )
            FilterTab(
                label = "Unread",
                isSelected = selected == ChatListFilter.UNREAD,
                onClick = { onSelect(ChatListFilter.UNREAD) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(UniLostShapes.full)
            .clickable(onClick = onClick),
        shape = UniLostShapes.full,
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = UniLostSpacing.md,
                vertical = UniLostSpacing.xs,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/**
 * Mirrors website `getChatStatusType` — drives the dot color on each row.
 */
private enum class ChatRowStatus(val color: Color) {
    COMPLETED(Success),
    PENDING(Warning),
    CLOSED(Slate400),
    ACTIVE(Info);

    companion object {
        fun of(chat: ChatDto): ChatRowStatus {
            val cs = (chat.claimStatus ?: "").uppercase()
            val itemStatus = (chat.itemStatus ?: "").uppercase()
            return when {
                cs == "COMPLETED" -> COMPLETED
                cs == "REJECTED" || cs == "CANCELLED" -> CLOSED
                itemStatus == "PENDING_OWNER_CONFIRMATION" -> PENDING
                else -> ACTIVE
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatDto, onClick: () -> Unit) {
    val unread = chat.unreadCount.toInt().coerceAtLeast(0)
    val displayName = chat.otherParticipantName.ifBlank { "Unknown" }
    val preview = chat.lastMessagePreview?.takeIf { it.isNotBlank() } ?: "No messages yet"
    val rowStatus = ChatRowStatus.of(chat)
    val itemStatus = chat.itemStatus?.takeIf { it.isNotBlank() }

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
            Box {
                AvatarView(name = displayName, size = 48.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(rowStatus.color)
                    )
                }
            }

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
                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
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
                    itemStatus?.let { StatusChip(status = it) }
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
