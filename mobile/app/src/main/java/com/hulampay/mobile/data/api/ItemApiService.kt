package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.PageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    /** GET /api/items/user/{userId} — current user's items (paginated). */
    @GET("items/user/{userId}")
    suspend fun getItemsByUser(
        @Path("userId") userId: String,
        @Query("page")  page: Int = 0,
        @Query("size")  size: Int = 50,
    ): Response<PageDto<ItemDto>>

    /**
     * POST /api/items — multipart.
     * Part `item` carries the ItemRequest JSON (application/json).
     * Part `images` carries up to 3 image files (optional).
     */
    @Multipart
    @POST("items")
    suspend fun createItem(
        @Part("item") item: RequestBody,
        @Part images: List<MultipartBody.Part>,
    ): Response<ItemDto>
}
