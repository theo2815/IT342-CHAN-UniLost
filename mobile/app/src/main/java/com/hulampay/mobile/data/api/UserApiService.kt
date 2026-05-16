package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ChangePasswordRequest
import com.hulampay.mobile.data.model.UpdateUserRequest
import com.hulampay.mobile.data.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    /** GET /api/users/leaderboard — top users by karma score. Optionally filtered by campus. */
    @GET("users/leaderboard")
    suspend fun getLeaderboard(
        @Query("size") size: Int = 20,
        @Query("campusId") campusId: String? = null,
    ): Response<List<User>>

    /** PUT /api/users/{id} — update editable profile fields (currently fullName). */
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest,
    ): Response<User>

    /** POST /api/users/{id}/profile-picture — multipart upload, field name is "file". */
    @Multipart
    @POST("users/{id}/profile-picture")
    suspend fun uploadProfilePicture(
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
    ): Response<User>

    /** PUT /api/users/{id}/change-password — verifies current password then sets the new one. */
    @PUT("users/{id}/change-password")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body request: ChangePasswordRequest,
    ): Response<Map<String, String>>
}
