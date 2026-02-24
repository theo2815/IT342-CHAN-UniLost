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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<UiState<Any>>(UiState.Idle)
    val registerState: StateFlow<UiState<Any>> = _registerState

    private val _schoolsState = MutableStateFlow<List<com.hulampay.mobile.data.model.School>>(emptyList())
    val schoolsState: StateFlow<List<com.hulampay.mobile.data.model.School>> = _schoolsState

    init {
        getSchools()
    }

    private fun getSchools() {
        viewModelScope.launch {
            val result = authRepository.getSchools()
            if (result.isSuccess) {
                _schoolsState.value = result.getOrThrow()
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        address: String,
        phoneNumber: String,
        studentIdNumber: String
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = UiState.Error("First name, last name, email, and password are required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = UiState.Error("Invalid email address")
            return
        }

        if (password.length < 6) {
             _registerState.value = UiState.Error("Password must be at least 6 characters")
             return
        }

        if (password != confirmPassword) {
            _registerState.value = UiState.Error("Passwords do not match")
            return
        }

        _registerState.value = UiState.Loading
        viewModelScope.launch {
            val data = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "password" to password
            )
            if (address.isNotBlank()) data["address"] = address
            if (phoneNumber.isNotBlank()) data["phoneNumber"] = phoneNumber
            if (studentIdNumber.isNotBlank()) data["studentIdNumber"] = studentIdNumber

            val result = authRepository.register(data)
            if (result.isSuccess) {
                _registerState.value = UiState.Success(result.getOrThrow())
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Registration failed"
                _registerState.value = UiState.Error(errorMsg)
            }
        }
    }
}
