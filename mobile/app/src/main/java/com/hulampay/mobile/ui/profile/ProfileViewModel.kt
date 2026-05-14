package com.hulampay.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.data.repository.ClaimRepository
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.data.state.ChatUnreadCountState
import com.hulampay.mobile.data.state.UnreadCountState
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository,
    private val claimRepository: ClaimRepository,
    private val tokenManager: TokenManager,
    private val unreadCountState: UnreadCountState,
    private val chatUnreadCountState: ChatUnreadCountState,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _itemsState = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Idle)
    val itemsState: StateFlow<UiState<List<ItemDto>>> = _itemsState

    private val _claimsState = MutableStateFlow<UiState<List<ClaimDto>>>(UiState.Idle)
    val claimsState: StateFlow<UiState<List<ClaimDto>>> = _claimsState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            // Fast initial render from cache.
            tokenManager.userJson.first()?.let { json ->
                runCatching { gson.fromJson(json, User::class.java) }
                    .getOrNull()
                    ?.let { _currentUser.value = it }
            }

            // Refresh user from /auth/me; this also re-caches the JSON.
            val userResult = authRepository.getCurrentUser()
            userResult.getOrNull()?.let { _currentUser.value = it }

            val userId = _currentUser.value?.id
            if (userId.isNullOrBlank()) {
                _itemsState.value = UiState.Error("You're not signed in.")
                _claimsState.value = UiState.Error("You're not signed in.")
                return@launch
            }

            _itemsState.value = UiState.Loading
            _claimsState.value = UiState.Loading

            val itemsDeferred = async { itemRepository.getItemsByUser(userId, page = 0, size = 50) }
            val claimsDeferred = async { claimRepository.getMyClaims(page = 0, size = 50) }

            val itemsResult = itemsDeferred.await()
            _itemsState.value = if (itemsResult.isSuccess) {
                UiState.Success(itemsResult.getOrThrow().content)
            } else {
                UiState.Error(itemsResult.exceptionOrNull()?.message ?: "Failed to load your items")
            }

            val claimsResult = claimsDeferred.await()
            _claimsState.value = if (claimsResult.isSuccess) {
                UiState.Success(claimsResult.getOrThrow().content)
            } else {
                UiState.Error(claimsResult.exceptionOrNull()?.message ?: "Failed to load your claims")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            unreadCountState.reset()
            chatUnreadCountState.reset()
        }
    }
}
