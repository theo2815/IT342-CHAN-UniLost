package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches the per-campus map returned by GET /api/campuses/stats.
 * Backend builds these as `Map<String, Object>` so optional fields may be absent.
 */
data class CampusStatDto(
    @SerializedName("id")          val id: String = "",
    @SerializedName("name")        val name: String? = null,
    @SerializedName("shortLabel")  val shortLabel: String? = null,
    @SerializedName("activeItems") val activeItems: Long = 0L,
)
