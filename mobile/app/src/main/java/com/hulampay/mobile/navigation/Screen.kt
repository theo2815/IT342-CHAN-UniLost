package com.hulampay.mobile.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Dashboard : Screen("dashboard_screen")
    object ItemFeed : Screen("item_feed_screen")
    object ItemDetail : Screen("item_detail_screen")
    object PostItem : Screen("post_item_screen?itemId={itemId}") {
        /**
         * Build a navigation route. Omit [itemId] for new posts; supply an id
         * to open the screen in edit mode (prefills the form, PUTs on submit).
         */
        fun createRoute(itemId: String? = null): String =
            if (itemId.isNullOrBlank()) "post_item_screen"
            else "post_item_screen?itemId=${Uri.encode(itemId)}"
    }
    object MyItems : Screen("my_items_screen")
    object MyClaims : Screen("my_claims_screen")
    object ClaimDetail : Screen("claim_detail_screen")
    object Notifications : Screen("notifications_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")
    object Landing : Screen("landing_screen")
    object ChatList : Screen("chat_list_screen")
    object ChatDetail : Screen("chat_detail_screen")
    object Map : Screen("map_screen?lat={lat}&lng={lng}&itemId={itemId}") {
        /**
         * Build a navigation route. Omit args for the default Cebu-center view.
         * Supply [lat]+[lng] (and optionally [itemId]) to auto-focus on a specific
         * coordinate when the map loads — mirrors the website's "View Location"
         * deep-link in `MapView.jsx:91-113`.
         */
        fun createRoute(
            lat: Double? = null,
            lng: Double? = null,
            itemId: String? = null,
        ): String {
            if (lat == null || lng == null) return "map_screen"
            val params = buildList {
                add("lat=$lat")
                add("lng=$lng")
                if (!itemId.isNullOrBlank()) add("itemId=${Uri.encode(itemId)}")
            }
            return "map_screen?" + params.joinToString("&")
        }
    }
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
