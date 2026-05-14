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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun MyClaimsScreen(
    navController: NavController,
    viewModel: MyClaimsViewModel = hiltViewModel(),
) {
    val filters = listOf("All", "Pending", "Approved", "Rejected", "Handed Over")
    var selectedFilter by remember { mutableStateOf("All") }
    val state by viewModel.state.collectAsState()

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

            when (val current = state) {
                UiState.Idle, UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is UiState.Error -> EmptyState(
                    icon = Icons.Default.ErrorOutline,
                    title = "Couldn't load claims",
                    message = current.message,
                    actionLabel = "Try again",
                    actionIcon = Icons.Default.Refresh,
                    onAction = { viewModel.load() }
                )
                is UiState.Success -> {
                    val filteredClaims = filterClaims(current.data, selectedFilter)
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
    }
}

private fun filterClaims(claims: List<ClaimDto>, selectedFilter: String): List<ClaimDto> {
    return when (selectedFilter) {
        "Pending" -> claims.filter { it.status.equals("PENDING", ignoreCase = true) }
        "Approved" -> claims.filter { it.status.equals("ACCEPTED", ignoreCase = true) }
        "Rejected" -> claims.filter { it.status.equals("REJECTED", ignoreCase = true) }
        "Handed Over" -> claims.filter {
            it.status.equals("COMPLETED", ignoreCase = true) ||
                it.status.equals("HANDED_OVER", ignoreCase = true)
        }
        else -> claims
    }
}

@Composable
fun ClaimCard(claim: ClaimDto, onClick: () -> Unit) {
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
                    "Submitted ${timeAgo(claim.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Handover progress for ACCEPTED claims (not yet completed)
                val isAccepted = claim.status.equals("ACCEPTED", ignoreCase = true)
                if (isAccepted) {
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    val finderConfirmed = claim.finderMarkedReturnedAt != null
                    val ownerConfirmed = claim.ownerConfirmedReceivedAt != null
                    val progressText = when {
                        finderConfirmed && ownerConfirmed -> "Both confirmed"
                        finderConfirmed -> "Poster confirmed - Your turn"
                        ownerConfirmed -> "You confirmed - Waiting for poster"
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
