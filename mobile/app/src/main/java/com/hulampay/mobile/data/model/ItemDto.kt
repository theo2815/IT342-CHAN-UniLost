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
    @SerializedName("flagDetails")          val flagDetails: List<FlagDetail> = emptyList(),

    // Admin moderation action (HIDDEN | DELETED) — visible to owner + admin only.
    @SerializedName("adminActionType")      val adminActionType: String? = null,
    @SerializedName("adminActionReason")    val adminActionReason: String? = null,
    @SerializedName("adminActionAt")        val adminActionAt: String? = null,

    // Owner appeal lifecycle: NONE | PENDING | APPROVED | REJECTED.
    @SerializedName("appealStatus")         val appealStatus: String? = null,
    @SerializedName("appealText")           val appealText: String? = null,
    @SerializedName("appealedAt")           val appealedAt: String? = null,
    @SerializedName("appealResolvedAt")     val appealResolvedAt: String? = null,
    @SerializedName("appealAdminNote")      val appealAdminNote: String? = null,

    // Viewer-scoped report state — only populated by GET /api/items/{id}.
    @SerializedName("viewerHasFlagged")     val viewerHasFlagged: Boolean? = null,
    @SerializedName("viewerFlagDetail")     val viewerFlagDetail: FlagDetail? = null,

    // True only when the owner/admin fetches their own soft-deleted item.
    @SerializedName("isDeleted")            val isDeleted: Boolean? = null,
)
