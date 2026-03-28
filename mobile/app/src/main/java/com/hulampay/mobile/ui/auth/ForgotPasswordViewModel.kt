package com.hulampay.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<String>>(UiState.Idle)
    val state: StateFlow<UiState<String>> = _state

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _state.value = UiState.Error("Please enter your email address")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = UiState.Error("Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = authRepository.forgotPassword(email)
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to send reset email") },
            )
        }
    }

    fun resetState() {
        _state.value = UiState.Idle
    }
}
