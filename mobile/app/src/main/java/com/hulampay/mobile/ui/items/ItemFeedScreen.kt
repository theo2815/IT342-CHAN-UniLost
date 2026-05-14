package com.hulampay.mobile.ui.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo

private val ITEM_CATEGORIES = listOf(
    "ELECTRONICS",
    "WALLETS",
    "CLOTHING",
    "DOCUMENTS",
    "ACCESSORIES",
    "BOOKS",
    "KEYS",
    "BAGS",
    "OTHER",
)

private val CATEGORY_LABELS = mapOf(
    "ELECTRONICS" to "Electronics",
    "WALLETS" to "Wallets",
    "CLOTHING" to "Clothing",
    "DOCUMENTS" to "Documents",
    "ACCESSORIES" to "Accessories",
    "BOOKS" to "Books",
    "KEYS" to "Keys",
    "BAGS" to "Bags",
    "OTHER" to "Other",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ItemFeedScreen(
    navController: NavController,
    viewModel: ItemFeedViewModel = hiltViewModel(),
    badgeViewModel: NotificationBadgeViewModel = hiltViewModel(),
    chatBadgeViewModel: ChatBadgeViewModel = hiltViewModel(),
) {
    val itemsState by viewModel.itemsState.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeType by viewModel.activeType.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val activeCampusId by viewModel.activeCampusId.collectAsState()
    val campuses by viewModel.campuses.collectAsState()
    val unreadNotifications by badgeViewModel.unread.collectAsState()
    val unreadChats by chatBadgeViewModel.unread.collectAsState()

    val items = (itemsState as? UiState.Success)?.data.orEmpty()
    val hasMore = items.size < totalItems

    Scaffold(
        topBar = {
            UniLostTopBar(
                onLogoClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onChatClick = { navController.navigate(Screen.ChatList.route) },
                notificationCount = unreadNotifications.toInt(),
                chatCount = unreadChats.toInt()
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = UniLostSpacing.xxl)
        ) {
            item {
                PageHeader(
                    onPostItem = { navController.navigate(Screen.PostItem.route) }
                )
            }

            item {
                FilterSection(
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::setSearch,
                    activeType = activeType,
                    onTypeChange = viewModel::setType,
                    activeCategory = activeCategory,
                    onCategoryChange = viewModel::setCategory,
                    activeCampusId = activeCampusId,
                    onCampusChange = viewModel::setCampus,
                    campuses = campuses,
                )
            }

            when (val state = itemsState) {
                is UiState.Loading, UiState.Idle -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UniLostSpacing.xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        EmptyState(
                            icon = Icons.Default.CloudOff,
                            title = "Couldn't load items",
                            message = state.message,
                            actionLabel = "Retry",
                            onAction = { viewModel.loadItems() }
                        )
                    }
                }
                is UiState.Success -> {
                    if (items.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.SearchOff,
                                title = "No items found",
                                message = "Try adjusting your filters or search query, or be the first to post an item!",
                                actionLabel = "Post an Item",
                                onAction = { navController.navigate(Screen.PostItem.route) }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "Showing ${items.size} of $totalItems items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = UniLostSpacing.md,
                                    vertical = UniLostSpacing.sm
                                )
                            )
                        }

                        items(items, key = { it.id }) { item ->
                            ItemFeedCard(
                                item = item,
                                onClick = {
                                    navController.navigate("item_detail_screen/${item.id}")
                                },
                                modifier = Modifier.padding(
                                    horizontal = UniLostSpacing.md,
                                    vertical = UniLostSpacing.xs
                                )
                            )
                        }

                        if (hasMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = UniLostSpacing.md,
                                            vertical = UniLostSpacing.md
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.loadMore() },
                                        enabled = !isLoadingMore,
                                        shape = UniLostShapes.md,
                                    ) {
                                        if (isLoadingMore) {
                                            CircularProgressIndicator(
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(16.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(UniLostSpacing.sm))
                                            Text("Loading...")
                                        } else {
                                            Text("Load More")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageHeader(onPostItem: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = UniLostSpacing.md,
                end = UniLostSpacing.md,
                top = UniLostSpacing.md,
                bottom = UniLostSpacing.sm,
            )
    ) {
        Text(
            text = "Lost & Found Feed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(UniLostSpacing.xxs))
        Text(
            text = "Browse lost and found items across Cebu City campuses.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(UniLostSpacing.md))
        UniLostButton(
            text = "Post Item",
            onClick = onPostItem,
            icon = Icons.Default.Add,
            fillWidth = true,
        )
    }
}

@Composable
private fun FilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeType: String,
    onTypeChange: (String) -> Unit,
    activeCategory: String,
    onCategoryChange: (String) -> Unit,
    activeCampusId: String,
    onCampusChange: (String) -> Unit,
    campuses: List<School>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md),
        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
    ) {
        UniLostTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search items...",
            leadingIcon = Icons.Default.Search,
            height = 48.dp,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs)
        ) {
            TypeChip("All", activeType == "All", onClick = { onTypeChange("All") })
            TypeChip(
                "Lost",
                activeType == "Lost",
                activeColor = ErrorRed,
                onClick = { onTypeChange("Lost") }
            )
            TypeChip(
                "Found",
                activeType == "Found",
                activeColor = Success,
                onClick = { onTypeChange("Found") }
            )
        }

        CategoryDropdown(
            activeCategory = activeCategory,
            onChange = onCategoryChange,
        )

        CampusDropdown(
            activeCampusId = activeCampusId,
            campuses = campuses,
            onChange = onCampusChange,
        )
    }
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) activeColor else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) activeColor else MaterialTheme.colorScheme.outline
    Surface(
        modifier = Modifier
            .clip(UniLostShapes.full)
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, UniLostShapes.full),
        shape = UniLostShapes.full,
        color = containerColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryDropdown(
    activeCategory: String,
    onChange: (String) -> Unit,
) {
    val label = if (activeCategory.isNotBlank()) {
        CATEGORY_LABELS[activeCategory] ?: activeCategory
    } else "All Categories"

    UniLostSelectField(selectedLabel = label) { close ->
        UniLostDropdownItem(
            text = "All Categories",
            active = activeCategory.isBlank(),
            onClick = {
                onChange("")
                close()
            },
        )
        ITEM_CATEGORIES.forEach { code ->
            UniLostDropdownItem(
                text = CATEGORY_LABELS[code] ?: code,
                active = activeCategory == code,
                onClick = {
                    onChange(code)
                    close()
                },
            )
        }
    }
}

@Composable
private fun CampusDropdown(
    activeCampusId: String,
    campuses: List<School>,
    onChange: (String) -> Unit,
) {
    val activeCampus = campuses.find { it.id == activeCampusId }
    val label = activeCampus
        ?.let { it.name?.takeIf { n -> n.isNotBlank() } ?: it.displayName }
        ?: "All Schools"

    UniLostSelectField(
        selectedLabel = label,
        leadingIcon = Icons.Default.School,
    ) { close ->
        UniLostDropdownItem(
            text = "All Schools",
            active = activeCampusId.isBlank(),
            onClick = {
                onChange("")
                close()
            },
        )
        campuses.forEach { campus ->
            val campusLabel = campus.name?.takeIf { it.isNotBlank() } ?: campus.displayName
            UniLostDropdownItem(
                text = campusLabel,
                active = activeCampusId == campus.id,
                onClick = {
                    onChange(campus.id.orEmpty())
                    close()
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemFeedCard(
    item: ItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFound = item.type == "FOUND"
    val typeColor = if (isFound) Success else ErrorRed
    val images = item.imageUrls.ifEmpty { listOf("") }
    val pagerState = rememberPagerState(pageCount = { images.size })
    val schoolName = item.campus?.name?.takeIf { it.isNotBlank() }
        ?: item.campus?.displayName.orEmpty()
    val locationText = item.location.orEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.lg,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        // Image header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { pageIndex ->
                RemoteImage(
                    url = images[pageIndex].takeIf { it.isNotBlank() },
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    blurred = isFound,
                    placeholderIconSize = 40.dp,
                )
            }

            // Type badge top-left
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = UniLostShapes.full,
                color = typeColor.copy(alpha = 0.9f),
            ) {
                Text(
                    text = item.type,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            // "Image protected" label bottom-center for FOUND items
            if (isFound) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (images.size > 1) 20.dp else 8.dp),
                    shape = UniLostShapes.full,
                    color = Color.Black.copy(alpha = 0.6f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Image protected",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // Pager dots bottom-center
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(images.size) { index ->
                        val active = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (active) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(alpha = if (active) 1f else 0.5f)
                                )
                        )
                    }
                }
            }
        }

        // Body
        Column(
            modifier = Modifier.padding(UniLostSpacing.md),
            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
        ) {
            // Title + status
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (item.status.isNotBlank() && item.status != "ACTIVE") {
                    StatusChip(item.status)
                }
            }

            // Tags row
            Row(horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs)) {
                TagPill(
                    text = CATEGORY_LABELS[item.category] ?: item.category,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (schoolName.isNotBlank()) {
                    TagPill(
                        text = schoolName,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            // Meta
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (locationText.isNotBlank()) {
                    MetaRow(
                        icon = Icons.Default.LocationOn,
                        text = locationText,
                    )
                }
                MetaRow(
                    icon = Icons.Default.Schedule,
                    text = timeAgo(item.createdAt),
                )
            }
        }
    }
}

@Composable
private fun TagPill(text: String, color: Color) {
    Surface(
        shape = UniLostShapes.full,
        color = color.copy(alpha = 0.85f),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun MetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
