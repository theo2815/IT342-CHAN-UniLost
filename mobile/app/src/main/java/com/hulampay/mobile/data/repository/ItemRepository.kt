package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.ItemApiService
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.PageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val itemApiService: ItemApiService,
) {

    suspend fun getItems(
        keyword: String? = null,
        campusId: String? = null,
        category: String? = null,
        type: String? = null,
        status: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<PageDto<ItemDto>> {
        return try {
            val response = itemApiService.getItems(
                keyword = keyword?.takeIf { it.isNotBlank() },
                campusId = campusId?.takeIf { it.isNotBlank() },
                category = category?.takeIf { it.isNotBlank() },
                type = type?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() },
                page = page,
                size = size,
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(id: String): Result<ItemDto> {
        return try {
            val response = itemApiService.getItemById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 404) {
                Result.failure(NoSuchElementException("Item not found"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
