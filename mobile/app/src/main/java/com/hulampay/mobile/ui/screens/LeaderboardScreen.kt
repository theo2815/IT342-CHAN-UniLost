package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

/**
 * Leaderboard screen — Spec Section 10.12.
 * Layout-only stub with mock data.
 */

private data class MockLeaderboardEntry(
    val rank: Int,
    val name: String,
    val campus: String,
    val karma: Int
)

private val mockLeaderboard = listOf(
    MockLeaderboardEntry(1, "Maria Santos", "CIT-U", 156),
    MockLeaderboardEntry(2, "Carlos Reyes", "USC", 142),
    MockLeaderboardEntry(3, "Ana Garcia", "USJR", 128),
    MockLeaderboardEntry(4, "Juan Dela Cruz", "CIT-U", 95),
    MockLeaderboardEntry(5, "Sofia Lim", "UC", 88),
    MockLeaderboardEntry(6, "Miguel Torres", "SWU", 76),
    MockLeaderboardEntry(7, "Theo Chan", "CIT-U", 42),
    MockLeaderboardEntry(8, "Isabella Cruz", "USC", 38),
    MockLeaderboardEntry(9, "Rafael Aquino", "CTU", 31),
    MockLeaderboardEntry(10, "Carmen Villanueva", "CNU", 24),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Leaderboard",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(UniLostSpacing.md),
            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
        ) {
            // Top 3 Podium
            item {
                PodiumSection(mockLeaderboard.take(3))
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

            // 4th onward
            itemsIndexed(mockLeaderboard.drop(3)) { _, entry ->
                LeaderboardRow(entry)
            }
        }
    }
}

@Composable
private fun PodiumSection(top3: List<MockLeaderboardEntry>) {
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
            // 2nd place
            PodiumEntry(
                entry = top3[1],
                podiumHeight = 80.dp,
                badgeColor = Slate400
            )

            // 1st place
            PodiumEntry(
                entry = top3[0],
                podiumHeight = 110.dp,
                badgeColor = Warning
            )

            // 3rd place
            PodiumEntry(
                entry = top3[2],
                podiumHeight = 60.dp,
                badgeColor = Sage400
            )
        }
    }
}

@Composable
private fun PodiumEntry(
    entry: MockLeaderboardEntry,
    podiumHeight: androidx.compose.ui.unit.Dp,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        // Rank badge
        Surface(
            shape = UniLostShapes.full,
            color = badgeColor,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "#${entry.rank}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

        AvatarView(name = entry.name, size = 56.dp)

        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

        Text(
            entry.name.split(" ").first(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            "${entry.karma} pts",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

        // Podium bar
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
private fun LeaderboardRow(entry: MockLeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Rank
            Text(
                "#${entry.rank}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            AvatarView(name = entry.name, size = 40.dp)

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    entry.campus,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "${entry.karma} karma",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
