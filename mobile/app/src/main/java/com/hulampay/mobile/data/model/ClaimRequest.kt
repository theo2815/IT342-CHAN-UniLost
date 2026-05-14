package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Body for POST /api/claims. `providedAnswer` is only required for FOUND items
 * (the secret-detail check); the backend ignores it for LOST items.
 */
data class ClaimRequest(
    @SerializedName("itemId")         val itemId: String,
    @SerializedName("providedAnswer") val providedAnswer: String? = null,
    @SerializedName("message")        val message: String,
)
