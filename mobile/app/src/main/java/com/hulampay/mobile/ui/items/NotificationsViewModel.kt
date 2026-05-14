package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.NotificationDto
import com.hulampay.mobile.data.repository.NotificationRepository
import com.hulampay.mobile.data.state.UnreadCountState
import com.hulampay.mobile.data.ws.ChatWebSocketClient
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val webSocketClient: ChatWebSocketClient,
    private val unreadCountState: UnreadCountState,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<NotificationDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<NotificationDto>>> = _state

    init {
        load()
        observeIncoming()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = notificationRepository.getNotifications(page = 0, size = 30)
            _state.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow().content)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load notifications")
            }
            unreadCountState.refresh()
        }
    }

    fun markAsRead(id: String) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        if (current.none { it.id == id && !it.read }) return
        _state.value = UiState.Success(
            current.map { if (it.id == id) it.copy(read = true) else it }
        )
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
            unreadCountState.refresh()
        }
    }

    fun markAllAsRead() {
        val current = (_state.value as? UiState.Success)?.data ?: return
        if (current.none { !it.read }) return
        _state.value = UiState.Success(current.map { it.copy(read = true) })
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            unreadCountState.refresh()
        }
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            runCatching {
                webSocketClient.subscribeToNotifications().collect { incoming ->
                    val current = (_state.value as? UiState.Success)?.data ?: emptyList()
                    if (current.any { it.id == incoming.id }) return@collect
                    _state.value = UiState.Success(listOf(incoming) + current)
                }
            }
        }
    }
}
