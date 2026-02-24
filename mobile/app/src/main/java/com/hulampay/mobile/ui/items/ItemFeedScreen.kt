package com.hulampay.mobile.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockItem
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFeedScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var activeType by remember { mutableStateOf("All") }
    var activeCategory by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery, activeType, activeCategory) {
        MockItems.items.filter { item ->
            if (item.status != "ACTIVE") return@filter false
            if (activeType == "Lost" && item.type != "LOST") return@filter false
            if (activeType == "Found" && item.type != "FOUND") return@filter false
            if (activeCategory.isNotEmpty() && item.category != activeCategory) return@filter false
            if (searchQuery.isNotEmpty()) {
                val q = searchQuery.lowercase()
                return@filter item.title.lowercase().contains(q) ||
                        item.description.lowercase().contains(q) ||
                        item.locationDescription.lowercase().contains(q)
            }
            true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UniLost", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = { navController.navigate("profile_screen") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("post_item_screen") },
                containerColor = Slate600,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Item")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Slate600,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            // Type Filter Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Lost", "Found").forEach { type ->
                    FilterChip(
                        selected = activeType == type,
                        onClick = { activeType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (type) {
                                "Lost" -> ErrorRed.copy(alpha = 0.12f)
                                "Found" -> Sage.copy(alpha = 0.2f)
                                else -> Slate600.copy(alpha = 0.12f)
                            },
                            selectedLabelColor = when (type) {
                                "Lost" -> ErrorRed
                                "Found" -> Sage
                                else -> Slate600
                            }
                        )
                    )
                }

                // Category chips
                MockItems.categories.forEach { category ->
                    FilterChip(
                        selected = activeCategory == category,
                        onClick = {
                            activeCategory = if (activeCategory == category) "" else category
                        },
                        label = { Text(category, fontSize = 12.sp) }
                    )
                }
            }

            // Results count
            Text(
                text = "${filteredItems.size} items",
                fontSize = 12.sp,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Item List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Slate400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No items found", fontWeight = FontWeight.Medium, color = Slate800)
                        Text("Try adjusting your filters", fontSize = 14.sp, color = Slate400)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        ItemCard(
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
fun ItemCard(item: MockItem, onClick: () -> Unit) {
    val isFound = item.type == "FOUND"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Slate100)
            ) {
                // Placeholder for image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isFound) Modifier.blur(16.dp) else Modifier)
                        .background(Slate100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Slate400
                    )
                }

                // Type badge
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(20.dp),
                    color = if (item.type == "LOST") ErrorRed else Sage
                ) {
                    Text(
                        text = item.type,
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (isFound) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Image protected",
                            color = White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Slate800
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Slate600.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            color = Slate600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Sage.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.schoolShortName,
                            fontSize = 11.sp,
                            color = Sage,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Location & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.locationDescription,
                        fontSize = 12.sp,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = MockItems.timeAgo(item.createdAt),
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            }
        }
    }
}
