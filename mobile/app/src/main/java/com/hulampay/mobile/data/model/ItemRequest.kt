package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend ItemRequest accepted by POST /api/items (multipart "item" part).
 *
 * Notes:
 * - `type` must be LOST or FOUND.
 * - `category` must be one of the backend enum names — see [ItemCategory].
 * - `dateLostFound` must be ISO-8601 LocalDateTime (e.g. "2026-05-13T00:00:00").
 * - If `campusId` is omitted the backend defaults to the reporter's universityTag.
 */
data class ItemRequest(
    @SerializedName("title")                val title: String,
    @SerializedName("description")          val description: String,
    @SerializedName("type")                 val type: String,
    @SerializedName("category")             val category: String,
    @SerializedName("location")             val location: String? = null,
    @SerializedName("latitude")             val latitude: Double? = null,
    @SerializedName("longitude")            val longitude: Double? = null,
    @SerializedName("secretDetailQuestion") val secretDetailQuestion: String? = null,
    @SerializedName("dateLostFound")        val dateLostFound: String? = null,
    @SerializedName("campusId")             val campusId: String? = null,
)

/**
 * Canonical backend category enum values. Display labels are matched by index.
 * Keep in sync with backend ItemRequest's @Pattern regex.
 */
object ItemCategory {
    val backendNames = listOf(
        "ELECTRONICS", "DOCUMENTS", "CLOTHING", "ACCESSORIES",
        "BOOKS", "BAGS", "KEYS", "WALLETS", "OTHER"
    )
    val displayLabels = listOf(
        "Electronics", "Documents", "Clothing", "Accessories",
        "Books", "Bags", "Keys", "Wallets", "Other"
    )

    fun displayToBackend(display: String): String? {
        val idx = displayLabels.indexOfFirst { it.equals(display, ignoreCase = true) }
        return if (idx >= 0) backendNames[idx] else null
    }
}
