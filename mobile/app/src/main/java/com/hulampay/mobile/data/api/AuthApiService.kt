package com.hulampay.mobile.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.hulampay.mobile.data.model.School

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: Map<String, Any>): Response<AuthResponse>

    @GET("schools")
    suspend fun getSchools(): Response<List<School>>
}

data class AuthResponse(
    val token: String,
    val type: String,
    val user: com.hulampay.mobile.data.model.User?
)
