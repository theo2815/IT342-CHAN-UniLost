package com.hulampay.mobile.data.model

data class School(
    val schoolId: String,
    val name: String,
    val shortName: String = "",
    val city: String,
    val emailDomain: String
)
