package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

/**
 * Leaderboard screen — Spec Section 10.12.
 * Reads top users via GET /api/users/leaderboard.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Leaderboard",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading, UiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(UniLostSpacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        Text(
                            "Couldn't load leaderboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Text(
                            s.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.md))
                        Button(onClick = { viewModel.load() }) { Text("Retry") }
                    }
                }
            }

            is UiState.Success -> {
                val entries = s.data
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(UniLostSpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                            Text(
                                "No rankings yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                            Text(
                                "Return a found item to earn karma and get on the leaderboard.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(UniLostSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        if (entries.size >= 3) {
                            item { PodiumSection(entries.take(3)) }
                        }

                        item {
                            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                            Text(
                                "Rankings",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val rest = if (entries.size >= 3) entries.drop(3) else entries
                        val baseRank = if (entries.size >= 3) 4 else 1
                        itemsIndexed(rest) { index, entry ->
                            LeaderboardRow(
                                rank = baseRank + index,
                                user = entry,
                                isCurrentUser = entry.id == currentUserId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(top3: List<User>) {
    if (top3.size < 3) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = UniLostShapes.lg,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UniLostSpacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumEntry(rank = 2, user = top3[1], podiumHeight = 80.dp, badgeColor = Slate400)
            PodiumEntry(rank = 1, user = top3[0], podiumHeight = 110.dp, badgeColor = Warning)
            PodiumEntry(rank = 3, user = top3[2], podiumHeight = 60.dp, badgeColor = Sage400)
        }
    }
}

@Composable
private fun PodiumEntry(
    rank: Int,
    user: User,
    podiumHeight: androidx.compose.ui.unit.Dp,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Surface(
            shape = UniLostShapes.full,
            color = badgeColor,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

        AvatarView(name = user.fullName, size = 56.dp, imageUrl = user.profilePictureUrl)

        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

        Text(
            user.firstName.ifBlank { user.fullName },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            "${user.karmaScore} pts",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(badgeColor.copy(alpha = 0.15f), badgeColor.copy(alpha = 0.05f))
                    ),
                    shape = UniLostShapes.sm
                )
        )
    }
}

@Composable
private fun LeaderboardRow(rank: Int, user: User, isCurrentUser: Boolean) {
    val highlightModifier = if (isCurrentUser) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, UniLostShapes.md)
    } else {
        Modifier
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(highlightModifier),
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
            Text(
                "#$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            AvatarView(name = user.fullName, size = 40.dp, imageUrl = user.profilePictureUrl)

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isCurrentUser) "${user.fullName} (You)" else user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val campusLabel = user.campus?.displayName.orEmpty()
                Text(
                    campusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "${user.karmaScore} karma",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
