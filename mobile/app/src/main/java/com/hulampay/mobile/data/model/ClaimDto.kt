package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend ClaimDTO returned from the /api/claims endpoints.
 *
 * For FOUND items the `finderId` is the item poster; for LOST items the `finderId`
 * is the claimant themselves. The handover timestamps drive the state machine:
 * `finderMarkedReturnedAt` -> item PENDING_OWNER_CONFIRMATION,
 * `ownerConfirmedReceivedAt` -> claim COMPLETED, item RETURNED.
 */
data class ClaimDto(
    @SerializedName("id")             val id: String = "",
    @SerializedName("status")         val status: String = "",
    @SerializedName("providedAnswer") val providedAnswer: String? = null,
    @SerializedName("message")        val message: String = "",
    @SerializedName("createdAt")      val createdAt: String? = null,
    @SerializedName("updatedAt")      val updatedAt: String? = null,

    @SerializedName("itemId")         val itemId: String = "",
    @SerializedName("itemTitle")      val itemTitle: String = "",
    @SerializedName("itemType")       val itemType: String = "",
    @SerializedName("itemImageUrl")   val itemImageUrl: String? = null,

    @SerializedName("claimantId")     val claimantId: String = "",
    @SerializedName("claimantName")   val claimantName: String = "",
    @SerializedName("claimantSchool") val claimantSchool: String? = null,

    @SerializedName("finderId")       val finderId: String = "",
    @SerializedName("finderName")     val finderName: String = "",

    @SerializedName("secretDetailQuestion")    val secretDetailQuestion: String? = null,
    @SerializedName("chatId")                  val chatId: String? = null,
    @SerializedName("finderMarkedReturnedAt")  val finderMarkedReturnedAt: String? = null,
    @SerializedName("ownerConfirmedReceivedAt") val ownerConfirmedReceivedAt: String? = null,
)
