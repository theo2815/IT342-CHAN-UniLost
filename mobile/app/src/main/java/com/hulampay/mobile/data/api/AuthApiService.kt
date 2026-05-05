package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<AuthResponse>

    /** GET /api/campuses — returns the list of supported university campuses. */
    @GET("campuses")
    suspend fun getCampuses(): Response<List<School>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<Map<String, String>>

    /** GET /api/auth/me — returns the currently authenticated user (requires Bearer token). */
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
}

data class AuthResponse(
    val token: String = "",
    val type: String = "Bearer",
    val user: User? = null,
)
