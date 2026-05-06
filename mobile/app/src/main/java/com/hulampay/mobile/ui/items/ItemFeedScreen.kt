package com.hulampay.mobile.ui.items

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFeedScreen(
    navController: NavController,
    viewModel: ItemFeedViewModel = hiltViewModel(),
) {
    val filters = listOf("All", "Lost", "Found")
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Electronics", "Documents", "Accessories", "Clothing", "Others")
    var selectedCategory by remember { mutableStateOf("All") }

    val itemsState by viewModel.itemsState.collectAsState()
    val allItems: List<ItemDto> = (itemsState as? UiState.Success)?.data.orEmpty()

    val filteredItems = remember(itemsState, selectedFilter, searchQuery, selectedCategory) {
        allItems.filter { item ->
            val matchesType = when (selectedFilter) {
                "Lost" -> item.type == "LOST"
                "Found" -> item.type == "FOUND"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true
            else item.title.contains(searchQuery, true) ||
                    item.description.contains(searchQuery, true) ||
                    (item.location?.contains(searchQuery, true) == true)
            val matchesCategory = if (selectedCategory == "All") true
            else item.category.equals(selectedCategory, true)
            matchesType && matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            UniLostTopBar(
                onNotificationsClick = { navController.navigate("notifications_screen") },
                notificationCount = 3
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Search Bar ──
            UniLostTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search items...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
                height = 48.dp
            )

            // ── Type Filter Chips ──
            LazyRow(
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
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
                        shape = UniLostShapes.full,
                        border = null
                    )
                }
            }

            // ── Category Filter ──
            LazyRow(
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                modifier = Modifier.padding(vertical = UniLostSpacing.xs)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                cat,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = UniLostShapes.full,
                        border = if (selectedCategory == cat) null
                            else FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = Color.Transparent
                            )
                    )
                }
            }

            // ── Item Count Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredItems.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Items Feed ──
            when (val state = itemsState) {
                is UiState.Loading, UiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.CloudOff,
                        title = "Couldn't load items",
                        message = state.message,
                        actionLabel = "Retry",
                        onAction = { viewModel.loadItems() }
                    )
                }
                is UiState.Success -> {
                    if (filteredItems.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "No items found",
                            message = "Try adjusting your filters or search term.",
                            actionLabel = "Clear Filters",
                            onAction = {
                                selectedFilter = "All"
                                selectedCategory = "All"
                                searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = UniLostSpacing.md,
                                end = UniLostSpacing.md,
                                bottom = UniLostSpacing.xxl
                            ),
                            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            items(filteredItems) { item ->
                                FeedItemCard(
                                    item = item,
                                    onClick = {
                                        navController.navigate("item_detail_screen/${item.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feed item card with image thumbnail.
 * FOUND items show a blurred placeholder to protect the owner.
 */
@Composable
private fun FeedItemCard(
    item: ItemDto,
    onClick: () -> Unit
) {
    val isFound = item.type == "FOUND"
    val accentColor = if (item.type == "LOST") ErrorRed else Sage400
    val posterName = item.reporter?.fullName.takeUnless { it.isNullOrBlank() } ?: "Unknown"
    val locationText = item.location.orEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Image thumbnail area
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder image (blurred for FOUND)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isFound) Modifier.blur(12.dp) else Modifier)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                // Type badge overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    shape = UniLostShapes.xs,
                    color = accentColor
                ) {
                    Text(
                        item.type,
                        color = White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Blur overlay text for FOUND
                if (isFound) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = UniLostShapes.xs
                    ) {
                        Text(
                            "Blurred",
                            style = MaterialTheme.typography.labelSmall,
                            color = White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(UniLostSpacing.sm)
            ) {
                // Status row — claim count badge omitted: backend ItemDTO does not expose it.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs)
                ) {
                    StatusChip(item.status)
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.xs))

                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))

                // Description
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.xs))

                // Meta row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Time ago
                    Text(
                        text = timeAgo(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.xs))

                // Posted by
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarView(
                        name = posterName,
                        size = 20.dp
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Text(
                        text = posterName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

