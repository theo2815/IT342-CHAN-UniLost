package com.hulampay.mobile.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemsScreen(navController: NavController) {
    val tabs = listOf("All", "Active", "Claimed", "Recovered", "Expired")
    var selectedTab by remember { mutableStateOf(0) }

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
                title = { Text("My Items", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("post_item_screen") }) {
                        Icon(Icons.Default.Add, contentDescription = "Post Item")
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
            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard("Active", statCounts["Active"] ?: 0, Color(0xFF3b82f6), Modifier.weight(1f))
                StatMiniCard("Claimed", statCounts["Claimed"] ?: 0, Color(0xFFa855f7), Modifier.weight(1f))
                StatMiniCard("Recovered", statCounts["Recovered"] ?: 0, Sage, Modifier.weight(1f))
                StatMiniCard("Expired", statCounts["Expired"] ?: 0, Slate400, Modifier.weight(1f))
            }

            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = { Divider(color = Color.LightGray, thickness = 0.5.dp) }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab, fontSize = 13.sp) }
                    )
                }
            }

            // Items list
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Slate400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No items here", fontWeight = FontWeight.Medium, color = Slate800)
                        Text("Post an item to get started", fontSize = 14.sp, color = Slate400)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("post_item_screen") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate600)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Item")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemRow(item: com.hulampay.mobile.data.mock.MockItem, onClick: () -> Unit) {
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
                color = if (item.type == "LOST") ErrorRed.copy(alpha = 0.1f) else Sage.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (item.type == "LOST") Icons.Default.SearchOff else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (item.type == "LOST") ErrorRed else Sage,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(item.status, when (item.status) {
                        "ACTIVE" -> Color(0xFF3b82f6)
                        "CLAIMED" -> Color(0xFFa855f7)
                        "HANDED_OVER" -> Sage
                        else -> Slate400
                    })
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${item.category} \u2022 ${item.locationDescription}",
                    fontSize = 12.sp,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    MockItems.timeAgo(item.createdAt),
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            // Claim count badge
            if (item.claimCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Badge(containerColor = Color(0xFFa855f7)) {
                    Text("${item.claimCount}", fontSize = 11.sp)
                }
            }
        }
    }
}
