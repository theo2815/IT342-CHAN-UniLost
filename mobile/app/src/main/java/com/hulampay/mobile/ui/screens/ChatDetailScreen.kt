package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ChatDto
import com.hulampay.mobile.data.model.MessageDto
import com.hulampay.mobile.data.ws.ChatWebSocketClient
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

/**
 * Chat Detail screen — Spec Section 10.7.
 * Wired to /api/chats/{id} + /api/chats/{id}/messages and the STOMP topic
 * /topic/chat/{id}. Pinned claim card runs the live mark-returned /
 * confirm-received / dispute-handover actions against /api/claims/{id}.
 *
 * Pass 2 additions (handover-UX parity with website):
 *  - Progress tracker + next-action banner under the header card.
 *  - CLAIM_SUBMISSION rendered as an inline card with inline Accept/Reject
 *    for the holder when the claim is still PENDING (FOUND items only).
 *  - System messages (CLAIM_ACCEPTED/REJECTED, HANDOVER_*) get per-type icons
 *    and accent backgrounds; HANDOVER_CONFIRMED surfaces karma totals.
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
    val loadingOlder by viewModel.loadingOlder.collectAsState()
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
    val claimStatusUpper = (chat?.claimStatus ?: "").uppercase()
    val isChatEnded = claimStatusUpper == "COMPLETED" ||
        claimStatusUpper == "REJECTED" ||
        claimStatusUpper == "CANCELLED"

    var pendingConfirm by remember { mutableStateOf<PendingConfirm?>(null) }

    val backToChatList: () -> Unit = {
        // Prefer popping the current entry so we land on whatever invoked us
        // (chat list, notification, claim detail, etc.). Fall back to
        // navigating to the chat list if there is nothing to pop — e.g. when
        // ChatDetail is the start destination of a deep link.
        val popped = navController.popBackStack()
        if (!popped) {
            navController.navigate(Screen.ChatList.route) {
                popUpTo(Screen.ChatList.route) {
                    inclusive = false
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = chat?.otherParticipantName?.takeIf { it.isNotBlank() } ?: "Chat",
                onBackClick = backToChatList,
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
            if (isChatEnded) {
                ChatEndedBanner()
            } else {
                ChatInputBar(
                    value = messageText,
                    onValueChange = { next -> messageText = next.take(MESSAGE_MAX_CHARS) },
                    onSend = {
                        if (messageText.isNotBlank() && !sending) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = data != null && !sending,
                    onQuickReply = { canned -> messageText = canned.take(MESSAGE_MAX_CHARS) },
                )
            }
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
                        onMarkReturned = {
                            pendingConfirm = PendingConfirm(
                                title = "Mark Item as Returned?",
                                message = "Confirm that you have physically handed the item to the owner. They will be asked to verify receipt.",
                                confirmLabel = "Yes, I Returned It",
                                variant = ButtonVariant.PRIMARY,
                                onConfirm = { viewModel.markReturned() },
                            )
                        },
                        onConfirmReceived = {
                            pendingConfirm = PendingConfirm(
                                title = "Confirm Item Received?",
                                message = "Confirm that you have received your item. This will complete the handover and award karma to both parties.",
                                confirmLabel = "Yes, I Received It",
                                variant = ButtonVariant.PRIMARY,
                                onConfirm = { viewModel.confirmReceived() },
                            )
                        },
                        onDispute = {
                            pendingConfirm = PendingConfirm(
                                title = "Report Problem?",
                                message = "Report that you did not receive the item. The handover status will be reverted so the other party can try again.",
                                confirmLabel = "I Did Not Receive It",
                                variant = ButtonVariant.DANGER,
                                onConfirm = { viewModel.disputeHandover() },
                            )
                        },
                        onViewClaim = {
                            current.data.chat.claimId?.let { claimId ->
                                navController.navigate("claim_detail_screen/$claimId")
                            }
                        },
                    )

                    val isLostItem = (current.data.chat.itemType ?: "")
                        .equals("LOST", ignoreCase = true)
                    val claimCancelled = (current.data.chat.claimStatus ?: "")
                        .equals("CANCELLED", ignoreCase = true)
                    if (!claimCancelled) {
                        HandoverProgressBlock(
                            steps = getProgressSteps(current.data.chat),
                            nextAction = getNextAction(
                                chat = current.data.chat,
                                isFinder = current.data.isFinder,
                                isOwner = current.data.isOwner,
                                isLostItem = isLostItem,
                            ),
                        )
                    }

                    if (wsConnection == ChatWebSocketClient.ConnectionState.CONNECTING ||
                        wsConnection == ChatWebSocketClient.ConnectionState.DISCONNECTED
                    ) {
                        ConnectionBanner(state = wsConnection)
                    }

                    val listState = rememberLazyListState()
                    val displayMessages = remember(current.data.messages) {
                        current.data.messages.asReversed()
                    }
                    val rows = remember(displayMessages) {
                        buildChatRows(displayMessages)
                    }
                    // Only auto-scroll to newest when the user is already near the
                    // bottom of the list (within ~3 items). Mirrors the website's
                    // `isNearBottom < 150px` heuristic.
                    LaunchedEffect(displayMessages.size) {
                        if (displayMessages.isNotEmpty() &&
                            listState.firstVisibleItemIndex <= 2
                        ) {
                            listState.animateScrollToItem(0)
                        }
                    }
                    // Load older messages once the user scrolls within 3 rows of
                    // the visual top (= the oldest currently-loaded message).
                    val nearTop by remember {
                        derivedStateOf {
                            val info = listState.layoutInfo
                            val total = info.totalItemsCount
                            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                            total > 0 && lastVisible >= total - 3
                        }
                    }
                    LaunchedEffect(nearTop) {
                        if (nearTop &&
                            current.data.hasMoreOlder &&
                            !loadingOlder
                        ) {
                            viewModel.loadOlderMessages()
                        }
                    }
                    val activeClaimId = current.data.chat.claimId
                    val canActOnPendingClaim = current.data.isFinder &&
                        (current.data.chat.claimStatus ?: "").uppercase() == "PENDING" &&
                        !isLostItem
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
                        items(rows, key = { it.key }) { row ->
                            when (row) {
                                is ChatRow.DateSep -> DateSeparator(row.label)
                                is ChatRow.Msg -> {
                                    val message = row.message
                                    when {
                                        message.type.equals("CLAIM_SUBMISSION", ignoreCase = true) -> {
                                            val claimIdMatches = message.metadata
                                                ?.get("claimId")
                                                ?.toString()
                                                ?.takeIf { it.isNotBlank() && it != "null" }
                                                ?.let { it == activeClaimId }
                                                ?: false
                                            ClaimSubmissionMessage(
                                                message = message,
                                                time = formatTime(message.createdAt),
                                                canAct = canActOnPendingClaim && claimIdMatches,
                                                actionInFlight = actionInFlight,
                                                onAccept = { viewModel.acceptClaim() },
                                                onReject = { viewModel.rejectClaim() },
                                            )
                                        }

                                        !message.type.equals("TEXT", ignoreCase = true) ->
                                            TypedSystemMessage(
                                                message = message,
                                                time = formatTime(message.createdAt),
                                            )

                                        !message.senderId.isNullOrBlank() &&
                                            message.senderId == current.data.currentUserId ->
                                                OwnMessage(
                                                    text = message.content,
                                                    time = formatTime(message.createdAt),
                                                    read = message.read,
                                                )

                                        else ->
                                            OtherMessage(message.content, formatTime(message.createdAt))
                                    }
                                }
                            }
                        }
                        if (current.data.hasMoreOlder && loadingOlder) {
                            item(key = "older-loading") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = UniLostSpacing.sm),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingConfirm?.let { config ->
        UniLostConfirmDialog(
            title = config.title,
            message = config.message,
            confirmLabel = config.confirmLabel,
            confirmVariant = config.variant,
            onConfirm = {
                config.onConfirm()
                pendingConfirm = null
            },
            onDismiss = { pendingConfirm = null },
        )
    }
}

private data class PendingConfirm(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val variant: ButtonVariant,
    val onConfirm: () -> Unit,
)

// ── Progress + next-action helpers ──────────────────────────────────────────

private enum class ProgressState { TODO, ACTIVE, DONE, REJECTED }
private data class ProgressStep(val label: String, val state: ProgressState)

private enum class NextActionType { ACTION, WAITING, SUCCESS, ERROR, MUTED }
private data class NextAction(val text: String, val type: NextActionType)

private fun getProgressSteps(chat: ChatDto): List<ProgressStep> {
    val cs = (chat.claimStatus ?: "").uppercase()
    val itemStatus = (chat.itemStatus ?: "").uppercase()
    val reviewedDone = cs == "ACCEPTED" || cs == "COMPLETED" || cs == "REJECTED"
    val returnedDone = itemStatus == "PENDING_OWNER_CONFIRMATION" || itemStatus == "RETURNED"
    val confirmedDone = cs == "COMPLETED"
    return listOf(
        ProgressStep("Filed", ProgressState.DONE),
        ProgressStep(
            label = if (cs == "REJECTED") "Rejected" else "Reviewed",
            state = when {
                cs == "REJECTED" -> ProgressState.REJECTED
                reviewedDone -> ProgressState.DONE
                else -> ProgressState.TODO
            },
        ),
        ProgressStep(
            label = "Returned",
            state = when {
                returnedDone -> ProgressState.DONE
                cs == "ACCEPTED" && itemStatus == "CLAIMED" -> ProgressState.ACTIVE
                else -> ProgressState.TODO
            },
        ),
        ProgressStep(
            label = "Confirmed",
            state = when {
                confirmedDone -> ProgressState.DONE
                itemStatus == "PENDING_OWNER_CONFIRMATION" -> ProgressState.ACTIVE
                else -> ProgressState.TODO
            },
        ),
    )
}

private fun getNextAction(
    chat: ChatDto,
    isFinder: Boolean,
    isOwner: Boolean,
    isLostItem: Boolean,
): NextAction? {
    val cs = (chat.claimStatus ?: "").uppercase()
    val itemStatus = (chat.itemStatus ?: "").uppercase()
    return when {
        cs == "COMPLETED" -> NextAction(
            "Handover complete! Both parties earned karma.",
            NextActionType.SUCCESS,
        )
        cs == "REJECTED" -> NextAction("This claim was rejected.", NextActionType.ERROR)
        cs == "CANCELLED" -> NextAction("This claim was cancelled.", NextActionType.MUTED)
        itemStatus == "PENDING_OWNER_CONFIRMATION" ->
            if (isOwner) NextAction(
                "Your turn — confirm you received the item, or report a problem.",
                NextActionType.ACTION,
            )
            else NextAction(
                "Waiting for the owner to confirm receipt.",
                NextActionType.WAITING,
            )
        cs == "ACCEPTED" && itemStatus == "CLAIMED" ->
            if (isFinder) NextAction(
                "Your turn — meet up and mark the item as returned.",
                NextActionType.ACTION,
            )
            else NextAction(
                "Waiting for the item to be returned to you.",
                NextActionType.WAITING,
            )
        cs == "PENDING" && !isLostItem ->
            if (isFinder) NextAction(
                "Your turn — review this claim and accept or reject.",
                NextActionType.ACTION,
            )
            else NextAction(
                "Waiting for the finder to review your claim.",
                NextActionType.WAITING,
            )
        cs == "PENDING" && isLostItem -> NextAction(
            "Claim submitted — waiting for auto-verification.",
            NextActionType.WAITING,
        )
        else -> null
    }
}

@Composable
private fun HandoverProgressBlock(
    steps: List<ProgressStep>,
    nextAction: NextAction?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md)
            .padding(bottom = UniLostSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, step ->
                ProgressStepCol(
                    step = step,
                    showLeadLine = index > 0,
                    showTailLine = index < steps.size - 1,
                    leadFilled = index > 0 && steps[index - 1].state == ProgressState.DONE,
                    tailFilled = step.state == ProgressState.DONE,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        nextAction?.let { NextActionBanner(it) }
    }
}

@Composable
private fun ProgressStepCol(
    step: ProgressStep,
    showLeadLine: Boolean,
    showTailLine: Boolean,
    leadFilled: Boolean,
    tailFilled: Boolean,
    modifier: Modifier = Modifier,
) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val primary = MaterialTheme.colorScheme.primary
    val dotColor = when (step.state) {
        ProgressState.DONE -> Success
        ProgressState.ACTIVE -> primary
        ProgressState.REJECTED -> ErrorRed
        ProgressState.TODO -> outline
    }
    val labelColor = when (step.state) {
        ProgressState.DONE -> Success
        ProgressState.ACTIVE -> primary
        ProgressState.REJECTED -> ErrorRed
        ProgressState.TODO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(
                        when {
                            !showLeadLine -> Color.Transparent
                            leadFilled -> Success
                            else -> outline
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center,
            ) {
                when (step.state) {
                    ProgressState.DONE -> Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                    ProgressState.REJECTED -> Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                    else -> {}
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(
                        when {
                            !showTailLine -> Color.Transparent
                            tailFilled -> Success
                            else -> outline
                        }
                    )
            )
        }
        Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
        Text(
            text = step.label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = when (step.state) {
                ProgressState.ACTIVE, ProgressState.DONE, ProgressState.REJECTED -> FontWeight.SemiBold
                ProgressState.TODO -> FontWeight.Normal
            },
        )
    }
}

@Composable
private fun NextActionBanner(action: NextAction) {
    val primary = MaterialTheme.colorScheme.primary
    val mutedBg = MaterialTheme.colorScheme.surfaceVariant
    val mutedFg = MaterialTheme.colorScheme.onSurfaceVariant
    val (bg, fg, icon) = when (action.type) {
        NextActionType.ACTION -> Triple(primary.copy(alpha = 0.08f), primary, Icons.AutoMirrored.Filled.ArrowForward)
        NextActionType.WAITING -> Triple(WarningBg, Warning, Icons.Default.Schedule)
        NextActionType.SUCCESS -> Triple(SuccessBg, Success, Icons.Default.EmojiEvents)
        NextActionType.ERROR -> Triple(ErrorBg, ErrorRed, Icons.Default.Cancel)
        NextActionType.MUTED -> Triple(mutedBg, mutedFg, Icons.Default.Info)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = UniLostShapes.sm,
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = UniLostSpacing.sm,
                vertical = UniLostSpacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
            Text(
                action.text,
                style = MaterialTheme.typography.bodySmall,
                color = fg,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ChatEndedBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Text(
                text = "This conversation has ended.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
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
                Column(horizontalAlignment = Alignment.End) {
                    if (claimStatus.isNotBlank()) {
                        StatusChip(claimStatus)
                    }
                    if (itemStatus.isNotBlank()) {
                        if (claimStatus.isNotBlank()) {
                            Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                        }
                        StatusChip(itemStatus)
                    }
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

private const val MESSAGE_MAX_CHARS = 2000
private const val MESSAGE_WARN_THRESHOLD = 1800

private val QUICK_REPLIES = listOf(
    "I'm here now",
    "Running a bit late, be there soon",
    "Can we meet at the security office?",
)

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    onQuickReply: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = UniLostSpacing.md,
                vertical = UniLostSpacing.sm,
            ),
        ) {
            QuickReplyRow(onSelect = onQuickReply)
            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            if (value.length > MESSAGE_WARN_THRESHOLD) {
                val atLimit = value.length >= MESSAGE_MAX_CHARS
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    text = "${MESSAGE_MAX_CHARS - value.length} characters remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (atLimit) ErrorRed else Warning,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
private fun QuickReplyRow(onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
    ) {
        QUICK_REPLIES.forEach { reply ->
            AssistChip(
                onClick = { onSelect(reply) },
                label = {
                    Text(
                        text = reply,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                shape = UniLostShapes.full,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                ),
            )
        }
    }
}

@Composable
private fun OwnMessage(text: String, time: String, read: Boolean) {
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
        Row(
            modifier = Modifier.padding(top = UniLostSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xxs),
        ) {
            if (time.isNotBlank()) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = if (read) Icons.Default.DoneAll else Icons.Default.Done,
                contentDescription = if (read) "Read" else "Sent",
                tint = if (read) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp),
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

// ── System message rendering ────────────────────────────────────────────────

private data class SystemMessageStyle(
    val background: Color,
    val accent: Color,
    val icon: ImageVector,
)

@Composable
private fun systemMessageStyle(type: String): SystemMessageStyle {
    val fallbackBg = MaterialTheme.colorScheme.surfaceVariant
    val fallbackFg = MaterialTheme.colorScheme.onSurfaceVariant
    return when (type.uppercase()) {
        "CLAIM_ACCEPTED" -> SystemMessageStyle(SuccessBg, Success, Icons.Default.CheckCircle)
        "CLAIM_REJECTED" -> SystemMessageStyle(ErrorBg, ErrorRed, Icons.Default.Cancel)
        "HANDOVER_REQUEST" -> SystemMessageStyle(WarningBg, Warning, Icons.Default.Handshake)
        "HANDOVER_CONFIRMED" -> SystemMessageStyle(SuccessBg, Success, Icons.Default.Inventory2)
        "HANDOVER_DISPUTED" -> SystemMessageStyle(ErrorBg, ErrorRed, Icons.Default.ReportProblem)
        else -> SystemMessageStyle(fallbackBg, fallbackFg, Icons.Default.Info)
    }
}

@Composable
private fun TypedSystemMessage(message: MessageDto, time: String) {
    val style = systemMessageStyle(message.type)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = UniLostShapes.md,
                color = style.background,
                modifier = Modifier.widthIn(max = 320.dp),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = UniLostSpacing.sm,
                        vertical = UniLostSpacing.xs,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            style.icon,
                            contentDescription = null,
                            tint = style.accent,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.labelMedium,
                            color = style.accent,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (message.type.equals("HANDOVER_CONFIRMED", ignoreCase = true)) {
                        val finderKarma = (message.metadata?.get("finderKarma") as? Number)?.toInt()
                        val ownerKarma = (message.metadata?.get("ownerKarma") as? Number)?.toInt()
                        if (finderKarma != null || ownerKarma != null) {
                            Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Warning,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(UniLostSpacing.xxs))
                                Text(
                                    text = buildKarmaSummary(finderKarma, ownerKarma),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            if (time.isNotBlank()) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = UniLostSpacing.xxs),
                )
            }
        }
    }
}

private fun buildKarmaSummary(finderKarma: Int?, ownerKarma: Int?): String {
    val parts = mutableListOf<String>()
    if (finderKarma != null) parts.add("Finder +$finderKarma karma")
    if (ownerKarma != null) parts.add("Owner +$ownerKarma karma")
    return parts.joinToString(", ")
}

@Composable
private fun ClaimSubmissionMessage(
    message: MessageDto,
    time: String,
    canAct: Boolean,
    actionInFlight: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    val meta = message.metadata
    val itemTitle = meta.metaString("itemTitle")
    val claimantName = meta.metaString("claimantName")
    val providedAnswer = meta.metaString("providedAnswer")
    val claimMessage = meta.metaString("claimMessage")

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                shape = UniLostShapes.md,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.widthIn(max = 320.dp),
            ) {
                Column(
                    modifier = Modifier.padding(UniLostSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xxs),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                        Text(
                            text = "Claim Submitted",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (!itemTitle.isNullOrBlank()) ClaimField("Item:", itemTitle)
                    if (!claimantName.isNullOrBlank()) ClaimField("By:", claimantName)
                    if (!providedAnswer.isNullOrBlank()) ClaimField("Verification Answer:", providedAnswer)
                    if (!claimMessage.isNullOrBlank()) ClaimField("Message:", claimMessage)

                    if (canAct) {
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                        ) {
                            UniLostButton(
                                text = "Accept",
                                onClick = onAccept,
                                icon = Icons.Default.CheckCircle,
                                isCompact = true,
                                isLoading = actionInFlight,
                                enabled = !actionInFlight,
                                modifier = Modifier.weight(1f),
                            )
                            UniLostButton(
                                text = "Reject",
                                onClick = onReject,
                                variant = ButtonVariant.DANGER,
                                icon = Icons.Default.Cancel,
                                isCompact = true,
                                isLoading = actionInFlight,
                                enabled = !actionInFlight,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            if (time.isNotBlank()) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = UniLostSpacing.xxs),
                )
            }
        }
    }
}

@Composable
private fun ClaimField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = UniLostSpacing.xs),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun Map<String, Any?>?.metaString(key: String): String? =
    this?.get(key)?.toString()?.takeIf { it.isNotBlank() && it != "null" }

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val tIndex = iso.indexOf('T')
    if (tIndex < 0 || tIndex + 6 > iso.length) return ""
    return iso.substring(tIndex + 1, tIndex + 6)
}

// ── Date separators ─────────────────────────────────────────────────────────

private sealed interface ChatRow {
    val key: String

    data class Msg(val message: MessageDto) : ChatRow {
        override val key: String = "m_${message.id}"
    }

    data class DateSep(val label: String, val anchorId: String) : ChatRow {
        override val key: String = "sep_$anchorId"
    }
}

/**
 * Flattens a newest-first message list into LazyColumn rows for `reverseLayout`.
 * Emits a date separator AFTER the chronologically-first message of each day so
 * it lands visually ABOVE that message. Matches website `formatDateSeparator`
 * keyed by `toDateString()`.
 */
private fun buildChatRows(displayMessages: List<MessageDto>): List<ChatRow> {
    if (displayMessages.isEmpty()) return emptyList()
    val out = ArrayList<ChatRow>(displayMessages.size + 4)
    for (idx in displayMessages.indices) {
        val msg = displayMessages[idx]
        out.add(ChatRow.Msg(msg))
        val msgKey = dayKey(msg.createdAt) ?: continue
        // displayMessages is newest-first, so the chronologically-older neighbour
        // sits at the NEXT index. When its day differs (or doesn't exist), the
        // current message is the first-of-day → emit the separator above it.
        val olderNeighbour = displayMessages.getOrNull(idx + 1)
        val olderKey = dayKey(olderNeighbour?.createdAt)
        if (olderKey != msgKey) {
            val label = formatDateSeparator(msg.createdAt) ?: continue
            out.add(ChatRow.DateSep(label = label, anchorId = msg.id))
        }
    }
    return out
}

private fun dayKey(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    val tIndex = iso.indexOf('T')
    return if (tIndex > 0) iso.substring(0, tIndex) else iso
}

private fun formatDateSeparator(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    val cleaned = iso.substringBefore('.').substringBefore('+').substringBefore('Z').take(19)
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = fmt.parse(cleaned) ?: return null
        val cal = Calendar.getInstance().apply { time = date }
        val today = Calendar.getInstance()
        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        when {
            isSameDay(cal, today) -> "Today"
            isSameDay(cal, yesterday) -> "Yesterday"
            else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        null
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

@Composable
private fun DateSeparator(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = UniLostShapes.full,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    horizontal = UniLostSpacing.sm,
                    vertical = UniLostSpacing.xxs,
                ),
            )
        }
    }
}
