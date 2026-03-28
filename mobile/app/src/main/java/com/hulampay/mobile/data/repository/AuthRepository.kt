package com.hulampay.mobile.data.repository

import com.google.gson.Gson
import com.hulampay.mobile.data.api.AuthApiService
import com.hulampay.mobile.data.api.AuthResponse
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.utils.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
) {
    private val gson = Gson()

    // ── Login ─────────────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.login(mapOf("email" to email, "password" to password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveToken(authResponse.token)
                authResponse.user?.let { user ->
                    user.role.let { tokenManager.saveRole(it) }
                    tokenManager.saveUser(gson.toJson(user))
                }
                Result.success(authResponse)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────
    suspend fun register(registrationData: Map<String, String>): Result<AuthResponse> {
        return try {
            val response = authApiService.register(registrationData)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    suspend fun logout() {
        tokenManager.clearToken()
    }

    // ── Campuses ──────────────────────────────────────────────────────────────
    suspend fun getCampuses(): Result<List<School>> {
        return try {
            val response = authApiService.getCampuses()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch campuses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Backward-compat alias — kept so nothing else breaks. */
    suspend fun getSchools(): Result<List<School>> = getCampuses()

    // ── Current User ──────────────────────────────────────────────────────────
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authApiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenManager.saveUser(gson.toJson(user))
                Result.success(user)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Forgot Password ───────────────────────────────────────────────────────
    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = authApiService.forgotPassword(mapOf("email" to email))
            if (response.isSuccessful) {
                val message = response.body()?.get("message") ?: "Verification code sent to your email."
                Result.success(message)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to send reset email"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────
    suspend fun verifyOtp(email: String, otp: String): Result<String> {
        return try {
            val response = authApiService.verifyOtp(mapOf("email" to email, "otp" to otp))
            if (response.isSuccessful && response.body() != null) {
                val resetToken = response.body()?.get("resetToken") ?: ""
                Result.success(resetToken)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Invalid OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Reset Password ────────────────────────────────────────────────────────
    suspend fun resetPassword(email: String, resetToken: String, newPassword: String): Result<String> {
        return try {
            val response = authApiService.resetPassword(
                mapOf("email" to email, "resetToken" to resetToken, "newPassword" to newPassword)
            )
            if (response.isSuccessful) {
                val message = response.body()?.get("message") ?: "Password reset successfully."
                Result.success(message)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to reset password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
