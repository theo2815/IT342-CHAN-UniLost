package com.hulampay.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hulampay.mobile.ui.detail.DetailScreen
import com.hulampay.mobile.ui.home.MainScreen

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
    }
}
