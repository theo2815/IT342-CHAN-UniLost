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
    val school: School?
)
