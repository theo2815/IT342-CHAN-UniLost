package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.AuthApiService
import com.hulampay.mobile.data.api.AuthResponse
import com.hulampay.mobile.utils.TokenManager
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.login(mapOf("email" to email, "password" to password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveToken(authResponse.token)
                Result.success(authResponse)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(registrationData: Map<String, Any>): Result<AuthResponse> {
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

    suspend fun logout() {
        tokenManager.clearToken()
    }

    suspend fun getSchools(): Result<List<com.hulampay.mobile.data.model.School>> {
        return try {
            val response = authApiService.getSchools()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch schools"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
