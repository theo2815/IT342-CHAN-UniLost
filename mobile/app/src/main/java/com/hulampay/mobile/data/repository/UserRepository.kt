package com.hulampay.mobile.data.repository

import com.google.gson.JsonSyntaxException
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.api.UserApiService
import com.hulampay.mobile.data.model.ChangePasswordRequest
import com.hulampay.mobile.data.model.UpdateUserRequest
import com.hulampay.mobile.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    suspend fun updateProfile(userId: String, fullName: String): Result<User> = runCatching {
        val response = userApiService.updateUser(
            id = userId,
            request = UpdateUserRequest(fullName = fullName),
        )
        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string()
            throw IllegalStateException(parseErrorMessage(raw) ?: "Failed to update profile")
        }
        response.body() ?: throw IllegalStateException("Empty response from server")
    }

    suspend fun uploadProfilePicture(
        userId: String,
        fileName: String,
        body: RequestBody,
    ): Result<User> = runCatching {
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        val response = userApiService.uploadProfilePicture(id = userId, file = part)
        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string()
            throw IllegalStateException(parseErrorMessage(raw) ?: "Failed to upload photo")
        }
        response.body() ?: throw IllegalStateException("Empty response from server")
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
