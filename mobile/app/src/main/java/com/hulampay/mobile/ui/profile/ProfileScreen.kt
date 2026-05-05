package com.hulampay.mobile.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockClaims
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Mock user data
    val firstName = "Theo"
    val lastName = "Chan"
    val email = "theo.chan@cit.edu"
    val studentId = "2023-12345"
    val school = "Cebu Institute of Technology - University"
    val schoolShort = "CIT-U"
    val role = "STUDENT"
    val karmaScore = 42
    val karmaRank = "Contributor"
    val nextRankAt = 50
    val nextRank = "Helper"
    val isAdmin = true

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Lost Items", "Found Items", "My Claims")

    // Mock data for tabs
    val myLostItems = remember { MockItems.items.filter { it.type == "LOST" && it.postedByName == "Juan D." } }
    val myFoundItems = remember { MockItems.items.filter { it.type == "FOUND" && it.postedByName == "Juan D." } }
    val myClaims = remember { MockClaims.getMyOutgoingClaims("u1") }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Profile",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UniLostSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with verified badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AvatarView(
                            firstName = firstName,
                            lastName = lastName,
                            size = 96.dp
                        )
                        Surface(
                            shape = UniLostShapes.full,
                            color = Success,
                            modifier = Modifier
                                .size(28.dp)
                                .offset(x = 2.dp, y = 2.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    Text(
                        "$firstName $lastName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        school,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                    Text(
                        "Member since 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    // Role badge
                    StatusChip(role)
                }
            }

            // Karma Progress
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                            Text(
                                "Karma Score",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = UniLostShapes.full,
                            color = WarningBg
                        ) {
                            Text(
                                karmaRank,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningHover,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    // Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$karmaScore",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            @Suppress("DEPRECATION")
                            LinearProgressIndicator(
                                progress = karmaScore.toFloat() / nextRankAt.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                            Text(
                                "${nextRankAt - karmaScore} pts to $nextRank",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                ProfileStatCard("Items\nPosted", 5, Info, Modifier.weight(1f))
                ProfileStatCard("Claims\nMade", 3, Purple, Modifier.weight(1f))
                ProfileStatCard("Items\nRecovered", 2, Success, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Tabs: Lost Items / Found Items / My Claims
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> {
                    // Lost Items
                    if (myLostItems.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "No lost items",
                            message = "You haven't posted any lost items yet.",
                            modifier = Modifier.padding(UniLostSpacing.lg)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(UniLostSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            myLostItems.forEach { item ->
                                ProfileItemCard(
                                    title = item.title,
                                    subtitle = "${item.category} • ${item.locationDescription}",
                                    type = item.type,
                                    status = item.status,
                                    timeAgo = MockItems.timeAgo(item.createdAt),
                                    onClick = { navController.navigate("item_detail_screen/${item.id}") }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Found Items
                    if (myFoundItems.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Inventory2,
                            title = "No found items",
                            message = "You haven't posted any found items yet.",
                            modifier = Modifier.padding(UniLostSpacing.lg)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(UniLostSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            myFoundItems.forEach { item ->
                                ProfileItemCard(
                                    title = item.title,
                                    subtitle = "${item.category} • ${item.locationDescription}",
                                    type = item.type,
                                    status = item.status,
                                    timeAgo = MockItems.timeAgo(item.createdAt),
                                    onClick = { navController.navigate("item_detail_screen/${item.id}") }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // My Claims
                    if (myClaims.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Assignment,
                            title = "No claims",
                            message = "You haven't made any claims yet.",
                            modifier = Modifier.padding(UniLostSpacing.lg)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(UniLostSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            myClaims.forEach { claim ->
                                ProfileClaimCard(
                                    itemTitle = claim.itemTitle,
                                    itemType = claim.itemType,
                                    status = claim.status,
                                    posterName = claim.posterName,
                                    timeAgo = MockItems.timeAgo(claim.createdAt),
                                    onClick = { navController.navigate("claim_detail_screen/${claim.id}") }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Student Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.lg)) {
                    Text(
                        "Student Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    InfoRow(Icons.Outlined.Email, "Email", email)
                    InfoRow(Icons.Outlined.Badge, "Student ID", studentId)
                    InfoRow(Icons.Outlined.School, "School", "$schoolShort — $school")
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Action Buttons
            Column(
                modifier = Modifier.padding(horizontal = UniLostSpacing.md),
                verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                UniLostButton(
                    text = "Edit Profile",
                    onClick = { navController.navigate(Screen.Settings.route) },
                    variant = ButtonVariant.SECONDARY,
                    icon = Icons.Default.Edit
                )

                if (isAdmin) {
                    UniLostButton(
                        text = "Admin Panel",
                        onClick = { navController.navigate(Screen.Admin.route) },
                        variant = ButtonVariant.SECONDARY,
                        icon = Icons.Default.Shield
                    )
                }

                UniLostButton(
                    text = "Logout",
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    variant = ButtonVariant.DANGER,
                    icon = Icons.Default.Logout
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))
        }
    }
}

@Composable
private fun ProfileStatCard(label: String, value: Int, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = UniLostSpacing.md, horizontal = UniLostSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileItemCard(
    title: String,
    subtitle: String,
    type: String,
    status: String,
    timeAgo: String,
    onClick: () -> Unit
) {
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
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = UniLostShapes.sm,
                color = if (type == "LOST") ErrorBg else SuccessBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (type == "LOST") Icons.Default.SearchOff else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (type == "LOST") ErrorRed else Success,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(status)
        }
    }
}

@Composable
private fun ProfileClaimCard(
    itemTitle: String,
    itemType: String,
    status: String,
    posterName: String,
    timeAgo: String,
    onClick: () -> Unit
) {
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
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = UniLostShapes.sm,
                color = PurpleBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    itemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Posted by $posterName • $itemType",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(status)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = UniLostSpacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
