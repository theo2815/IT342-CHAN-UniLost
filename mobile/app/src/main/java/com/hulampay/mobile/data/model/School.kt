package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches the backend CampusDTO from GET /api/campuses.
 */
data class School(
    @SerializedName("id")                val id: String = "",
    @SerializedName("universityCode")    val universityCode: String? = null,
    @SerializedName("campusName")        val campusName: String? = null,
    @SerializedName("name")              val name: String? = null,
    @SerializedName("shortLabel")        val shortLabel: String? = null,
    @SerializedName("address")           val address: String? = null,
    @SerializedName("domainWhitelist")   val domainWhitelist: String? = null,
    // Backend stores GeoJSON-style [lng, lat]. Nullable because some campuses lack a center.
    @SerializedName("centerCoordinates") val centerCoordinates: List<Double>? = null,
) {
    // Gson bypasses Kotlin constructor defaults via reflection, so any String
    // field returned as null by the backend lands as null in Kotlin even when
    // declared non-null. Mirror reality on the wire types and expose non-null
    // helpers for the common UI cases.
    val schoolId: String    get() = id
    val shortName: String   get() = shortLabel.orEmpty()
    val emailDomain: String get() = domainWhitelist.orEmpty()
    val displayName: String get() = shortLabel?.takeIf { it.isNotBlank() } ?: name.orEmpty()

    val centerLat: Double? get() = centerCoordinates?.getOrNull(1)
    val centerLng: Double? get() = centerCoordinates?.getOrNull(0)
}
