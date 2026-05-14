package com.hulampay.mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.UserRepository
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.isPasswordValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _passwordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val passwordState: StateFlow<UiState<String>> = _passwordState

    init {
        loadCachedUser()
    }

    private fun loadCachedUser() {
        viewModelScope.launch {
            tokenManager.userJson.first()?.let { json ->
                runCatching { gson.fromJson(json, User::class.java) }
                    .getOrNull()
                    ?.let { _currentUser.value = it }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        val userId = _currentUser.value?.id
        if (userId.isNullOrBlank()) {
            _passwordState.value = UiState.Error("You're not signed in.")
            return
        }
        if (currentPassword.isBlank()) {
            _passwordState.value = UiState.Error("Current password is required")
            return
        }
        if (!isPasswordValid(newPassword)) {
            _passwordState.value = UiState.Error(
                "Password must be 8+ characters with uppercase, number, and special character"
            )
            return
        }
        if (newPassword != confirmPassword) {
            _passwordState.value = UiState.Error("Passwords do not match")
            return
        }
        if (newPassword == currentPassword) {
            _passwordState.value =
                UiState.Error("New password must be different from your current password")
            return
        }
        viewModelScope.launch {
            _passwordState.value = UiState.Loading
            val result = userRepository.changePassword(userId, currentPassword, newPassword)
            _passwordState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to change password") },
            )
        }
    }

    fun consumePasswordState() {
        _passwordState.value = UiState.Idle
    }
}
