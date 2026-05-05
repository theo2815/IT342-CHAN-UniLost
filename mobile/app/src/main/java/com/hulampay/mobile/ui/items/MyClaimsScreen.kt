package com.hulampay.mobile.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockClaim
import com.hulampay.mobile.data.mock.MockClaims
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClaimsScreen(navController: NavController) {
    val filters = listOf("All", "Pending", "Approved", "Rejected", "Handed Over")
    var selectedFilter by remember { mutableStateOf("All") }

    val myClaims = remember { MockClaims.getMyOutgoingClaims("u1") }

    val filteredClaims = remember(selectedFilter, myClaims) {
        when (selectedFilter) {
            "Pending" -> myClaims.filter { it.status == "PENDING" }
            "Approved" -> myClaims.filter { it.status == "APPROVED" }
            "Rejected" -> myClaims.filter { it.status == "REJECTED" }
            "Handed Over" -> myClaims.filter { it.status == "HANDED_OVER" }
            else -> myClaims
        }
    }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "My Claims",
                onBackClick = { navController.popBackStack() }
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

            if (filteredClaims.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Description,
                    title = "No claims here",
                    message = "Claims you submit on items will appear here",
                    actionLabel = "Browse Items",
                    actionIcon = Icons.Default.Search,
                    onAction = { navController.navigate("item_feed_screen") }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(UniLostSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    items(filteredClaims) { claim ->
                        ClaimCard(
                            claim = claim,
                            onClick = { navController.navigate("claim_detail_screen/${claim.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClaimCard(claim: MockClaim, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type indicator
            Surface(
                shape = UniLostShapes.sm,
                color = if (claim.itemType == "LOST") ErrorBg else SuccessBg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (claim.itemType == "LOST") Icons.Default.SearchOff else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (claim.itemType == "LOST") ErrorRed else Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        claim.itemTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                    StatusChip(claim.status)
                }
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    claim.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Submitted ${MockItems.timeAgo(claim.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Handover progress for APPROVED claims
                if (claim.status == "APPROVED") {
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    val progressText = when {
                        claim.posterConfirmed && claim.claimantConfirmed -> "Both confirmed"
                        claim.posterConfirmed -> "Poster confirmed - Your turn"
                        claim.claimantConfirmed -> "You confirmed - Waiting for poster"
                        else -> "Awaiting confirmations"
                    }
                    Surface(
                        shape = UniLostShapes.md,
                        color = SuccessBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = UniLostSpacing.sm, vertical = UniLostSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Handshake,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Success
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                            Text(
                                progressText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Success,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
