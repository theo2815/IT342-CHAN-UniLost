package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.PageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemApiService {

    /** GET /api/items — paginated, optionally filtered. */
    @GET("items")
    suspend fun getItems(
        @Query("keyword")  keyword: String? = null,
        @Query("campusId") campusId: String? = null,
        @Query("category") category: String? = null,
        @Query("type")     type: String? = null,
        @Query("status")   status: String? = null,
        @Query("page")     page: Int = 0,
        @Query("size")     size: Int = 20,
    ): Response<PageDto<ItemDto>>

    /** GET /api/items/{id} */
    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<ItemDto>
}
