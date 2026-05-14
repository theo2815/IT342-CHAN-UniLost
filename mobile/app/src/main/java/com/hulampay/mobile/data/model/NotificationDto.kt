package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend NotificationDTO from /api/notifications.
 *
 * Backend's `boolean isRead` Lombok field serializes to JSON key `"read"` —
 * not `"isRead"` — because of Jackson's bean-property naming.
 */
data class NotificationDto(
    @SerializedName("id")        val id: String = "",
    @SerializedName("type")      val type: String = "",
    @SerializedName("title")     val title: String = "",
    @SerializedName("message")   val message: String = "",
    @SerializedName("linkId")    val linkId: String? = null,
    @SerializedName("read")      val read: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
)
