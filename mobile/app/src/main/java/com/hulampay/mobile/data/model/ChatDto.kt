package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend ChatDTO returned from /api/chats.
 *
 * `otherParticipantId`/`otherParticipantName` resolve to the conversation partner
 * for the authenticated user (so the chat list row can render without resolving
 * finder-vs-owner on the client).
 */
data class ChatDto(
    @SerializedName("id")                     val id: String = "",
    @SerializedName("itemId")                 val itemId: String = "",
    @SerializedName("itemTitle")              val itemTitle: String = "",
    @SerializedName("itemImageUrl")           val itemImageUrl: String? = null,
    @SerializedName("claimId")                val claimId: String? = null,

    @SerializedName("finderId")               val finderId: String = "",
    @SerializedName("finderName")             val finderName: String = "",
    @SerializedName("ownerId")                val ownerId: String = "",
    @SerializedName("ownerName")              val ownerName: String = "",

    @SerializedName("otherParticipantId")     val otherParticipantId: String = "",
    @SerializedName("otherParticipantName")   val otherParticipantName: String = "",

    @SerializedName("lastMessagePreview")     val lastMessagePreview: String? = null,
    @SerializedName("lastMessageAt")          val lastMessageAt: String? = null,
    @SerializedName("unreadCount")            val unreadCount: Long = 0,
    @SerializedName("createdAt")              val createdAt: String? = null,

    @SerializedName("itemStatus")             val itemStatus: String? = null,
    @SerializedName("itemType")               val itemType: String? = null,
    @SerializedName("claimStatus")            val claimStatus: String? = null,
)
