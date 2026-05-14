package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApiService {

    /** GET /api/users/leaderboard — top users by karma score. Optionally filtered by campus. */
    @GET("users/leaderboard")
    suspend fun getLeaderboard(
        @Query("size") size: Int = 20,
        @Query("campusId") campusId: String? = null,
    ): Response<List<User>>
}
