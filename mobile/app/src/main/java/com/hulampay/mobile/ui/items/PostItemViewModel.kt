package com.hulampay.mobile.ui.items

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.ItemCategory
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.ItemRequest
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class PostItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _submitState = MutableStateFlow<UiState<ItemDto>>(UiState.Idle)
    val submitState: StateFlow<UiState<ItemDto>> = _submitState

    /**
     * When the screen opens in edit mode, the existing item lands here so the screen
     * can prefill its form state. Null while loading / on a fresh post.
     */
    private val _prefill = MutableStateFlow<ItemDto?>(null)
    val prefill: StateFlow<ItemDto?> = _prefill

    fun resetSubmitState() {
        _submitState.value = UiState.Idle
    }

    fun loadForEdit(itemId: String) {
        if (itemId.isBlank()) return
        if (_prefill.value?.id == itemId) return
        viewModelScope.launch {
            val result = itemRepository.getItemById(itemId)
            if (result.isSuccess) {
                _prefill.value = result.getOrThrow()
            } else {
                _submitState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load item for editing"
                )
            }
        }
    }

    fun submit(
        type: String,
        title: String,
        description: String,
        categoryDisplay: String,
        location: String,
        secretDetail: String,
        dateMillis: Long?,
        imageUris: List<Uri>,
    ) {
        if (imageUris.size > MAX_IMAGES) {
            _submitState.value = UiState.Error("Maximum $MAX_IMAGES images allowed")
            return
        }
        val category = ItemCategory.displayToBackend(categoryDisplay)
        if (category == null) {
            _submitState.value = UiState.Error("Please choose a category")
            return
        }

        _submitState.value = UiState.Loading
        viewModelScope.launch {
            val user = readCachedUser()
            val request = ItemRequest(
                title = title.trim(),
                description = description.trim(),
                type = type,
                category = category,
                location = location.trim().takeIf { it.isNotBlank() },
                secretDetailQuestion = secretDetail.trim()
                    .takeIf { it.isNotBlank() && type == "FOUND" },
                dateLostFound = dateMillis?.let { toIsoLocalDateTime(it) },
                campusId = user?.universityTag,
            )

            val parts = imageUris.mapIndexedNotNull { index, uri -> uriToImagePart(uri, index) }
            val result = itemRepository.createItem(request, parts)
            _submitState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to post item")
            }
        }
    }

    fun update(
        itemId: String,
        type: String,
        title: String,
        description: String,
        categoryDisplay: String,
        location: String,
        secretDetail: String,
        dateMillis: Long?,
        imageUris: List<Uri>,
    ) {
        if (imageUris.size > MAX_IMAGES) {
            _submitState.value = UiState.Error("Maximum $MAX_IMAGES images allowed")
            return
        }
        val category = ItemCategory.displayToBackend(categoryDisplay)
        if (category == null) {
            _submitState.value = UiState.Error("Please choose a category")
            return
        }

        _submitState.value = UiState.Loading
        viewModelScope.launch {
            val user = readCachedUser()
            val request = ItemRequest(
                title = title.trim(),
                description = description.trim(),
                type = type,
                category = category,
                location = location.trim().takeIf { it.isNotBlank() },
                secretDetailQuestion = secretDetail.trim()
                    .takeIf { it.isNotBlank() && type == "FOUND" },
                dateLostFound = dateMillis?.let { toIsoLocalDateTime(it) },
                campusId = user?.universityTag,
            )

            val parts = imageUris.mapIndexedNotNull { index, uri -> uriToImagePart(uri, index) }
            val result = itemRepository.updateItem(itemId, request, parts)
            _submitState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update item")
            }
        }
    }

    private fun toIsoLocalDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(millis))
    }

    private suspend fun uriToImagePart(uri: Uri, index: Int): MultipartBody.Part? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext null
                val ext = when {
                    mimeType.contains("png") -> "png"
                    mimeType.contains("webp") -> "webp"
                    else -> "jpg"
                }
                val body = bytes.toRequestBody(mimeType.toMediaType())
                MultipartBody.Part.createFormData("images", "image_$index.$ext", body)
            } catch (e: Exception) {
                null
            }
        }

    private suspend fun readCachedUser(): User? {
        val json = tokenManager.userJson.first() ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val MAX_IMAGES = 3
    }
}
