package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimDetailScreen(
    navController: NavController,
    claimId: String,
    viewModel: ClaimDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val actionInFlight by viewModel.actionInFlight.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(claimId) {
        viewModel.load(claimId)
    }

    LaunchedEffect(actionError) {
        actionError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeActionError()
        }
    }

    when (val current = state) {
        UiState.Idle, UiState.Loading -> ClaimLoadingScaffold(navController)
        is UiState.Error -> ClaimErrorScaffold(navController, current.message)
        is UiState.Success -> ClaimDetailContent(
            navController = navController,
            data = current.data,
            actionInFlight = actionInFlight,
            onAccept = viewModel::acceptClaim,
            onReject = viewModel::rejectClaim,
            onCancel = {
                viewModel.cancelClaim()
            },
            onMarkReturned = viewModel::markReturned,
            onConfirmReceived = viewModel::confirmReceived,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaimLoadingScaffold(navController: NavController) {
    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Claim Details",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaimErrorScaffold(navController: NavController, message: String) {
    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Not Found",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        EmptyState(
            icon = Icons.Default.SearchOff,
            title = "Claim not found",
            message = message.ifBlank { "This claim may have been removed or doesn't exist." },
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaimDetailContent(
    navController: NavController,
    data: ClaimDetailData,
    actionInFlight: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onMarkReturned: () -> Unit,
    onConfirmReceived: () -> Unit,
) {
    val claim = data.claim
    var showCancelDialog by remember { mutableStateOf(false) }

    val isClaimant = data.isClaimant
    val isPoster = data.isPoster
    val isFinder = data.isFinder
    val isOwner = data.isOwner
    val isAccepted = claim.status.equals("ACCEPTED", ignoreCase = true)
    val isCompleted = claim.status.equals("COMPLETED", ignoreCase = true) ||
        claim.status.equals("HANDED_OVER", ignoreCase = true)

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Claim Details",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            val showBottomBar = (isClaimant && claim.status.equals("PENDING", ignoreCase = true)) ||
                isAccepted || isCompleted
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(UniLostSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        if (isAccepted || isCompleted) {
                            UniLostButton(
                                text = "Open Chat",
                                onClick = {
                                    val chatId = claim.chatId ?: claim.id
                                    navController.navigate("chat_detail_screen/$chatId")
                                },
                                icon = Icons.Default.Chat,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (isClaimant && claim.status.equals("PENDING", ignoreCase = true)) {
                            UniLostButton(
                                text = "Cancel Claim",
                                onClick = { showCancelDialog = true },
                                variant = ButtonVariant.DANGER,
                                icon = Icons.Default.Cancel,
                                modifier = Modifier.weight(1f),
                                enabled = !actionInFlight
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(UniLostSpacing.md),
            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.md)
        ) {
            // Claim Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = UniLostShapes.md,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(claim.itemType)
                        StatusChip(claim.status)
                    }
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        claim.itemTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Claimed ${timeAgo(claim.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Parties
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                // Poster (item reporter)
                val posterDisplayName = data.posterName.ifBlank { "Unknown" }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = UniLostShapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(UniLostSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(name = posterDisplayName, size = 36.dp)
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Column {
                            Text(
                                "Posted by",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                posterDisplayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Claimant
                Card(
                    modifier = Modifier.weight(1f),
                    shape = UniLostShapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(UniLostSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(name = claim.claimantName, size = 36.dp)
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Column {
                            Text(
                                "Claimed by",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                claim.claimantName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Claim Content
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = UniLostShapes.md,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                    val providedAnswer = claim.providedAnswer.orEmpty()
                    if (providedAnswer.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                            Text(
                                "SECRET DETAIL ANSWER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Text(
                            providedAnswer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                        Text(
                            "MESSAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        claim.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Status-specific content
            when {
                claim.status.equals("PENDING", ignoreCase = true) -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = UniLostShapes.md,
                        colors = CardDefaults.cardColors(
                            containerColor = WarningBg
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(UniLostSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = WarningHover,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            Column {
                                Text(
                                    "Waiting for Review",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningHover
                                )
                                Text(
                                    if (isClaimant) "The poster has not reviewed your claim yet."
                                    else "Review this claim and approve or reject it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarningHover.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Approve/Reject buttons for poster
                    if (isPoster) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            UniLostButton(
                                text = "Reject",
                                onClick = onReject,
                                variant = ButtonVariant.DANGER,
                                icon = Icons.Default.Close,
                                modifier = Modifier.weight(1f),
                                enabled = !actionInFlight
                            )
                            UniLostButton(
                                text = "Approve",
                                onClick = onAccept,
                                variant = ButtonVariant.PRIMARY,
                                icon = Icons.Default.Check,
                                modifier = Modifier.weight(1f),
                                enabled = !actionInFlight
                            )
                        }
                    }
                }
                claim.status.equals("REJECTED", ignoreCase = true) -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = UniLostShapes.md,
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorBg
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(UniLostSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            Column {
                                Text(
                                    "Claim Rejected",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ErrorRed
                                )
                                Text(
                                    "The poster did not approve this claim.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ErrorRed.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                isAccepted || isCompleted -> {
                    HandoverStepper(
                        claim = claim,
                        isFinder = isFinder,
                        isOwner = isOwner,
                        actionInFlight = actionInFlight,
                        onMarkReturned = onMarkReturned,
                        onConfirmReceived = onConfirmReceived,
                    )
                }
            }
        }
    }

    // Cancel Claim Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    "Cancel Claim",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to cancel your claim on \"${claim.itemTitle}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Claim", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Claim")
                }
            },
            shape = UniLostShapes.lg
        )
    }
}

@Composable
fun HandoverStepper(
    claim: ClaimDto,
    isFinder: Boolean,
    isOwner: Boolean,
    actionInFlight: Boolean,
    onMarkReturned: () -> Unit,
    onConfirmReceived: () -> Unit,
) {
    val steps = listOf("Claim Approved", "Poster Confirms", "Claimant Confirms", "Handed Over")

    val finderConfirmed = claim.finderMarkedReturnedAt != null
    val ownerConfirmed = claim.ownerConfirmedReceivedAt != null
    val isCompleted = claim.status.equals("COMPLETED", ignoreCase = true) ||
        claim.status.equals("HANDED_OVER", ignoreCase = true)

    val currentStep = when {
        isCompleted || (finderConfirmed && ownerConfirmed) -> 4
        finderConfirmed -> 2
        ownerConfirmed -> 1
        else -> 1
    }

    val canMarkReturned = isFinder && !finderConfirmed && !isCompleted
    val canConfirmReceived = isOwner && finderConfirmed && !ownerConfirmed && !isCompleted

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(UniLostSpacing.md)) {
            Text(
                "Handover Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            steps.forEachIndexed { index, step ->
                val stepNum = index + 1
                val isStepCompleted = stepNum < currentStep || currentStep == 4
                val isActive = stepNum == currentStep && currentStep < 4

                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isStepCompleted -> Success
                                    isActive -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isStepCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "$stepNum",
                                color = White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(UniLostSpacing.sm))

                    Column {
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isStepCompleted || isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isStepCompleted || isActive) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (index < steps.size - 1) {
                            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        }
                    }
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .width(2.dp)
                            .height(UniLostSpacing.md)
                            .background(
                                if (isStepCompleted) Success else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Action button — call the right backend endpoint based on role + state
            when {
                canMarkReturned -> {
                    UniLostButton(
                        text = "Mark as Returned",
                        onClick = onMarkReturned,
                        icon = Icons.Default.CheckCircle,
                        enabled = !actionInFlight
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Confirm that you have handed over the item.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                canConfirmReceived -> {
                    UniLostButton(
                        text = "Confirm Receipt",
                        onClick = onConfirmReceived,
                        icon = Icons.Default.CheckCircle,
                        enabled = !actionInFlight
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Confirm that you have received the item.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Status text
            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
            if (currentStep == 4) {
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
            } else {
                Text(
                    when {
                        finderConfirmed && !ownerConfirmed && isOwner ->
                            "The poster has confirmed. Please confirm once you receive the item."
                        ownerConfirmed && !finderConfirmed && isFinder ->
                            "The claimant has confirmed. Please confirm the handover."
                        else -> "Both parties need to confirm the handover."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location hint
            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = UniLostShapes.sm,
                color = InfoBg
            ) {
                Row(
                    modifier = Modifier.padding(UniLostSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Info,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Text(
                        "Suggested meetup: Campus Security Office",
                        style = MaterialTheme.typography.bodySmall,
                        color = Info
                    )
                }
            }
        }
    }
}
