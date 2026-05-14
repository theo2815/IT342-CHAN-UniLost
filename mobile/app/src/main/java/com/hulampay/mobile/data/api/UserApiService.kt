package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ChangePasswordRequest
import com.hulampay.mobile.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    /** GET /api/users/leaderboard — top users by karma score. Optionally filtered by campus. */
    @GET("users/leaderboard")
    suspend fun getLeaderboard(
        @Query("size") size: Int = 20,
        @Query("campusId") campusId: String? = null,
    ): Response<List<User>>

    /** PUT /api/users/{id}/change-password — verifies current password then sets the new one. */
    @PUT("users/{id}/change-password")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body request: ChangePasswordRequest,
    ): Response<Map<String, String>>
}
