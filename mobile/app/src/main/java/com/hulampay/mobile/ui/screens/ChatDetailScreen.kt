package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.ws.ChatWebSocketClient
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

/**
 * Chat Detail screen — Spec Section 10.7.
 * Wired to /api/chats/{id} + /api/chats/{id}/messages and the STOMP topic
 * /topic/chat/{id}. Pinned claim card runs the live mark-returned /
 * confirm-received / dispute-handover actions against /api/claims/{id}.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String,
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sending by viewModel.sending.collectAsState()
    val actionInFlight by viewModel.actionInFlight.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val wsConnection by viewModel.wsConnectionState.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chatId) {
        viewModel.load(chatId)
    }
    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionError()
        }
    }

    val data = (state as? UiState.Success<ChatDetailData>)?.data
    val chat = data?.chat

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = chat?.otherParticipantName?.takeIf { it.isNotBlank() } ?: "Chat",
                onBackClick = { navController.popBackStack() },
                actions = {
                    val claimId = chat?.claimId
                    if (!claimId.isNullOrBlank()) {
                        IconButton(onClick = {
                            navController.navigate("claim_detail_screen/$claimId")
                        }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Claim Details",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank() && !sending) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = data != null && !sending,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    ChatHeaderCard(
                        data = current.data,
                        actionInFlight = actionInFlight,
                        onMarkReturned = viewModel::markReturned,
                        onConfirmReceived = viewModel::confirmReceived,
                        onDispute = viewModel::disputeHandover,
                        onViewClaim = {
                            current.data.chat.claimId?.let { claimId ->
                                navController.navigate("claim_detail_screen/$claimId")
                            }
                        },
                    )

                    if (wsConnection == ChatWebSocketClient.ConnectionState.CONNECTING ||
                        wsConnection == ChatWebSocketClient.ConnectionState.DISCONNECTED
                    ) {
                        ConnectionBanner(state = wsConnection)
                    }

                    val listState = rememberLazyListState()
                    val displayMessages = remember(current.data.messages) {
                        current.data.messages.asReversed()
                    }
                    LaunchedEffect(current.data.messages.size) {
                        if (displayMessages.isNotEmpty()) {
                            listState.animateScrollToItem(0)
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = UniLostSpacing.md,
                            vertical = UniLostSpacing.sm,
                        ),
                        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                        reverseLayout = true
                    ) {
                        items(displayMessages, key = { it.id }) { message ->
                            val isOwn = !message.senderId.isNullOrBlank() &&
                                message.senderId == current.data.currentUserId &&
                                message.type == "TEXT"
                            val isSystem = message.type != "TEXT"
                            when {
                                isSystem -> SystemMessage(message.content)
                                isOwn -> OwnMessage(message.content, formatTime(message.createdAt))
                                else -> OtherMessage(message.content, formatTime(message.createdAt))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHeaderCard(
    data: ChatDetailData,
    actionInFlight: Boolean,
    onMarkReturned: () -> Unit,
    onConfirmReceived: () -> Unit,
    onDispute: () -> Unit,
    onViewClaim: () -> Unit,
) {
    val chat = data.chat
    val claimStatus = (chat.claimStatus ?: "").uppercase()
    val itemStatus = (chat.itemStatus ?: "").uppercase()
    val handoverPending = itemStatus == "PENDING_OWNER_CONFIRMATION"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(UniLostSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        chat.itemTitle.ifBlank { "Item" },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        (chat.itemType ?: "").ifBlank { "—" } + " item",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (claimStatus.isNotBlank()) {
                    StatusChip(claimStatus)
                }
            }

            HandoverActionRow(
                data = data,
                claimStatus = claimStatus,
                handoverPending = handoverPending,
                actionInFlight = actionInFlight,
                onMarkReturned = onMarkReturned,
                onConfirmReceived = onConfirmReceived,
                onDispute = onDispute,
                onViewClaim = onViewClaim,
            )
        }
    }
}

@Composable
private fun HandoverActionRow(
    data: ChatDetailData,
    claimStatus: String,
    handoverPending: Boolean,
    actionInFlight: Boolean,
    onMarkReturned: () -> Unit,
    onConfirmReceived: () -> Unit,
    onDispute: () -> Unit,
    onViewClaim: () -> Unit,
) {
    val claimId = data.chat.claimId
    if (claimId.isNullOrBlank()) return
    val show = claimStatus == "ACCEPTED" || claimStatus == "COMPLETED"
    if (!show) return

    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

    when {
        claimStatus == "COMPLETED" -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = UniLostShapes.sm,
                color = SuccessBg
            ) {
                Row(
                    modifier = Modifier.padding(UniLostSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Text(
                        "Item successfully handed over!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Success,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        data.isOwner && handoverPending -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                UniLostButton(
                    text = "Confirm Received",
                    onClick = onConfirmReceived,
                    icon = Icons.Default.CheckCircle,
                    isCompact = true,
                    isLoading = actionInFlight,
                    enabled = !actionInFlight,
                    modifier = Modifier.weight(1f)
                )
                UniLostButton(
                    text = "Dispute",
                    onClick = onDispute,
                    variant = ButtonVariant.DANGER,
                    icon = Icons.Default.ReportProblem,
                    isCompact = true,
                    isLoading = actionInFlight,
                    enabled = !actionInFlight,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
            Text(
                "The finder has marked the item returned. Please confirm receipt or dispute.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        data.isFinder && !handoverPending -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                UniLostButton(
                    text = "Mark as Returned",
                    onClick = onMarkReturned,
                    icon = Icons.Default.Handshake,
                    isCompact = true,
                    isLoading = actionInFlight,
                    enabled = !actionInFlight,
                    modifier = Modifier.weight(1f)
                )
                UniLostButton(
                    text = "View Details",
                    onClick = onViewClaim,
                    variant = ButtonVariant.SECONDARY,
                    icon = Icons.Default.OpenInNew,
                    isCompact = true,
                    enabled = !actionInFlight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        data.isFinder && handoverPending -> {
            Text(
                "Waiting for the owner to confirm receipt.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        else -> {
            Text(
                "Waiting for the finder to mark the item as returned.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConnectionBanner(state: ChatWebSocketClient.ConnectionState) {
    val (label, color) = when (state) {
        ChatWebSocketClient.ConnectionState.CONNECTING -> "Connecting to chat…" to MaterialTheme.colorScheme.onSurfaceVariant
        ChatWebSocketClient.ConnectionState.DISCONNECTED -> "Live updates unavailable" to MaterialTheme.colorScheme.error
        else -> return
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md),
        shape = UniLostShapes.sm,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = UniLostSpacing.sm, vertical = UniLostSpacing.xs)
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UniLostTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "Type a message...",
                modifier = Modifier.weight(1f),
                height = 48.dp
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            FilledIconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OwnMessage(text: String, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = UniLostShapes.md,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(
                    horizontal = UniLostSpacing.sm,
                    vertical = UniLostSpacing.sm
                )
            )
        }
        if (time.isNotBlank()) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = UniLostSpacing.xxs)
            )
        }
    }
}

@Composable
private fun OtherMessage(text: String, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = UniLostShapes.md,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = UniLostSpacing.sm,
                    vertical = UniLostSpacing.sm
                )
            )
        }
        if (time.isNotBlank()) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = UniLostSpacing.xxs)
            )
        }
    }
}

@Composable
private fun SystemMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = UniLostShapes.full,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = UniLostSpacing.sm, vertical = UniLostSpacing.xs)
            )
        }
    }
}

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val tIndex = iso.indexOf('T')
    if (tIndex < 0 || tIndex + 6 > iso.length) return ""
    return iso.substring(tIndex + 1, tIndex + 6)
}
