package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

private val GoldYellow   = Color(0xFFEAB308)
private val SilverGray   = Color(0xFF9CA3AF)
private val BronzeOrange = Color(0xFFEA580C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val campuses by viewModel.campuses.collectAsState()
    val selectedCampusId by viewModel.selectedCampusId.collectAsState()
    val scrollState = rememberScrollState()

    val selectedCampus = remember(campuses, selectedCampusId) {
        selectedCampusId?.let { id -> campuses.firstOrNull { it.id == id } }
    }
    val selectedCampusLabel = selectedCampus?.let {
        it.name?.takeIf { n -> n.isNotBlank() } ?: it.displayName
    }

    val entries: List<User> = (state as? UiState.Success)?.data.orEmpty()
    val topScore = entries.firstOrNull()?.karmaScore ?: 0
    val currentUserRank = currentUserId?.let { id ->
        val idx = entries.indexOfFirst { it.id == id }
        if (idx >= 0) idx + 1 else 0
    } ?: 0

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Leaderboard",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            HeroSection(
                campusLabel = selectedCampusLabel,
                campuses = campuses,
                selectedCampusId = selectedCampusId,
                onSelectCampus = viewModel::selectCampus,
            )

            StatsGrid(
                rankedUsers = entries.size,
                universities = campuses.size,
                topScore = topScore,
                currentUserRank = currentUserRank,
            )

            RankingsCard(
                state = state,
                entries = entries,
                currentUserId = currentUserId,
                currentUserRank = currentUserRank,
                selectedCampusLabel = selectedCampusLabel,
                onClearFilter = { viewModel.selectCampus(null) },
                onBrowse = { navController.navigate(Screen.ItemFeed.route) },
                onRetry = viewModel::load,
            )

            CtaBanner(onStartFinding = { navController.navigate(Screen.ItemFeed.route) })

            Spacer(modifier = Modifier.height(UniLostSpacing.xxl))
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Hero
// ────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroSection(
    campusLabel: String?,
    campuses: List<School>,
    selectedCampusId: String?,
    onSelectCampus: (String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md)
            .padding(top = UniLostSpacing.md, bottom = UniLostSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
    ) {
        Text(
            text = if (campusLabel != null) "$campusLabel Leaderboard"
                   else "Global Karma Leaderboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.headlineLarge.fontSize.times(1.1f),
        )
        Text(
            text = "Recognizing the top finders making a difference across Cebu City universities. Your honesty builds our community.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CampusFilter(
            campuses = campuses,
            selectedCampusId = selectedCampusId,
            onSelectCampus = onSelectCampus,
        )
    }
}

@Composable
private fun CampusFilter(
    campuses: List<School>,
    selectedCampusId: String?,
    onSelectCampus: (String?) -> Unit,
) {
    val selectedLabel = selectedCampusId?.let { id ->
        campuses.firstOrNull { it.id == id }
            ?.let { it.name?.takeIf { n -> n.isNotBlank() } ?: it.displayName }
    } ?: "All Universities"

    UniLostSelectField(
        selectedLabel = selectedLabel,
        leadingIcon = Icons.Default.School,
    ) { close ->
        UniLostDropdownItem(
            text = "All Universities",
            active = selectedCampusId == null,
            onClick = {
                onSelectCampus(null)
                close()
            },
        )
        campuses.forEach { campus ->
            val label = campus.name?.takeIf { it.isNotBlank() } ?: campus.displayName
            UniLostDropdownItem(
                text = label,
                active = selectedCampusId == campus.id,
                onClick = {
                    onSelectCampus(campus.id)
                    close()
                },
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Stats Grid (2×2)
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsGrid(
    rankedUsers: Int,
    universities: Int,
    topScore: Int,
    currentUserRank: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md)
            .padding(bottom = UniLostSpacing.md),
        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.SubdirectoryArrowLeft,
                iconBg = Success.copy(alpha = 0.1f),
                iconTint = Success,
                label = "Ranked Users",
                value = rankedUsers.toString(),
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.School,
                iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                label = "Universities",
                value = universities.toString(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Star,
                iconBg = Warning.copy(alpha = 0.1f),
                iconTint = Warning,
                label = "Top Score",
                value = topScore.toString(),
            )
            RankStatCard(
                modifier = Modifier.weight(1f),
                currentUserRank = currentUserRank,
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        shape = UniLostShapes.lg,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UniLostSpacing.md),
            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(UniLostShapes.xs)
                        .background(iconBg)
                        .padding(6.dp),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RankStatCard(
    modifier: Modifier = Modifier,
    currentUserRank: Int,
) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier,
        shape = UniLostShapes.lg,
        colors = CardDefaults.cardColors(
            containerColor = primary.copy(alpha = 0.05f),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box {
            // Glow blur — softly tinted gradient in the top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(80.dp)
                    .offset(x = 16.dp, y = (-16).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = 0.25f), Color.Transparent),
                        ),
                        shape = CircleShape,
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UniLostSpacing.md),
                verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(UniLostShapes.xs)
                            .background(primary)
                            .padding(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                    Text(
                        text = "Your Rank",
                        style = MaterialTheme.typography.labelMedium,
                        color = primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = if (currentUserRank > 0) "#$currentUserRank" else "—",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Rankings Card
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun RankingsCard(
    state: UiState<List<User>>,
    entries: List<User>,
    currentUserId: String?,
    currentUserRank: Int,
    selectedCampusLabel: String?,
    onClearFilter: () -> Unit,
    onBrowse: () -> Unit,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md),
        shape = UniLostShapes.lg,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        when (state) {
            is UiState.Loading, UiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = UniLostSpacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        Text(
                            "Loading leaderboard...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UniLostSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        "Couldn't load leaderboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }

            is UiState.Success -> {
                if (entries.isEmpty()) {
                    LeaderboardEmptyState(
                        selectedCampusLabel = selectedCampusLabel,
                        onClearFilter = onClearFilter,
                        onBrowse = onBrowse,
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TableHeader()
                        entries.forEachIndexed { index, user ->
                            LeaderboardRow(
                                rank = index + 1,
                                user = user,
                                isCurrentUser = user.id == currentUserId,
                            )
                            if (index < entries.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                )
                            }
                        }
                        if (currentUserId != null && currentUserRank > 0) {
                            val currentEntry = entries[currentUserRank - 1]
                            val pointsToNext = if (currentUserRank > 1)
                                entries[currentUserRank - 2].karmaScore - currentEntry.karmaScore
                            else null
                            CurrentUserStrip(
                                rank = currentUserRank,
                                user = currentEntry,
                                pointsToNext = pointsToNext,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
            .padding(horizontal = UniLostSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderText("RANK", Modifier.width(56.dp))
        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
        HeaderText("FINDER", Modifier.weight(1f))
        HeaderText("KARMA", Modifier.wrapContentWidth(), textAlign = TextAlign.End)
    }
}

@Composable
private fun HeaderText(text: String, modifier: Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        textAlign = textAlign,
        letterSpacing = 0.5.sp,
    )
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    user: User,
    isCurrentUser: Boolean,
) {
    val rowBg = if (isCurrentUser)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(horizontal = UniLostSpacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.CenterStart) {
            RankBadge(rank = rank)
        }
        Spacer(modifier = Modifier.width(UniLostSpacing.sm))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankedAvatar(user = user, rank = rank)
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val campusLabel = user.campus?.displayName.orEmpty()
                if (campusLabel.isNotBlank()) {
                    Text(
                        text = campusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        ScoreBadge(score = user.karmaScore, rank = rank)
    }
}

@Composable
private fun RankBadge(rank: Int) {
    when (rank) {
        1 -> Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GoldYellow.copy(alpha = 0.1f))
                .border(1.dp, GoldYellow.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "Rank 1",
                tint = GoldYellow,
                modifier = Modifier.size(16.dp),
            )
        }
        2 -> RankNumberBadge(rank = 2, color = SilverGray)
        3 -> RankNumberBadge(rank = 3, color = BronzeOrange)
        else -> Text(
            text = rank.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun RankNumberBadge(rank: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun RankedAvatar(user: User, rank: Int) {
    val borderColor = when (rank) {
        1 -> GoldYellow
        2 -> SilverGray
        3 -> BronzeOrange
        else -> Color.Transparent
    }
    Box {
        val avatarModifier = if (borderColor != Color.Transparent) {
            Modifier.border(2.dp, borderColor, CircleShape)
        } else Modifier
        AvatarView(
            name = user.fullName,
            size = 40.dp,
            imageUrl = user.profilePictureUrl,
            modifier = avatarModifier,
        )
        if (rank == 1) {
            // Small "1" badge at bottom-right of #1 avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .clip(UniLostShapes.full)
                    .background(GoldYellow)
                    .border(1.dp, MaterialTheme.colorScheme.surface, UniLostShapes.full)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text(
                    "1",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                )
            }
        }
    }
}

@Composable
private fun ScoreBadge(score: Int, rank: Int) {
    val (bg, fg) = when (rank) {
        1 -> GoldYellow.copy(alpha = 0.1f) to GoldYellow
        2 -> SilverGray.copy(alpha = 0.1f) to SilverGray
        3 -> BronzeOrange.copy(alpha = 0.1f) to BronzeOrange
        else -> Color.Transparent to MaterialTheme.colorScheme.onSurface
    }
    if (rank in 1..3) {
        Surface(
            shape = UniLostShapes.full,
            color = bg,
        ) {
            Text(
                text = "$score pts",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = fg,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    } else {
        Text(
            text = "$score pts",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}

@Composable
private fun CurrentUserStrip(
    rank: Int,
    user: User,
    pointsToNext: Int?,
) {
    val primary = MaterialTheme.colorScheme.primary
    Column {
        HorizontalDivider(color = primary.copy(alpha = 0.2f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primary.copy(alpha = 0.05f))
                .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            AvatarView(
                name = user.fullName,
                size = 40.dp,
                imageUrl = user.profilePictureUrl,
                modifier = Modifier.border(2.dp, primary, CircleShape),
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "You",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Surface(
                        shape = UniLostShapes.xs,
                        color = primary,
                    ) {
                        Text(
                            "CURRENT RANK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                val campusLabel = user.campus?.displayName.orEmpty()
                if (campusLabel.isNotBlank()) {
                    Text(
                        campusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (pointsToNext != null && pointsToNext > 0) {
                    Text(
                        "Next rank",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "+$pointsToNext pts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Success,
                    )
                }
                Text(
                    text = "${user.karmaScore} pts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardEmptyState(
    selectedCampusLabel: String?,
    onClearFilter: () -> Unit,
    onBrowse: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.lg, vertical = UniLostSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Warning.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
        Text(
            text = if (selectedCampusLabel != null)
                "No rankings yet for $selectedCampusLabel"
            else "No rankings yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (selectedCampusLabel != null)
                "No one from this university has earned Karma points yet. Be the first to return a found item and claim the top spot!"
            else "The leaderboard is waiting for its first hero. Return a found item to earn Karma points and get recognized.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
        Row(
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
        ) {
            Button(onClick = onBrowse) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                Text("Browse Found Items")
            }
            if (selectedCampusLabel != null) {
                OutlinedButton(onClick = onClearFilter) {
                    Text("View All")
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CTA Banner
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun CtaBanner(onStartFinding: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md)
            .padding(top = UniLostSpacing.md),
        shape = UniLostShapes.lg,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Decorative background icon (low opacity)
            Icon(
                Icons.Default.VolunteerActivism,
                contentDescription = null,
                tint = primary.copy(alpha = 0.06f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
                    .offset(x = 16.dp, y = 16.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UniLostSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
            ) {
                Text(
                    text = "Want to climb the ranks?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Every returned item earns you Karma Points. Help your community and get recognized on the Global Leaderboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Button(
                    onClick = onStartFinding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Start Finding", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
