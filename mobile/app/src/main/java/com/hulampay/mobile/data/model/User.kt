package com.hulampay.mobile.data.model

data class User(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val address: String,
    val phoneNumber: String,
    val profilePicture: String?,
    val studentIdNumber: String,
    val schoolId: String?,
    val school: School?,
    val role: String = "STUDENT",
    val karmaScore: Int = 0,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false
)
