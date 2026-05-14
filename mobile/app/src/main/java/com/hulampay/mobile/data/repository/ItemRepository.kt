package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.api.ItemApiService
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.ItemRequest
import com.hulampay.mobile.data.model.PageDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val itemApiService: ItemApiService,
) {

    private val gson = AppGson.instance

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

    suspend fun getItemsByUser(
        userId: String,
        page: Int = 0,
        size: Int = 50,
    ): Result<PageDto<ItemDto>> {
        return try {
            val response = itemApiService.getItemsByUser(userId, page, size)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load your items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Serialize [request] as JSON for the multipart "item" part and submit alongside [images].
     * The caller is responsible for building the image parts (the field name must be "images").
     */
    suspend fun createItem(
        request: ItemRequest,
        images: List<MultipartBody.Part>,
    ): Result<ItemDto> {
        return try {
            val itemBody = gson.toJson(request).toRequestBody("application/json".toMediaType())
            val response = itemApiService.createItem(itemBody, images)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to post item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
