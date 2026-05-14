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
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo
import java.text.SimpleDateFormat
import java.util.Locale

private data class KarmaTier(
    val label: String,
    val nextLabel: String?,
    val nextAt: Int?,
)

private fun karmaTierOf(score: Int): KarmaTier = when {
    score < 10  -> KarmaTier("Newcomer",   "Helper",      10)
    score < 50  -> KarmaTier("Helper",     "Contributor", 50)
    score < 100 -> KarmaTier("Contributor", "Hero",       100)
    score < 200 -> KarmaTier("Hero",       "Legend",      200)
    else        -> KarmaTier("Legend",     null,          null)
}

private fun formatMemberSince(iso: String?): String {
    if (iso.isNullOrBlank()) return "Member"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(iso.substringBefore('.')) ?: return "Member"
        val out = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        "Member since ${out.format(date)}"
    } catch (e: Exception) {
        "Member"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val itemsState by viewModel.itemsState.collectAsState()
    val claimsState by viewModel.claimsState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Lost Items", "Found Items", "My Claims")

    val allItems: List<ItemDto> = (itemsState as? UiState.Success)?.data.orEmpty()
    val myLostItems  = allItems.filter { it.type == "LOST"  }
    val myFoundItems = allItems.filter { it.type == "FOUND" }
    val myClaims: List<ClaimDto> = (claimsState as? UiState.Success)?.data.orEmpty()

    val firstName = user?.firstName.orEmpty().ifBlank { user?.fullName.orEmpty() }
    val lastName  = user?.lastName.orEmpty()
    val email     = user?.email.orEmpty()
    val schoolShort = user?.campus?.shortLabel?.takeIf { it.isNotBlank() }
        ?: user?.universityTag.orEmpty()
    val schoolFull  = user?.campus?.name?.takeIf { it.isNotBlank() } ?: schoolShort
    val role        = user?.role ?: "STUDENT"
    val karmaScore  = user?.karmaScore ?: 0
    val tier        = karmaTierOf(karmaScore)
    val recoveredCount = myClaims.count { it.status == "COMPLETED" }

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
        },
        bottomBar = { BottomNavBar(navController = navController) }
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
                        if (user?.emailVerified == true) {
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
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    Text(
                        listOf(firstName, lastName).filter { it.isNotBlank() }
                            .joinToString(" ").ifBlank { "—" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        schoolFull.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                    Text(
                        formatMemberSince(user?.createdAt),
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
                                tier.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningHover,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

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
                            val nextAt = tier.nextAt
                            val progress = if (nextAt != null && nextAt > 0) {
                                (karmaScore.toFloat() / nextAt.toFloat()).coerceIn(0f, 1f)
                            } else {
                                1f
                            }
                            @Suppress("DEPRECATION")
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                            Text(
                                if (nextAt != null && tier.nextLabel != null) {
                                    "${nextAt - karmaScore} pts to ${tier.nextLabel}"
                                } else {
                                    "You've reached the top tier!"
                                },
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
                ProfileStatCard("Items\nPosted", allItems.size, Info, Modifier.weight(1f))
                ProfileStatCard("Claims\nMade", myClaims.size, Purple, Modifier.weight(1f))
                ProfileStatCard("Items\nRecovered", recoveredCount, Success, Modifier.weight(1f))
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
                0 -> ItemsList(
                    items = myLostItems,
                    state = itemsState,
                    emptyIcon = Icons.Default.SearchOff,
                    emptyTitle = "No lost items",
                    emptyMessage = "You haven't posted any lost items yet.",
                    onRetry = { viewModel.load() },
                    onItemClick = { id -> navController.navigate("item_detail_screen/$id") }
                )
                1 -> ItemsList(
                    items = myFoundItems,
                    state = itemsState,
                    emptyIcon = Icons.Default.Inventory2,
                    emptyTitle = "No found items",
                    emptyMessage = "You haven't posted any found items yet.",
                    onRetry = { viewModel.load() },
                    onItemClick = { id -> navController.navigate("item_detail_screen/$id") }
                )
                2 -> ClaimsList(
                    claims = myClaims,
                    state = claimsState,
                    onRetry = { viewModel.load() },
                    onClaimClick = { id -> navController.navigate("claim_detail_screen/$id") }
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Info Card
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
                        "Account Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    InfoRow(Icons.Outlined.Email, "Email", email.ifBlank { "—" })
                    if (schoolFull.isNotBlank() || schoolShort.isNotBlank()) {
                        val schoolLine = listOf(schoolShort, schoolFull)
                            .filter { it.isNotBlank() }
                            .distinct()
                            .joinToString(" — ")
                        InfoRow(Icons.Outlined.School, "School", schoolLine)
                    }
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
private fun ItemsList(
    items: List<ItemDto>,
    state: UiState<List<ItemDto>>,
    emptyIcon: ImageVector,
    emptyTitle: String,
    emptyMessage: String,
    onRetry: () -> Unit,
    onItemClick: (String) -> Unit,
) {
    when (state) {
        is UiState.Loading, UiState.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UniLostSpacing.lg),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        is UiState.Error -> {
            EmptyState(
                icon = Icons.Default.ErrorOutline,
                title = "Couldn't load items",
                message = state.message,
                actionLabel = "Retry",
                onAction = onRetry,
                modifier = Modifier.padding(UniLostSpacing.lg)
            )
        }
        is UiState.Success -> {
            if (items.isEmpty()) {
                EmptyState(
                    icon = emptyIcon,
                    title = emptyTitle,
                    message = emptyMessage,
                    modifier = Modifier.padding(UniLostSpacing.lg)
                )
            } else {
                Column(
                    modifier = Modifier.padding(UniLostSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    items.forEach { item ->
                        ProfileItemCard(
                            title = item.title,
                            subtitle = "${item.category} • ${item.location.orEmpty()}",
                            type = item.type,
                            status = item.status,
                            timeAgo = timeAgo(item.createdAt),
                            onClick = { onItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClaimsList(
    claims: List<ClaimDto>,
    state: UiState<List<ClaimDto>>,
    onRetry: () -> Unit,
    onClaimClick: (String) -> Unit,
) {
    when (state) {
        is UiState.Loading, UiState.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UniLostSpacing.lg),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        is UiState.Error -> {
            EmptyState(
                icon = Icons.Default.ErrorOutline,
                title = "Couldn't load claims",
                message = state.message,
                actionLabel = "Retry",
                onAction = onRetry,
                modifier = Modifier.padding(UniLostSpacing.lg)
            )
        }
        is UiState.Success -> {
            if (claims.isEmpty()) {
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
                    claims.forEach { claim ->
                        ProfileClaimCard(
                            itemTitle = claim.itemTitle,
                            itemType = claim.itemType,
                            status = claim.status,
                            posterName = claim.finderName,
                            timeAgo = timeAgo(claim.createdAt),
                            onClick = { onClaimClick(claim.id) }
                        )
                    }
                }
            }
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
