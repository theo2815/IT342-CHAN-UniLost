package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.CampusApiService
import com.hulampay.mobile.data.model.CampusStatDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampusRepository @Inject constructor(
    private val campusApiService: CampusApiService,
) {

    suspend fun getCampusStats(): Result<List<CampusStatDto>> = runCatching {
        val response = campusApiService.getCampusStats()
        if (!response.isSuccessful) {
            val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: "Failed to load campus stats"
            throw IllegalStateException(message)
        }
        response.body() ?: emptyList()
    }
}
