package com.hulampay.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val items by viewModel.items.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val recentItems = remember(items) { items.take(6) }
    val lostCount = remember(items) { items.count { it.type == "LOST" } }
    val foundCount = remember(items) { items.count { it.type == "FOUND" } }

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
                .verticalScroll(scrollState)
        ) {
            // ── Greeting Banner ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md)
                    .padding(top = UniLostSpacing.sm)
                    .clip(UniLostShapes.lg)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Slate700, Slate800)
                        )
                    )
                    .padding(UniLostSpacing.lg)
            ) {
                Column {
                    Text(
                        text = "Hi there! 👋",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        text = "Welcome to the campus lost & found network",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    // Quick stats in banner
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.md)
                    ) {
                        BannerStat(totalItems.toString(), "Total Items")
                        BannerStat(lostCount.toString(), "Lost")
                        BannerStat(foundCount.toString(), "Found")
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))

            // ── Quick Actions ──
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = UniLostSpacing.md),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                QuickActionCard(
                    icon = Icons.Default.SearchOff,
                    label = "Report Lost",
                    color = ErrorRed,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("post_item_screen") }
                )
                QuickActionCard(
                    icon = Icons.Default.Inventory2,
                    label = "Report Found",
                    color = Sage400,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("post_item_screen") }
                )
                QuickActionCard(
                    icon = Icons.Default.Map,
                    label = "Browse Map",
                    color = Info,
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: Map screen */ }
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))

            // ── Community Pulse ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Community Pulse",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { navController.navigate("item_feed_screen") }) {
                    Text(
                        "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            LazyRow(
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                items(recentItems) { item ->
                    // Compact card for horizontal scroll
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable {
                                navController.navigate("item_detail_screen/${item.id}")
                            },
                        shape = UniLostShapes.lg,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                            ) {
                                StatusChip(item.type)
                                StatusChip(item.status)
                            }

                            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(UniLostSpacing.xs))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(UniLostSpacing.xxs))
                                Text(
                                    text = item.location.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = timeAgo(item.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = UniLostSpacing.xs)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))

            // ── Stats Overview ──
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = UniLostSpacing.md),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                DashboardStatCard(
                    label = "Active",
                    value = "${items.count { it.status == "ACTIVE" }}",
                    color = Info,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    label = "Claimed",
                    value = "${items.count { it.status == "CLAIMED" || it.status == "PENDING_OWNER_CONFIRMATION" }}",
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    label = "Recovered",
                    value = "${items.count { it.status == "RETURNED" }}",
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))

            // ── Hot Zones ──
            Text(
                text = "Hot Zones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = UniLostSpacing.md),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
            Text(
                text = "Areas with the most activity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = UniLostSpacing.md)
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            LazyRow(
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                val hotZones = listOf(
                    Triple("CIT-U Library", 12, "2nd Floor"),
                    Triple("USC Bunzel", 8, "Room 301"),
                    Triple("UP Cebu Lab", 6, "Room 204"),
                    Triple("USJ-R Gate", 5, "Main Campus"),
                    Triple("SWU Parking", 4, "Area B"),
                    Triple("CNU Room 105", 3, "Main Building")
                )
                items(hotZones) { (name, count, detail) ->
                    Card(
                        modifier = Modifier.width(150.dp),
                        shape = UniLostShapes.md,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(UniLostSpacing.sm)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                                Text(
                                    name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                detail,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                            Surface(
                                shape = UniLostShapes.full,
                                color = ErrorRed.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    "$count reports",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = ErrorRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))

            // ── Top Contributors ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Contributors",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { navController.navigate(Screen.Leaderboard.route) }) {
                    Text(
                        "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                    val topUsers = listOf(
                        Triple("Patricia L.", "SWU", 85),
                        Triple("Carlo R.", "UP Cebu", 72),
                        Triple("Maria S.", "USC", 65),
                        Triple("Juan D.", "CIT-U", 42),
                        Triple("Ana G.", "USJ-R", 38)
                    )
                    topUsers.forEachIndexed { index, (name, school, karma) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = UniLostSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = when (index) {
                                    0 -> Warning
                                    1 -> Slate400
                                    2 -> Sage400
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.width(24.dp)
                            )
                            AvatarView(name = name, size = 32.dp)
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    school,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = UniLostShapes.full,
                                color = WarningBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Warning,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "$karma",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningHover
                                    )
                                }
                            }
                        }
                        if (index < topUsers.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.padding(start = 56.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.xxl))
        }
    }
}

@Composable
private fun BannerStat(value: String, label: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate300
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = UniLostSpacing.md, horizontal = UniLostSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DashboardStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = UniLostSpacing.sm, horizontal = UniLostSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}
