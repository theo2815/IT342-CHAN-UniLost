package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ChatDto
import com.hulampay.mobile.data.model.MessageDto
import com.hulampay.mobile.data.model.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {

    /** GET /api/chats — all chat rooms for the authenticated user (no paging). */
    @GET("chats")
    suspend fun getMyChats(): Response<List<ChatDto>>

    /** GET /api/chats/{chatId} */
    @GET("chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: String): Response<ChatDto>

    /** GET /api/chats/{chatId}/messages — paged DESC by createdAt. */
    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<PageDto<MessageDto>>

    /** POST /api/chats/{chatId}/messages — body { content }. */
    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body body: Map<String, String>,
    ): Response<MessageDto>

    /** PUT /api/chats/{chatId}/read — mark all messages read. */
    @PUT("chats/{chatId}/read")
    suspend fun markAsRead(@Path("chatId") chatId: String): Response<Unit>

    /** GET /api/chats/unread-count — { unreadCount: Long }. */
    @GET("chats/unread-count")
    suspend fun getUnreadCount(): Response<Map<String, Long>>
}
