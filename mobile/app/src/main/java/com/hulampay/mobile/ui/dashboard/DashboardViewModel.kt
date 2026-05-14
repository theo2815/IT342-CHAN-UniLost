package com.hulampay.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hulampay.mobile.data.model.CampusStatDto
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.data.repository.CampusRepository
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.data.repository.UserRepository
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val campusRepository: CampusRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = Gson()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    private val _items = MutableStateFlow<List<ItemDto>>(emptyList())
    val items: StateFlow<List<ItemDto>> = _items

    private val _totalItems = MutableStateFlow(0L)
    val totalItems: StateFlow<Long> = _totalItems

    private val _campusStats = MutableStateFlow<List<CampusStatDto>>(emptyList())
    val campusStats: StateFlow<List<CampusStatDto>> = _campusStats

    private val _topContributors = MutableStateFlow<List<User>>(emptyList())
    val topContributors: StateFlow<List<User>> = _topContributors

    init {
        loadCurrentUser()
        loadDashboardItems()
        loadCampusStats()
        loadTopContributors()
    }

    fun loadDashboardItems() {
        viewModelScope.launch {
            val result = itemRepository.getItems(page = 0, size = 50)
            if (result.isSuccess) {
                val page = result.getOrThrow()
                _items.value = page.content
                _totalItems.value = page.totalElements
            }
        }
    }

    fun loadCampusStats() {
        viewModelScope.launch {
            val result = campusRepository.getCampusStats()
            if (result.isSuccess) {
                _campusStats.value = result.getOrThrow()
                    .sortedByDescending { it.activeItems }
            }
        }
    }

    fun loadTopContributors() {
        viewModelScope.launch {
            val result = userRepository.getLeaderboard(size = 5)
            if (result.isSuccess) {
                _topContributors.value = result.getOrThrow()
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Show cached user immediately for a fast initial render
            tokenManager.userJson.first()?.let { json ->
                try {
                    _currentUser.value = gson.fromJson(json, User::class.java)
                } catch (_: Exception) { /* ignore malformed cache */ }
            }

            // Refresh from backend
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                _currentUser.value = result.getOrThrow()
                _uiState.value = UiState.Success(Unit)
            } else {
                // If we already have cached data, treat as success (stale-but-ok)
                _uiState.value = if (_currentUser.value != null) {
                    UiState.Success(Unit)
                } else {
                    UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load profile")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
