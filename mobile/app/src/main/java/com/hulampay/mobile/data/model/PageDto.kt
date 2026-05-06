package com.hulampay.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Subset of Spring's Page<T> JSON shape — only the fields the mobile app needs.
 */
data class PageDto<T>(
    @SerializedName("content")          val content: List<T> = emptyList(),
    @SerializedName("number")           val number: Int = 0,
    @SerializedName("size")             val size: Int = 0,
    @SerializedName("totalElements")    val totalElements: Long = 0,
    @SerializedName("totalPages")       val totalPages: Int = 0,
    @SerializedName("first")            val first: Boolean = true,
    @SerializedName("last")             val last: Boolean = true,
    @SerializedName("numberOfElements") val numberOfElements: Int = 0,
    @SerializedName("empty")            val empty: Boolean = true,
)
