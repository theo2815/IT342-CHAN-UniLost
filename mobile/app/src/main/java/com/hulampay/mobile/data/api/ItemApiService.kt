package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.PageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
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

    /** GET /api/items/map — list of items with coordinates for the campus map. */
    @GET("items/map")
    suspend fun getMapItems(
        @Query("campusId") campusId: String? = null,
        @Query("type")     type: String? = null,
        @Query("limit")    limit: Int? = null,
    ): Response<List<ItemDto>>

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

    /**
     * PUT /api/items/{id} — multipart, owner-only.
     * Part `item` carries the ItemRequest JSON (only non-null fields are applied).
     * Part `images` is optional — when empty the backend keeps the existing images.
     */
    @Multipart
    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Part("item") item: RequestBody,
        @Part images: List<MultipartBody.Part>,
    ): Response<ItemDto>

    /** DELETE /api/items/{id} — owner soft-delete, returns 204. */
    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<Unit>

    /**
     * POST /api/items/{id}/flag — file a report.
     * Body: { "reason": "SPAM|INAPPROPRIATE|FAKE|DUPLICATE", "description"?: "<=280 chars" }
     * Returns the updated ItemDTO with viewerHasFlagged populated.
     */
    @POST("items/{id}/flag")
    suspend fun flagItem(
        @Path("id") id: String,
        @Body body: Map<String, String>,
    ): Response<ItemDto>

    /**
     * POST /api/items/{id}/appeal — owner appeals an admin HIDE.
     * Body: { "text": "<=500 chars" }
     */
    @POST("items/{id}/appeal")
    suspend fun submitAppeal(
        @Path("id") id: String,
        @Body body: Map<String, String>,
    ): Response<ItemDto>
}
