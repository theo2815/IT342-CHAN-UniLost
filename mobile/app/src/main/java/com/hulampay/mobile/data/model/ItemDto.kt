package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend ItemDTO returned by /api/items endpoints.
 * Note: claimCount is not exposed by the backend on this DTO.
 */
data class ItemDto(
    @SerializedName("id")                   val id: String = "",
    @SerializedName("title")                val title: String = "",
    @SerializedName("description")          val description: String = "",
    @SerializedName("type")                 val type: String = "",
    @SerializedName("status")               val status: String = "",
    @SerializedName("category")             val category: String = "",
    @SerializedName("location")             val location: String? = null,
    @SerializedName("latitude")             val latitude: Double? = null,
    @SerializedName("longitude")            val longitude: Double? = null,
    @SerializedName("imageUrls")            val imageUrls: List<String> = emptyList(),
    @SerializedName("secretDetailQuestion") val secretDetailQuestion: String? = null,
    @SerializedName("dateLostFound")        val dateLostFound: String? = null,
    @SerializedName("createdAt")            val createdAt: String? = null,
    @SerializedName("updatedAt")            val updatedAt: String? = null,
    @SerializedName("reporterId")           val reporterId: String? = null,
    @SerializedName("campusId")             val campusId: String? = null,
    @SerializedName("reporter")             val reporter: User? = null,
    @SerializedName("campus")               val campus: School? = null,
    @SerializedName("flagCount")            val flagCount: Int = 0,
    @SerializedName("flagReasons")          val flagReasons: List<String> = emptyList(),
)
