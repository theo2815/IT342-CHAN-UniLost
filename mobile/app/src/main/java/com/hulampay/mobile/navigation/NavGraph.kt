package com.hulampay.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hulampay.mobile.ui.auth.ForgotPasswordScreen
import com.hulampay.mobile.ui.auth.VerifyOtpScreen
import com.hulampay.mobile.ui.auth.ResetPasswordScreen
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
import com.hulampay.mobile.ui.screens.LandingScreen
import com.hulampay.mobile.ui.screens.ChatListScreen
import com.hulampay.mobile.ui.screens.ChatDetailScreen
import com.hulampay.mobile.ui.screens.MapScreen
import com.hulampay.mobile.ui.screens.LeaderboardScreen

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
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(
            Screen.VerifyOtp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            VerifyOtpScreen(
                navController = navController,
                email = it.arguments?.getString("email") ?: ""
            )
        }
        composable(
            Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("email")      { type = NavType.StringType },
                navArgument("resetToken") { type = NavType.StringType },
            )
        ) {
            ResetPasswordScreen(
                navController = navController,
                email      = it.arguments?.getString("email")      ?: "",
                resetToken = it.arguments?.getString("resetToken") ?: "",
            )
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

        // Phase F: Guest & Social Screens
        composable(Screen.Landing.route) {
            LandingScreen(navController = navController)
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(navController = navController)
        }
        composable(
            "${Screen.ChatDetail.route}/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) {
            ChatDetailScreen(
                navController = navController,
                chatId = it.arguments?.getString("chatId") ?: ""
            )
        }
        composable(Screen.Map.route) {
            MapScreen(navController = navController)
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(navController = navController)
        }
    }
}
