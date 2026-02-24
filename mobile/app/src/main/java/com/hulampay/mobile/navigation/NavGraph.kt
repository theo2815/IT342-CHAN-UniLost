package com.hulampay.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hulampay.mobile.ui.detail.DetailScreen
import com.hulampay.mobile.ui.home.MainScreen
import com.hulampay.mobile.ui.items.ItemFeedScreen
import com.hulampay.mobile.ui.items.ItemDetailScreen
import com.hulampay.mobile.ui.items.PostItemScreen
import com.hulampay.mobile.ui.items.MyItemsScreen
import com.hulampay.mobile.ui.items.MyClaimsScreen
import com.hulampay.mobile.ui.items.ClaimDetailScreen
import com.hulampay.mobile.ui.items.NotificationsScreen
import com.hulampay.mobile.ui.items.AdminScreen
import com.hulampay.mobile.ui.profile.ProfileScreen
import com.hulampay.mobile.ui.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController, startDestination = Screen.Login.route
    ) {

        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(
            "${Screen.Detail.route}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            DetailScreen(navController = navController, id = it.arguments?.getInt("id") ?: 0)
        }
        composable(Screen.Login.route) {
            com.hulampay.mobile.ui.auth.LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            com.hulampay.mobile.ui.auth.RegisterScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            com.hulampay.mobile.ui.dashboard.DashboardScreen(navController = navController)
        }

        // Phase A: Core Item Screens
        composable(Screen.ItemFeed.route) {
            ItemFeedScreen(navController = navController)
        }
        composable(
            "${Screen.ItemDetail.route}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            ItemDetailScreen(
                navController = navController,
                itemId = it.arguments?.getString("itemId") ?: ""
            )
        }
        composable(Screen.PostItem.route) {
            PostItemScreen(navController = navController)
        }
        composable(Screen.MyItems.route) {
            MyItemsScreen(navController = navController)
        }

        // Phase B: Claims & Handover Screens
        composable(Screen.MyClaims.route) {
            MyClaimsScreen(navController = navController)
        }
        composable(
            "${Screen.ClaimDetail.route}/{claimId}",
            arguments = listOf(navArgument("claimId") { type = NavType.StringType })
        ) {
            ClaimDetailScreen(
                navController = navController,
                claimId = it.arguments?.getString("claimId") ?: ""
            )
        }

        // Phase C: Notifications
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        // Phase D: Admin
        composable(Screen.Admin.route) {
            AdminScreen(navController = navController)
        }

        // Phase E: Profile & Settings
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
