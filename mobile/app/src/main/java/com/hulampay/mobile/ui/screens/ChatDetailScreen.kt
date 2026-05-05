package com.hulampay.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

/**
 * Chat Detail screen — Spec Section 10.7.
 * Now includes handover action buttons and claim status with navigation.
 */

private data class MockMessage(
    val id: String,
    val text: String,
    val isOwn: Boolean,
    val isSystem: Boolean = false,
    val time: String
)

private val mockMessages = listOf(
    MockMessage("m1", "Hi! I think I found your Samsung Galaxy S24.", false, time = "2:30 PM"),
    MockMessage("m2", "Really? Where did you find it?", true, time = "2:31 PM"),
    MockMessage("m3", "At the CIT-U library, 2nd floor near the computers.", false, time = "2:32 PM"),
    MockMessage("m4", "That's exactly where I lost it! Can you describe it?", true, time = "2:33 PM"),
    MockMessage("m5", "Black with a clear case, has a small scratch on the back.", false, time = "2:34 PM"),
    MockMessage("m6", "Claim submitted by Maria Santos", false, isSystem = true, time = "2:35 PM"),
    MockMessage("m7", "That's mine! The scratch is from dropping it last week.", true, time = "2:36 PM"),
    MockMessage("m8", "Can we meet at the campus security office?", false, time = "2:38 PM"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavController, chatId: String) {
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Mock claim state for this chat
    var claimStatus by remember { mutableStateOf("APPROVED") }
    var posterConfirmed by remember { mutableStateOf(false) }
    var claimantConfirmed by remember { mutableStateOf(false) }
    val isClaimant = true // Mock: current user is the claimant
    val mockClaimId = "c3" // Mock claim ID for navigation

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Maria Santos",
                onBackClick = { navController.popBackStack() },
                actions = {
                    // Navigate to claim detail
                    IconButton(onClick = {
                        navController.navigate("claim_detail_screen/$mockClaimId")
                    }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Claim Details",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Input Bar
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
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = "Type a message...",
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                    FilledIconButton(
                        onClick = { messageText = "" },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Claim status card (pinned) with handover actions
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Black Samsung Galaxy S24",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "FOUND item",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusChip(claimStatus)
                    }

                    // Handover actions (shown when claim is APPROVED or partially confirmed)
                    if (claimStatus == "APPROVED" || (claimStatus == "HANDED_OVER" && !(posterConfirmed && claimantConfirmed))) {
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                        when {
                            posterConfirmed && claimantConfirmed -> {
                                // Both confirmed
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
                            isClaimant && !claimantConfirmed -> {
                                // Claimant can confirm receipt
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                                ) {
                                    UniLostButton(
                                        text = "Confirm Received",
                                        onClick = {
                                            claimantConfirmed = true
                                            if (posterConfirmed) {
                                                claimStatus = "HANDED_OVER"
                                            }
                                            Toast.makeText(context, "Receipt confirmed!", Toast.LENGTH_SHORT).show()
                                        },
                                        icon = Icons.Default.CheckCircle,
                                        isCompact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    UniLostButton(
                                        text = "View Details",
                                        onClick = {
                                            navController.navigate("claim_detail_screen/$mockClaimId")
                                        },
                                        variant = ButtonVariant.SECONDARY,
                                        icon = Icons.Default.OpenInNew,
                                        isCompact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (posterConfirmed) {
                                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                                    Text(
                                        "The poster has confirmed handover. Please confirm receipt.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            !isClaimant && !posterConfirmed -> {
                                // Poster can confirm handover
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                                ) {
                                    UniLostButton(
                                        text = "Mark as Handed Over",
                                        onClick = {
                                            posterConfirmed = true
                                            if (claimantConfirmed) {
                                                claimStatus = "HANDED_OVER"
                                            }
                                            Toast.makeText(context, "Handover marked!", Toast.LENGTH_SHORT).show()
                                        },
                                        icon = Icons.Default.Handshake,
                                        isCompact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    UniLostButton(
                                        text = "View Details",
                                        onClick = {
                                            navController.navigate("claim_detail_screen/$mockClaimId")
                                        },
                                        variant = ButtonVariant.SECONDARY,
                                        icon = Icons.Default.OpenInNew,
                                        isCompact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (claimantConfirmed) {
                                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                                    Text(
                                        "The claimant has confirmed receipt. Please confirm handover.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    "Waiting for both parties to confirm handover.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                reverseLayout = true
            ) {
                items(mockMessages.reversed()) { message ->
                    when {
                        message.isSystem -> SystemMessage(message.text)
                        message.isOwn -> OwnMessage(message.text, message.time)
                        else -> OtherMessage(message.text, message.time)
                    }
                }
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
        Text(
            time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = UniLostSpacing.xxs)
        )
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
        Text(
            time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = UniLostSpacing.xxs)
        )
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
