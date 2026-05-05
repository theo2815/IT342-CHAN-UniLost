package com.hulampay.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hulampay.mobile.ui.theme.*

// ============================================================
// Bottom Navigation Items
// ============================================================

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false
) {
    object Feed : BottomNavItem("item_feed_screen", "Feed", Icons.Default.ViewList)
    object Map : BottomNavItem("map_screen", "Map", Icons.Default.Map)
    object Post : BottomNavItem("post_item_screen", "Post", Icons.Default.Add, isCenter = true)
    object Board : BottomNavItem("leaderboard_screen", "Board", Icons.Default.EmojiEvents)
    object Profile : BottomNavItem("profile_screen", "Me", Icons.Default.Person)
}

// Authenticated user: 5 tabs with center FAB
private val authenticatedItems = listOf(
    BottomNavItem.Feed,
    BottomNavItem.Map,
    BottomNavItem.Post,
    BottomNavItem.Board,
    BottomNavItem.Profile,
)

// Guest user: 3 tabs (no Post, no Profile)
private val guestItems = listOf(
    BottomNavItem.Feed,
    BottomNavItem.Map,
    BottomNavItem.Board,
)

/**
 * UniLost Bottom Navigation Bar matching spec Section 8.5.
 *
 * Features:
 * - 5-tab authenticated layout with center FAB-style Post button
 * - 3-tab guest layout (Feed, Map, Leaderboard)
 * - Active state: colorPrimary icon + label
 * - Inactive state: colorTextMuted
 * - Background: colorBgCard with top border
 *
 * @param isAuthenticated Whether the user is logged in
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    isAuthenticated: Boolean = true
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = if (isAuthenticated) authenticatedItems else guestItems

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            // Top border line
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = UniLostSpacing.xs),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    if (item.isCenter) {
                        // ── Center FAB-style Post button ──
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.onPrimary),
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo("item_feed_screen") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        // ── Regular tab item ──
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo("item_feed_screen") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
