package com.hulampay.mobile.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.preferences.NotificationPreferences
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.data.repository.UserRepository
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.isPasswordValid
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val notificationPreferences: NotificationPreferences,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _profileState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val profileState: StateFlow<UiState<String>> = _profileState

    private val _passwordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val passwordState: StateFlow<UiState<String>> = _passwordState

    val notificationsEnabled: StateFlow<Boolean> = notificationPreferences.enabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        loadCachedUser()
        refreshFromServer()
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

    private fun refreshFromServer() {
        viewModelScope.launch {
            authRepository.getCurrentUser().getOrNull()?.let { user ->
                _currentUser.value = user
            }
        }
    }

    fun saveProfile(fullName: String, photoUri: Uri?) {
        val current = _currentUser.value
        val userId = current?.id
        if (userId.isNullOrBlank()) {
            _profileState.value = UiState.Error("You're not signed in.")
            return
        }
        val trimmedName = fullName.trim()
        if (trimmedName.isBlank()) {
            _profileState.value = UiState.Error("Full name cannot be empty")
            return
        }
        if (trimmedName.length > 100) {
            _profileState.value = UiState.Error("Full name must be 100 characters or fewer")
            return
        }

        viewModelScope.launch {
            _profileState.value = UiState.Loading

            var latest = current
            // Upload the pending picture first (matches website ordering).
            if (photoUri != null) {
                val upload = uploadPhoto(userId, photoUri)
                if (upload.isFailure) {
                    _profileState.value = UiState.Error(
                        upload.exceptionOrNull()?.message ?: "Failed to upload photo"
                    )
                    return@launch
                }
                latest = upload.getOrThrow()
            }

            if (trimmedName != latest.fullName) {
                val updated = userRepository.updateProfile(userId, trimmedName)
                if (updated.isFailure) {
                    _profileState.value = UiState.Error(
                        updated.exceptionOrNull()?.message ?: "Failed to update profile"
                    )
                    return@launch
                }
                latest = updated.getOrThrow()
            }

            _currentUser.value = latest
            tokenManager.saveUser(gson.toJson(latest))
            _profileState.value = UiState.Success("Profile updated successfully")
        }
    }

    private suspend fun uploadPhoto(userId: String, uri: Uri): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure<User>(
                        IllegalStateException("Could not read the selected image")
                    )
                val ext = when {
                    mimeType.contains("png") -> "png"
                    mimeType.contains("gif") -> "gif"
                    mimeType.contains("webp") -> "webp"
                    else -> "jpg"
                }
                val body = bytes.toRequestBody(mimeType.toMediaType())
                userRepository.uploadProfilePicture(userId, "profile_picture.$ext", body)
            } catch (e: Exception) {
                Result.failure(e)
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

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setEnabled(enabled)
        }
    }

    fun consumeProfileState() {
        _profileState.value = UiState.Idle
    }

    fun consumePasswordState() {
        _passwordState.value = UiState.Idle
    }
}
