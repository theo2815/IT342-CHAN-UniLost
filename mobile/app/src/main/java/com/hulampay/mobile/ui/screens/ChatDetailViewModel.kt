package com.hulampay.mobile.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.ChatDto
import com.hulampay.mobile.data.model.MessageDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.ChatRepository
import com.hulampay.mobile.data.repository.ClaimRepository
import com.hulampay.mobile.data.state.ChatUnreadCountState
import com.hulampay.mobile.data.ws.ChatWebSocketClient
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * State for ChatDetailScreen.
 *
 * `messages` is held in chronological-ascending order (oldest first). The
 * screen reverses for display so the newest message lands at the bottom.
 */
data class ChatDetailData(
    val chat: ChatDto,
    val messages: List<MessageDto>,
    val currentUserId: String?,
    val hasMoreOlder: Boolean,
) {
    /**
     * Actual physical holder of the item — they call mark-returned.
     * Backend chat.finderId is the item reporter; for LOST items the *holder* is
     * the claimant, so swap. Matches ClaimService.markItemReturned's check on
     * `actualHolderId`.
     */
    val isFinder: Boolean
        get() {
            val uid = currentUserId ?: return false
            return when (chat.itemType?.uppercase()) {
                "LOST" -> uid == chat.ownerId
                else -> uid == chat.finderId
            }
        }

    /**
     * Actual owner of the item — they call confirm-received / dispute.
     * For LOST items the *owner* is the item poster (= chat.finderId).
     */
    val isOwner: Boolean
        get() {
            val uid = currentUserId ?: return false
            return when (chat.itemType?.uppercase()) {
                "LOST" -> uid == chat.finderId
                else -> uid == chat.ownerId
            }
        }
}

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val claimRepository: ClaimRepository,
    private val webSocketClient: ChatWebSocketClient,
    private val tokenManager: TokenManager,
    private val chatUnreadCountState: ChatUnreadCountState,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _state = MutableStateFlow<UiState<ChatDetailData>>(UiState.Idle)
    val state: StateFlow<UiState<ChatDetailData>> = _state

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending

    private val _loadingOlder = MutableStateFlow(false)
    val loadingOlder: StateFlow<Boolean> = _loadingOlder

    private val _actionInFlight = MutableStateFlow(false)
    val actionInFlight: StateFlow<Boolean> = _actionInFlight

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError

    val wsConnectionState: StateFlow<ChatWebSocketClient.ConnectionState> =
        webSocketClient.connectionState

    private var subscriptionJob: Job? = null
    private var markReadJob: Job? = null
    private var chatId: String = ""
    private var currentPage: Int = 0

    fun load(chatId: String) {
        if (chatId.isBlank()) {
            _state.value = UiState.Error("Missing chat id")
            return
        }
        if (this.chatId == chatId && _state.value is UiState.Success<*>) return
        this.chatId = chatId
        _state.value = UiState.Loading
        viewModelScope.launch {
            val cachedUser = readCachedUser()
            val chatResult = chatRepository.getChatById(chatId)
            if (chatResult.isFailure) {
                _state.value = UiState.Error(
                    chatResult.exceptionOrNull()?.message ?: "Failed to load chat"
                )
                return@launch
            }
            val messagesResult = chatRepository.getMessages(chatId, page = 0, size = INITIAL_PAGE_SIZE)
            if (messagesResult.isFailure) {
                _state.value = UiState.Error(
                    messagesResult.exceptionOrNull()?.message ?: "Failed to load messages"
                )
                return@launch
            }
            val page = messagesResult.getOrThrow()
            // Backend returns DESC — flip to ASC so newest lands at the end.
            val messages = page.content.reversed()
            currentPage = 0
            _state.value = UiState.Success(
                ChatDetailData(
                    chat = chatResult.getOrThrow(),
                    messages = messages,
                    currentUserId = cachedUser?.id,
                    hasMoreOlder = !page.last,
                )
            )
            startSubscription(chatId)
            scheduleMarkRead()
        }
    }

    fun loadOlderMessages() {
        val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return
        if (!current.hasMoreOlder) return
        if (_loadingOlder.value) return
        val activeChatId = chatId.takeIf { it.isNotBlank() } ?: return
        _loadingOlder.value = true
        val nextPage = currentPage + 1
        viewModelScope.launch {
            val result = chatRepository.getMessages(activeChatId, page = nextPage, size = INITIAL_PAGE_SIZE)
            _loadingOlder.value = false
            if (result.isFailure) {
                _actionError.value = result.exceptionOrNull()?.message ?: "Failed to load older messages"
                return@launch
            }
            val page = result.getOrThrow()
            // DESC → ASC, then prepend so chronological order is preserved.
            val older = page.content.reversed()
            val snapshot = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return@launch
            if (snapshot.chat.id != activeChatId) return@launch
            currentPage = nextPage
            _state.value = UiState.Success(
                snapshot.copy(
                    messages = older + snapshot.messages,
                    hasMoreOlder = !page.last,
                )
            )
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return
        if (_sending.value) return
        _sending.value = true

        val tempId = "temp-" + UUID.randomUUID().toString()
        val optimistic = MessageDto(
            id = tempId,
            chatId = current.chat.id,
            senderId = current.currentUserId,
            senderName = null,
            content = trimmed,
            type = "TEXT",
            read = false,
            createdAt = null,
        )
        updateMessages { it + optimistic }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(current.chat.id, trimmed)
            _sending.value = false
            if (result.isSuccess) {
                val server = result.getOrThrow()
                updateMessages { list ->
                    list.map { if (it.id == tempId) server else it }
                }
            } else {
                updateMessages { list -> list.filterNot { it.id == tempId } }
                _actionError.value = result.exceptionOrNull()?.message ?: "Failed to send message"
            }
        }
    }

    fun markReturned() = runClaimAction { id -> claimRepository.markReturned(id) }
    fun confirmReceived() = runClaimAction { id -> claimRepository.confirmReceived(id) }
    fun disputeHandover() = runClaimAction { id -> claimRepository.disputeHandover(id) }
    fun acceptClaim() = runClaimAction { id -> claimRepository.acceptClaim(id) }
    fun rejectClaim() = runClaimAction { id -> claimRepository.rejectClaim(id) }

    fun consumeActionError() {
        _actionError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        subscriptionJob?.cancel()
        markReadJob?.cancel()
    }

    private fun startSubscription(chatId: String) {
        subscriptionJob?.cancel()
        subscriptionJob = viewModelScope.launch {
            try {
                webSocketClient.subscribeToChat(chatId).collect { incoming ->
                    onIncomingFrame(incoming)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Chat WS subscription error: ${t.message}")
            }
        }
    }

    private fun onIncomingFrame(message: MessageDto) {
        val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return
        val isOwnText = message.type == "TEXT" &&
            !current.currentUserId.isNullOrBlank() &&
            message.senderId == current.currentUserId
        if (isOwnText) return // already added optimistically; ignore the echo
        if (current.messages.any { it.id == message.id }) return // dedupe
        updateMessages { it + message }
        // System messages (handover-related) may have changed claim/item status;
        // refresh the chat header so the pinned card stays accurate.
        if (message.type != "TEXT") {
            viewModelScope.launch { refreshChatHeader() }
        }
        scheduleMarkRead()
    }

    private suspend fun refreshChatHeader() {
        val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return
        val result = chatRepository.getChatById(current.chat.id)
        if (result.isSuccess) {
            _state.value = UiState.Success(current.copy(chat = result.getOrThrow()))
        }
    }

    private fun scheduleMarkRead() {
        markReadJob?.cancel()
        markReadJob = viewModelScope.launch {
            delay(MARK_READ_DEBOUNCE_MS)
            val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return@launch
            if (current.chat.unreadCount <= 0) return@launch
            val result = chatRepository.markAsRead(current.chat.id)
            if (result.isSuccess) {
                _state.value = UiState.Success(
                    current.copy(chat = current.chat.copy(unreadCount = 0))
                )
                chatUnreadCountState.refresh()
            }
        }
    }

    private fun runClaimAction(block: suspend (String) -> Result<*>) {
        val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return
        val claimId = current.chat.claimId ?: run {
            _actionError.value = "This chat is not linked to a claim"
            return
        }
        if (_actionInFlight.value) return
        _actionInFlight.value = true
        viewModelScope.launch {
            val result = block(claimId)
            _actionInFlight.value = false
            if (result.isFailure) {
                _actionError.value = result.exceptionOrNull()?.message ?: "Action failed"
                return@launch
            }
            refreshChatHeader()
        }
    }

    private fun updateMessages(transform: (List<MessageDto>) -> List<MessageDto>) {
        val current = (_state.value as? UiState.Success<ChatDetailData>)?.data ?: return
        _state.value = UiState.Success(current.copy(messages = transform(current.messages)))
    }

    private suspend fun readCachedUser(): User? {
        val json = tokenManager.userJson.first() ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val TAG = "ChatDetailVM"
        const val INITIAL_PAGE_SIZE = 50
        const val MARK_READ_DEBOUNCE_MS = 800L
    }
}
