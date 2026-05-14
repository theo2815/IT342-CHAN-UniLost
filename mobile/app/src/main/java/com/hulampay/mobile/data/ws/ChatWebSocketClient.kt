package com.hulampay.mobile.data.ws

import com.google.gson.Gson
import com.hulampay.mobile.data.model.MessageDto
import com.hulampay.mobile.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * STOMP-over-WebSocket client for Phase 6 messaging.
 *
 * Connects to the backend's SockJS-registered endpoint via the raw WebSocket
 * transport (`/ws/websocket`). The JWT is passed in the STOMP CONNECT frame's
 * `Authorization` header — this is the contract enforced by
 * `WebSocketAuthInterceptor` on the server.
 *
 * A single shared session is reused across subscribers. Cancelling a collector
 * tears down only its STOMP subscription, not the socket.
 */
@Singleton
class ChatWebSocketClient @Inject constructor(
    private val tokenManager: TokenManager,
    okHttpClient: OkHttpClient,
) {

    private val gson = Gson()
    private val stompClient = StompClient(OkHttpWebSocketClient(okHttpClient))
    private val mutex = Mutex()
    private var session: StompSession? = null

    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    /** Cold flow of MessageDTOs broadcast to `/topic/chat/{chatId}`. */
    fun subscribeToChat(chatId: String): Flow<MessageDto> = flow {
        val s = ensureConnected()
        s.subscribeText("/topic/chat/$chatId").collect { body ->
            val msg = runCatching { gson.fromJson(body, MessageDto::class.java) }.getOrNull()
            if (msg != null) emit(msg)
        }
    }

    suspend fun disconnect() = mutex.withLock {
        val s = session
        session = null
        _connectionState.value = ConnectionState.IDLE
        if (s != null) {
            runCatching { s.disconnect() }
        }
    }

    private suspend fun ensureConnected(): StompSession = mutex.withLock {
        session?.let { return@withLock it }
        _connectionState.value = ConnectionState.CONNECTING
        val token = tokenManager.token.first()
            ?: run {
                _connectionState.value = ConnectionState.DISCONNECTED
                throw IllegalStateException("No auth token available for WebSocket")
            }
        val newSession = try {
            stompClient.connect(
                url = WS_URL,
                customStompConnectHeaders = mapOf("Authorization" to "Bearer $token"),
            )
        } catch (t: Throwable) {
            _connectionState.value = ConnectionState.DISCONNECTED
            throw t
        }
        session = newSession
        _connectionState.value = ConnectionState.CONNECTED
        newSession
    }

    enum class ConnectionState { IDLE, CONNECTING, CONNECTED, DISCONNECTED }

    companion object {
        // SockJS exposes a raw WebSocket transport at `${endpoint}/websocket`.
        // The backend registers `/ws` with `.withSockJS()` in WebSocketConfig.
        private const val WS_URL = "ws://10.0.2.2:8080/ws/websocket"
    }
}
