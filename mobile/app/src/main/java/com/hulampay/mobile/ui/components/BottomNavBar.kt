package com.hulampay.mobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Feed : BottomNavItem("item_feed_screen", "Feed", Icons.Default.Search)
    object MyItems : BottomNavItem("my_items_screen", "My Items", Icons.Default.Inventory2)
    object Post : BottomNavItem("post_item_screen", "Post", Icons.Default.AddCircle)
    object Notifications : BottomNavItem("notifications_screen", "Alerts", Icons.Default.Notifications)
    object Profile : BottomNavItem("profile_screen", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.MyItems,
        BottomNavItem.Post,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a stack
                            popUpTo("item_feed_screen") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
