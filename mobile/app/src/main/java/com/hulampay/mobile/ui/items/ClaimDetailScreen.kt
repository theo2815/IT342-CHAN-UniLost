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
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockClaims
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimDetailScreen(navController: NavController, claimId: String) {
    val claim = remember { MockClaims.getClaimById(claimId) }
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    if (claim == null) {
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
                message = "This claim may have been removed or doesn't exist.",
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    val isClaimant = claim.claimantId == "u1"
    val isPoster = claim.posterId == "u1"

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Claim Details",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            // Action buttons based on claim status and role
            val showBottomBar = (isClaimant && claim.status == "PENDING") ||
                    (claim.status == "APPROVED" || claim.status == "HANDED_OVER")
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
                        // "Open Chat" for approved/handed_over claims
                        if (claim.status == "APPROVED" || claim.status == "HANDED_OVER") {
                            UniLostButton(
                                text = "Open Chat",
                                onClick = {
                                    navController.navigate("chat_detail_screen/${claim.id}")
                                },
                                icon = Icons.Default.Chat,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // "Cancel Claim" for pending claims (claimant only)
                        if (isClaimant && claim.status == "PENDING") {
                            UniLostButton(
                                text = "Cancel Claim",
                                onClick = { showCancelDialog = true },
                                variant = ButtonVariant.DANGER,
                                icon = Icons.Default.Cancel,
                                modifier = Modifier.weight(1f)
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
                        "Claimed ${MockItems.timeAgo(claim.createdAt)}",
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
                // Poster
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
                        AvatarView(name = claim.posterName, size = 36.dp)
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Column {
                            Text(
                                "Posted by",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                claim.posterName,
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
                    if (claim.secretDetailAnswer.isNotBlank()) {
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
                            claim.secretDetailAnswer,
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
            when (claim.status) {
                "PENDING" -> {
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
                                onClick = {
                                    Toast.makeText(context, "Claim rejected (Mock action)", Toast.LENGTH_SHORT).show()
                                },
                                variant = ButtonVariant.DANGER,
                                icon = Icons.Default.Close,
                                modifier = Modifier.weight(1f)
                            )
                            UniLostButton(
                                text = "Approve",
                                onClick = {
                                    Toast.makeText(context, "Claim approved (Mock action)", Toast.LENGTH_SHORT).show()
                                },
                                variant = ButtonVariant.PRIMARY,
                                icon = Icons.Default.Check,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                "REJECTED" -> {
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
                "APPROVED", "HANDED_OVER" -> {
                    HandoverStepper(
                        claim = claim,
                        isClaimant = isClaimant,
                        isPoster = isPoster,
                        onConfirm = {
                            Toast.makeText(context, "Handover confirmed! (Mock action)", Toast.LENGTH_SHORT).show()
                        }
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
                    "Are you sure you want to cancel your claim on \"${claim?.itemTitle}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        Toast.makeText(context, "Claim cancelled (Mock action)", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
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
    claim: com.hulampay.mobile.data.mock.MockClaim,
    isClaimant: Boolean,
    isPoster: Boolean,
    onConfirm: () -> Unit
) {
    val steps = listOf("Claim Approved", "Poster Confirms", "Claimant Confirms", "Handed Over")

    val currentStep = when {
        claim.status == "HANDED_OVER" || (claim.posterConfirmed && claim.claimantConfirmed) -> 4
        claim.posterConfirmed -> 2
        claim.claimantConfirmed -> 1
        else -> 1
    }

    val canConfirm = (isClaimant && !claim.claimantConfirmed) || (isPoster && !claim.posterConfirmed)

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

            // Steps
            steps.forEachIndexed { index, step ->
                val stepNum = index + 1
                val isCompleted = stepNum < currentStep || currentStep == 4
                val isActive = stepNum == currentStep && currentStep < 4

                Row(verticalAlignment = Alignment.Top) {
                    // Circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> Success
                                    isActive -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
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
                            fontWeight = if (isCompleted || isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCompleted || isActive) {
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

                // Connecting line
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .width(2.dp)
                            .height(UniLostSpacing.md)
                            .background(
                                if (isCompleted) Success else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Confirm button
            if (canConfirm) {
                UniLostButton(
                    text = "Confirm Handover",
                    onClick = onConfirm,
                    icon = Icons.Default.CheckCircle
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                Text(
                    "Confirm that you have ${if (isClaimant) "received" else "handed over"} the item.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        claim.posterConfirmed && !claim.claimantConfirmed && isClaimant ->
                            "The poster has confirmed. Please confirm once you receive the item."
                        claim.claimantConfirmed && !claim.posterConfirmed && isPoster ->
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
