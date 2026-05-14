package com.hulampay.mobile.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.isPasswordValid
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

    /** Full list loaded from /api/campuses */
    private val _allCampuses = MutableStateFlow<List<School>>(emptyList())

    /** Campuses whose domainWhitelist matches the current email domain */
    private val _matchedCampuses = MutableStateFlow<List<School>>(emptyList())
    val matchedCampuses: StateFlow<List<School>> = _matchedCampuses

    init {
        loadCampuses()
    }

    private fun loadCampuses() {
        viewModelScope.launch {
            val result = authRepository.getCampuses()
            if (result.isSuccess) {
                _allCampuses.value = result.getOrThrow()
            }
        }
    }

    /**
     * Call whenever the email field changes.
     * Updates [matchedCampuses] so the UI can show the auto-detect chip or dropdown.
     */
    fun onEmailChanged(email: String) {
        if (!email.contains("@")) {
            _matchedCampuses.value = emptyList()
            return
        }
        val domain = email.substringAfter("@").lowercase().trim()
        _matchedCampuses.value = _allCampuses.value.filter {
            it.domainWhitelist?.lowercase() == domain
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        campusId: String?,
        agreedToTerms: Boolean,
    ) {
        // ── Client-side validation ────────────────────────────────────────────
        if (fullName.isBlank()) {
            _registerState.value = UiState.Error("Full name is required")
            return
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = UiState.Error("Enter a valid university email")
            return
        }
        if (_matchedCampuses.value.isEmpty()) {
            _registerState.value = UiState.Error("Email domain not recognized. Use your university email.")
            return
        }
        if (_matchedCampuses.value.size > 1 && campusId.isNullOrBlank()) {
            _registerState.value = UiState.Error("Please select your campus")
            return
        }
        if (!isPasswordValid(password)) {
            _registerState.value =
                UiState.Error("Password must be 8+ characters with uppercase, number, and special character")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = UiState.Error("Passwords do not match")
            return
        }
        if (!agreedToTerms) {
            _registerState.value = UiState.Error("You must agree to the Terms of Service")
            return
        }

        // ── Determine campusId to send ────────────────────────────────────────
        val resolvedCampusId = when {
            !campusId.isNullOrBlank() -> campusId
            _matchedCampuses.value.size == 1 -> _matchedCampuses.value.first().id
            else -> null
        }

        _registerState.value = UiState.Loading
        viewModelScope.launch {
            val data = mutableMapOf(
                "fullName" to fullName,
                "email"    to email,
                "password" to password,
            )
            resolvedCampusId?.let { data["campusId"] = it }

            val result = authRepository.register(data)
            _registerState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}
