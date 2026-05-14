package com.hulampay.mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.ChatDto
import com.hulampay.mobile.data.repository.ChatRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ChatDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ChatDto>>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = chatRepository.getMyChats()
            _state.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load chats")
            }
        }
    }
}
