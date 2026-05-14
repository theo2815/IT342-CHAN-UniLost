package com.hulampay.mobile.data.state

import com.hulampay.mobile.data.repository.NotificationRepository
import com.hulampay.mobile.data.ws.ChatWebSocketClient
import com.hulampay.mobile.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-scoped holder for the authenticated user's unread-notification count.
 *
 * Top-level screens (Dashboard, Feed, Map) observe `unread` and render the
 * value as the bell-badge count in `UniLostTopBar`.
 *
 * `ensureStarted` is idempotent: the first call kicks off an initial REST
 * fetch and a long-lived STOMP subscription on `/user/queue/notifications`;
 * subsequent calls return immediately. On logout, call `reset()` so the
 * next session starts from zero.
 */
@Singleton
class UnreadCountState @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val webSocketClient: ChatWebSocketClient,
    @ApplicationScope private val appScope: CoroutineScope,
) {

    private val _unread = MutableStateFlow(0L)
    val unread: StateFlow<Long> = _unread

    private val started = AtomicBoolean(false)

    fun ensureStarted() {
        if (!started.compareAndSet(false, true)) return
        appScope.launch {
            refresh()
            runCatching {
                webSocketClient.subscribeToNotifications().collect {
                    // A new push arrived — re-fetch the authoritative count.
                    refresh()
                }
            }
            // Allow a future ensureStarted() to retry the subscription if
            // it died (e.g. token expired, socket closed).
            started.set(false)
        }
    }

    suspend fun refresh() {
        notificationRepository.getUnreadCount()
            .getOrNull()
            ?.let { _unread.value = it }
    }

    fun reset() {
        started.set(false)
        _unread.value = 0L
    }
}
