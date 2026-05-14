package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.NotificationApiService
import com.hulampay.mobile.data.model.NotificationDto
import com.hulampay.mobile.data.model.PageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService,
) {

    suspend fun getNotifications(page: Int = 0, size: Int = 30): Result<PageDto<NotificationDto>> =
        runCatching {
            notificationApiService.getNotifications(page, size)
                .bodyOrThrow("Failed to load notifications")
        }

    suspend fun getUnreadCount(): Result<Long> =
        runCatching {
            val map = notificationApiService.getUnreadCount()
                .bodyOrThrow("Failed to load unread count")
            map["count"] ?: 0L
        }

    suspend fun markAsRead(id: String): Result<NotificationDto> =
        runCatching {
            notificationApiService.markAsRead(id)
                .bodyOrThrow("Failed to mark notification as read")
        }

    suspend fun markAllAsRead(): Result<Unit> =
        runCatching {
            val response = notificationApiService.markAllAsRead()
            if (!response.isSuccessful) {
                val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "Failed to mark all as read"
                throw IllegalStateException(message)
            }
        }

    suspend fun deleteNotification(id: String): Result<Unit> =
        runCatching {
            val response = notificationApiService.deleteNotification(id)
            if (!response.isSuccessful) {
                val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "Failed to delete notification"
                throw IllegalStateException(message)
            }
        }
}

private fun <T> retrofit2.Response<T>.bodyOrThrow(fallbackMessage: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException(fallbackMessage)
    }
    val errorMessage = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: fallbackMessage
    throw IllegalStateException(errorMessage)
}
