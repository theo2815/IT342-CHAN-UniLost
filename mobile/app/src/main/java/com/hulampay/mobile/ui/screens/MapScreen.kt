package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

/**
 * Map View screen — Spec Section 10.8.
 * Layout-only stub. Full Google Maps integration in a later phase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    badgeViewModel: NotificationBadgeViewModel = hiltViewModel(),
    chatBadgeViewModel: ChatBadgeViewModel = hiltViewModel(),
) {
    val filters = listOf("All", "Lost", "Found")
    var selectedFilter by remember { mutableStateOf("All") }
    val unreadNotifications by badgeViewModel.unread.collectAsState()
    val unreadChats by chatBadgeViewModel.unread.collectAsState()

    Scaffold(
        topBar = {
            UniLostTopBar(
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onChatClick = { navController.navigate(Screen.ChatList.route) },
                notificationCount = unreadNotifications.toInt(),
                chatCount = unreadChats.toInt()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Center on user location */ },
                shape = UniLostShapes.md,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "My Location"
                )
            }
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        "Campus Map",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Google Maps integration coming soon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Cebu City (10.3157\u00b0N, 123.8854\u00b0E)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Floating filter chips
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = UniLostSpacing.sm),
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
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = UniLostShapes.full,
                        elevation = FilterChipDefaults.filterChipElevation(4.dp)
                    )
                }
            }

            // Mock pin markers info
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(UniLostSpacing.md)
                    .fillMaxWidth(),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                    Text(
                        "Nearby Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.md)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = UniLostShapes.full,
                                color = ErrorRed,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                            Text(
                                "5 Lost",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = UniLostShapes.full,
                                color = Success,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                            Text(
                                "3 Found",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
