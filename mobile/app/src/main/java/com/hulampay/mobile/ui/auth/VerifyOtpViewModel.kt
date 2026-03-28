package com.hulampay.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _verifyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val verifyState: StateFlow<UiState<String>> = _verifyState

    /** Counts down from 60 to 0. Resend button is disabled while > 0. */
    private val _resendCountdown = MutableStateFlow(60)
    val resendCountdown: StateFlow<Int> = _resendCountdown

    init {
        // 60s initial cooldown so the user can't immediately spam resend
        startCountdown()
    }

    fun verifyOtp(email: String, otp: String) {
        if (otp.length != 6) {
            _verifyState.value = UiState.Error("Enter all 6 digits")
            return
        }
        viewModelScope.launch {
            _verifyState.value = UiState.Loading
            val result = authRepository.verifyOtp(email, otp)
            _verifyState.value = result.fold(
                onSuccess = { resetToken -> UiState.Success(resetToken) },
                onFailure = { UiState.Error(it.message ?: "Invalid or expired code") }
            )
        }
    }

    fun resendOtp(email: String) {
        if (_resendCountdown.value > 0) return
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            if (result.isSuccess) {
                startCountdown()
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            _resendCountdown.value = 60
            while (_resendCountdown.value > 0) {
                delay(1_000)
                _resendCountdown.value -= 1
            }
        }
    }

    fun resetState() {
        _verifyState.value = UiState.Idle
    }
}
