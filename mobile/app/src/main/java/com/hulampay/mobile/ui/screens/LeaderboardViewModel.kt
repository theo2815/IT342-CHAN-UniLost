package com.hulampay.mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.AuthRepository
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
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _state = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val state: StateFlow<UiState<List<User>>> = _state

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _campuses = MutableStateFlow<List<School>>(emptyList())
    val campuses: StateFlow<List<School>> = _campuses

    private val _selectedCampusId = MutableStateFlow<String?>(null)
    val selectedCampusId: StateFlow<String?> = _selectedCampusId

    init {
        viewModelScope.launch {
            tokenManager.userJson.first()?.let { json ->
                runCatching { gson.fromJson(json, User::class.java) }
                    .getOrNull()
                    ?.let { _currentUserId.value = it.id }
            }
            authRepository.getCampuses().getOrNull()?.let { _campuses.value = it }
        }
        load()
    }

    fun selectCampus(campusId: String?) {
        val normalized = campusId?.takeIf { it.isNotBlank() }
        if (_selectedCampusId.value == normalized) return
        _selectedCampusId.value = normalized
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = userRepository.getLeaderboard(
                size = 20,
                campusId = _selectedCampusId.value,
            )
            _state.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load leaderboard")
            }
        }
    }
}
