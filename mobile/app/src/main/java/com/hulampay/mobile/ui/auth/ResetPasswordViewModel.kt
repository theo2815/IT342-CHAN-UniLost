package com.hulampay.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.isPasswordValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _resetState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetState: StateFlow<UiState<String>> = _resetState

    fun resetPassword(
        email: String,
        resetToken: String,
        newPassword: String,
        confirmPassword: String,
    ) {
        if (!isPasswordValid(newPassword)) {
            _resetState.value = UiState.Error(
                "Password must be 8+ characters with uppercase, number, and special character"
            )
            return
        }
        if (newPassword != confirmPassword) {
            _resetState.value = UiState.Error("Passwords do not match")
            return
        }
        viewModelScope.launch {
            _resetState.value = UiState.Loading
            val result = authRepository.resetPassword(email, resetToken, newPassword)
            _resetState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to reset password") }
            )
        }
    }
}
