package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.NotificationDto
import com.hulampay.mobile.data.model.PageDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    /** GET /api/notifications — paginated DESC by createdAt. */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
    ): Response<PageDto<NotificationDto>>

    /** GET /api/notifications/unread/count -> { "count": Long } */
    @GET("notifications/unread/count")
    suspend fun getUnreadCount(): Response<Map<String, Long>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<NotificationDto>

    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>
}
