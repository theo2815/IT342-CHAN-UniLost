package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend FlagDetail. Exposed two ways on ItemDTO:
 *  - aggregated list `flagDetails` (admin/owner view, includes every reporter)
 *  - viewer-scoped `viewerFlagDetail` (only the current user's own report)
 */
data class FlagDetail(
    @SerializedName("reporterId")  val reporterId: String? = null,
    @SerializedName("reason")      val reason: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("createdAt")   val createdAt: String? = null,
)
