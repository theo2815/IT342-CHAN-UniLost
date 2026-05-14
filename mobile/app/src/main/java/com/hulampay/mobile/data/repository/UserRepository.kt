package com.hulampay.mobile.data.repository

import com.google.gson.JsonSyntaxException
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.api.UserApiService
import com.hulampay.mobile.data.model.ChangePasswordRequest
import com.hulampay.mobile.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
) {

    private val gson = AppGson.instance

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

    suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String,
    ): Result<String> = runCatching {
        val response = userApiService.changePassword(
            id = userId,
            request = ChangePasswordRequest(currentPassword, newPassword),
        )
        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string()
            throw IllegalStateException(parseErrorMessage(raw) ?: "Failed to change password")
        }
        response.body()?.get("message") ?: "Password changed successfully"
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            @Suppress("UNCHECKED_CAST")
            val map = gson.fromJson(raw, Map::class.java) as? Map<String, Any?>
            (map?.get("error") as? String)
                ?: (map?.get("message") as? String)
                ?: raw
        } catch (_: JsonSyntaxException) {
            raw
        }
    }
}
