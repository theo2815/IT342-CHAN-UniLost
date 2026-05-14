package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches the backend UserDTO returned from /api/auth/login and /api/auth/me.
 */
data class User(
    @SerializedName("id")               val id: String = "",
    @SerializedName("fullName")         val fullName: String = "",
    @SerializedName("email")            val email: String = "",
    @SerializedName("role")             val role: String = "STUDENT",
    @SerializedName("karmaScore")       val karmaScore: Int = 0,
    @SerializedName("accountStatus")    val accountStatus: String = "ACTIVE",
    @SerializedName("universityTag")    val universityTag: String? = null,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String? = null,
    @SerializedName("emailVerified")    val emailVerified: Boolean = false,
    @SerializedName("createdAt")        val createdAt: String? = null,
    @SerializedName("lastLogin")        val lastLogin: String? = null,
    @SerializedName("campus")           val campus: School? = null,
) {
    /** First word of fullName — used in greeting "Hi, {firstName}!" */
    val firstName: String
        get() = fullName.split(" ").firstOrNull()?.trim() ?: ""

    val lastName: String
        get() = fullName.split(" ").drop(1).joinToString(" ").trim()

    val isAdmin: Boolean get() = role.equals("ADMIN", ignoreCase = true)
}
