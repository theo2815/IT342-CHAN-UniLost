package com.hulampay.mobile.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockClaim
import com.hulampay.mobile.data.mock.MockClaims
import com.hulampay.mobile.data.mock.MockItems
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
            TopAppBar(
                title = { Text("My Claims", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate600,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            if (filteredClaims.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Slate400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No claims here", fontWeight = FontWeight.Medium, color = Slate800)
                        Text(
                            "Claims you submit on items will appear here",
                            fontSize = 14.sp,
                            color = Slate400
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("item_feed_screen") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate600)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browse Items")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
    val statusColor = when (claim.status) {
        "PENDING" -> Color(0xFFf59e0b)
        "APPROVED" -> Color(0xFF22c55e)
        "REJECTED" -> ErrorRed
        "HANDED_OVER" -> Sage
        else -> Slate400
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type indicator
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (claim.itemType == "LOST") ErrorRed.copy(alpha = 0.1f) else Sage.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (claim.itemType == "LOST") Icons.Default.SearchOff else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (claim.itemType == "LOST") ErrorRed else Sage,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        claim.itemTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(claim.status.replace("_", " "), statusColor)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    claim.message,
                    fontSize = 12.sp,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Submitted ${MockItems.timeAgo(claim.createdAt)}",
                    fontSize = 11.sp,
                    color = Slate400
                )

                // Handover progress for APPROVED claims
                if (claim.status == "APPROVED") {
                    Spacer(modifier = Modifier.height(4.dp))
                    val progressText = when {
                        claim.posterConfirmed && claim.claimantConfirmed -> "Both confirmed"
                        claim.posterConfirmed -> "Poster confirmed - Your turn"
                        claim.claimantConfirmed -> "You confirmed - Waiting for poster"
                        else -> "Awaiting confirmations"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Sage.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Handshake,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Sage
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(progressText, fontSize = 10.sp, color = Sage, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
