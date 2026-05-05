package com.hulampay.mobile.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemsScreen(navController: NavController) {
    val tabs = listOf("All", "Active", "Claimed", "Recovered", "Expired")
    var selectedTab by remember { mutableIntStateOf(0) }

    // Mock: filter items by "Juan D." as the current user
    val myItems = remember {
        MockItems.items.filter { it.postedByName == "Juan D." }
    }

    val filteredItems = remember(selectedTab, myItems) {
        when (selectedTab) {
            0 -> myItems
            1 -> myItems.filter { it.status == "ACTIVE" }
            2 -> myItems.filter { it.status == "CLAIMED" }
            3 -> myItems.filter { it.status == "HANDED_OVER" }
            4 -> myItems.filter { it.status == "EXPIRED" }
            else -> myItems
        }
    }

    val statCounts = remember(myItems) {
        mapOf(
            "Active" to myItems.count { it.status == "ACTIVE" },
            "Claimed" to myItems.count { it.status == "CLAIMED" },
            "Recovered" to myItems.count { it.status == "HANDED_OVER" },
            "Expired" to myItems.count { it.status == "EXPIRED" }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("post_item_screen") }) {
                        Icon(Icons.Default.Add, contentDescription = "Post Item")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                StatMiniCard("Active", statCounts["Active"] ?: 0, Info, Modifier.weight(1f))
                StatMiniCard("Claimed", statCounts["Claimed"] ?: 0, Purple, Modifier.weight(1f))
                StatMiniCard("Recovered", statCounts["Recovered"] ?: 0, Success, Modifier.weight(1f))
                StatMiniCard("Expired", statCounts["Expired"] ?: 0, Slate400, Modifier.weight(1f))
            }

            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = UniLostSpacing.md,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                tab,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Items list
            if (filteredItems.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inbox,
                    title = "No items here",
                    message = "Post an item to get started",
                    actionLabel = "Post Item",
                    actionIcon = Icons.Default.Add,
                    onAction = { navController.navigate("post_item_screen") }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(UniLostSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    items(filteredItems) { item ->
                        MyItemRow(
                            item = item,
                            onClick = { navController.navigate("item_detail_screen/${item.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMiniCard(label: String, value: Int, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = UniLostShapes.sm,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = UniLostSpacing.sm, horizontal = UniLostSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun MyItemRow(item: com.hulampay.mobile.data.mock.MockItem, onClick: () -> Unit) {
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
                color = if (item.type == "LOST") ErrorBg else SuccessBg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (item.type == "LOST") Icons.Default.SearchOff else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (item.type == "LOST") ErrorRed else Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                    StatusChip(item.status)
                }
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    "${item.category} \u2022 ${item.locationDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    MockItems.timeAgo(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Claim count badge
            if (item.claimCount > 0) {
                Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                Badge(containerColor = Purple) {
                    Text(
                        "${item.claimCount}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
