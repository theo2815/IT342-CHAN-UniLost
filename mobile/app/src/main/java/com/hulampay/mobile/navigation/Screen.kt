package com.hulampay.mobile.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Detail : Screen("detail_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Dashboard : Screen("dashboard_screen")
    object ItemFeed : Screen("item_feed_screen")
    object ItemDetail : Screen("item_detail_screen")
    object PostItem : Screen("post_item_screen")
    object MyItems : Screen("my_items_screen")
    object MyClaims : Screen("my_claims_screen")
    object ClaimDetail : Screen("claim_detail_screen")
    object Notifications : Screen("notifications_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")
    object Landing : Screen("landing_screen")
    object ChatList : Screen("chat_list_screen")
    object ChatDetail : Screen("chat_detail_screen")
    object Map : Screen("map_screen")
    object Leaderboard : Screen("leaderboard_screen")

    object ForgotPassword : Screen("forgot_password_screen")

    object VerifyOtp : Screen("verify_otp_screen/{email}") {
        fun createRoute(email: String) = "verify_otp_screen/${Uri.encode(email)}"
    }

    object ResetPassword : Screen("reset_password_screen/{email}/{resetToken}") {
        fun createRoute(email: String, resetToken: String) =
            "reset_password_screen/${Uri.encode(email)}/${Uri.encode(resetToken)}"
    }
}
