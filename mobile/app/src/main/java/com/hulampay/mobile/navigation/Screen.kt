package com.hulampay.mobile.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Main        : Screen("main_screen")
    object Detail      : Screen("detail_screen")
    object Login       : Screen("login_screen")
    object Register    : Screen("register_screen")
    object Dashboard   : Screen("dashboard_screen")
    object ItemFeed    : Screen("item_feed_screen")
    object ItemDetail  : Screen("item_detail_screen")
    object PostItem    : Screen("post_item_screen")
    object MyItems     : Screen("my_items_screen")
    object MyClaims    : Screen("my_claims_screen")
    object ClaimDetail : Screen("claim_detail_screen")
    object Notifications : Screen("notifications_screen")
    object Admin       : Screen("admin_screen")
    object Profile     : Screen("profile_screen")
    object Settings    : Screen("settings_screen")

    // ── Auth flow ────────────────────────────────────────────────────────────
    object ForgotPassword : Screen("forgot_password_screen")

    object VerifyOtp : Screen("verify_otp_screen/{email}") {
        /** Email may contain '@' and '.' — encode it so the router treats it as a single segment. */
        fun createRoute(email: String) = "verify_otp_screen/${Uri.encode(email)}"
    }

    /** Carries both email and resetToken so ResetPasswordScreen can call the API. */
    object ResetPassword : Screen("reset_password_screen/{email}/{resetToken}") {
        fun createRoute(email: String, resetToken: String) =
            "reset_password_screen/${Uri.encode(email)}/${Uri.encode(resetToken)}"
    }
}
