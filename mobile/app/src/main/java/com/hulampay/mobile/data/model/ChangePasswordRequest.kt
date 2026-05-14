package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Body for PUT /api/users/{id}/change-password.
 * Matches the backend ChangePasswordRequest DTO.
 */
data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword")     val newPassword: String,
)
