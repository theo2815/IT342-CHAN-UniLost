package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches the backend CampusDTO from GET /api/campuses.
 */
data class School(
    @SerializedName("id")              val id: String = "",
    @SerializedName("universityCode")  val universityCode: String = "",
    @SerializedName("campusName")      val campusName: String = "",
    @SerializedName("name")            val name: String = "",
    @SerializedName("shortLabel")      val shortLabel: String = "",
    @SerializedName("address")         val address: String = "",
    @SerializedName("domainWhitelist") val domainWhitelist: String = "",
) {
    // Backward-compat accessors
    val schoolId: String   get() = id
    val shortName: String  get() = shortLabel
    val emailDomain: String get() = domainWhitelist
    val displayName: String get() = if (shortLabel.isNotBlank()) shortLabel else name
}
