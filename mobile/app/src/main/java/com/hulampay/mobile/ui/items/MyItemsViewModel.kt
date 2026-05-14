package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _state = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ItemDto>>> = _state

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val user = readCachedUser()
            if (user == null || user.id.isBlank()) {
                _state.value = UiState.Error("You're not signed in.")
                return@launch
            }
            val result = itemRepository.getItemsByUser(user.id, page = 0, size = 50)
            _state.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow().content)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load your items")
            }
        }
    }

    private suspend fun readCachedUser(): User? {
        val json = tokenManager.userJson.first() ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
