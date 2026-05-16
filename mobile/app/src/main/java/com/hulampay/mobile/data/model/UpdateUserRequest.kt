package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Body for PUT /api/users/{id}.
 * Matches the backend UpdateUserRequest DTO — currently only the display name is editable.
 */
data class UpdateUserRequest(
    @SerializedName("fullName") val fullName: String,
)
