package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.ChatApiService
import com.hulampay.mobile.data.model.ChatDto
import com.hulampay.mobile.data.model.MessageDto
import com.hulampay.mobile.data.model.PageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
) {

    suspend fun getMyChats(): Result<List<ChatDto>> =
        runCatching {
            val response = chatApiService.getMyChats()
            response.bodyOrThrow("Failed to load chats")
        }

    suspend fun getChatById(chatId: String): Result<ChatDto> =
        runCatching {
            val response = chatApiService.getChatById(chatId)
            if (response.code() == 404) throw NoSuchElementException("Chat not found")
            response.bodyOrThrow("Failed to load chat")
        }

    suspend fun getMessages(
        chatId: String,
        page: Int = 0,
        size: Int = 50,
    ): Result<PageDto<MessageDto>> =
        runCatching {
            val response = chatApiService.getMessages(chatId, page, size)
            response.bodyOrThrow("Failed to load messages")
        }

    suspend fun sendMessage(chatId: String, content: String): Result<MessageDto> =
        runCatching {
            val response = chatApiService.sendMessage(chatId, mapOf("content" to content))
            response.bodyOrThrow("Failed to send message")
        }

    suspend fun markAsRead(chatId: String): Result<Unit> =
        runCatching {
            val response = chatApiService.markAsRead(chatId)
            if (!response.isSuccessful) {
                val errorMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "Failed to mark chat as read"
                throw IllegalStateException(errorMessage)
            }
        }

    suspend fun getUnreadCount(): Result<Long> =
        runCatching {
            val response = chatApiService.getUnreadCount()
            val body = response.bodyOrThrow("Failed to load unread count")
            body["unreadCount"] ?: 0L
        }
}

private fun <T> retrofit2.Response<T>.bodyOrThrow(fallbackMessage: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException(fallbackMessage)
    }
    val errorMessage = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: fallbackMessage
    throw IllegalStateException(errorMessage)
}
