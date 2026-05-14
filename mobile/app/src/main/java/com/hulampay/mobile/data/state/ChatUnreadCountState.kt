package com.hulampay.mobile.data.state

import com.hulampay.mobile.data.repository.ChatRepository
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
 * App-scoped holder for the authenticated user's unread-chat count.
 *
 * Mirrors [UnreadCountState] but reads `GET /api/chats/unread-count`. Each
 * incoming chat message produces a `NEW_MESSAGE` notification on the user's
 * notification queue, so re-using that STOMP subscription as the refresh
 * trigger keeps both badges in sync without a second push channel.
 */
@Singleton
class ChatUnreadCountState @Inject constructor(
    private val chatRepository: ChatRepository,
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
                    refresh()
                }
            }
            started.set(false)
        }
    }

    suspend fun refresh() {
        chatRepository.getUnreadCount()
            .getOrNull()
            ?.let { _unread.value = it }
    }

    fun reset() {
        started.set(false)
        _unread.value = 0L
    }
}
