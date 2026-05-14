package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend MessageDTO returned from /api/chats/{chatId}/messages and
 * broadcast on the /topic/chat/{chatId} STOMP destination.
 *
 * `type` values: TEXT, CLAIM_SUBMISSION, CLAIM_ACCEPTED, CLAIM_REJECTED,
 * HANDOVER_REQUEST, HANDOVER_CONFIRMED, HANDOVER_DISPUTED. Non-TEXT entries are
 * system messages with no senderId.
 */
data class MessageDto(
    @SerializedName("id")         val id: String = "",
    @SerializedName("chatId")     val chatId: String = "",
    @SerializedName("senderId")   val senderId: String? = null,
    @SerializedName("senderName") val senderName: String? = null,
    @SerializedName("content")    val content: String = "",
    @SerializedName("type")       val type: String = "TEXT",
    @SerializedName("metadata")   val metadata: Map<String, Any?>? = null,
    @SerializedName("read")       val read: Boolean = false,
    @SerializedName("createdAt")  val createdAt: String? = null,
)
