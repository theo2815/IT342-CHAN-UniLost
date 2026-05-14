package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.UserApiService
import com.hulampay.mobile.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
) {

    suspend fun getLeaderboard(size: Int = 20, campusId: String? = null): Result<List<User>> =
        runCatching {
            val response = userApiService.getLeaderboard(size, campusId?.takeIf { it.isNotBlank() })
            if (!response.isSuccessful) {
                val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "Failed to load leaderboard"
                throw IllegalStateException(message)
            }
            response.body() ?: emptyList()
        }
}
