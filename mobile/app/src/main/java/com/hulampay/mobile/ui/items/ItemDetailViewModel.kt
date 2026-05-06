package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.utils.TokenManager
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailData(
    val item: ItemDto,
    val relatedItems: List<ItemDto>,
    val currentUserId: String?,
    val isAdmin: Boolean,
) {
    val isPoster: Boolean
        get() {
            val me = currentUserId ?: return false
            val owner = item.reporter?.id ?: item.reporterId ?: return false
            return me == owner
        }
}

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = Gson()

    private val _state = MutableStateFlow<UiState<ItemDetailData>>(UiState.Idle)
    val state: StateFlow<UiState<ItemDetailData>> = _state

    fun load(itemId: String) {
        if (itemId.isBlank()) {
            _state.value = UiState.Error("Missing item id")
            return
        }
        _state.value = UiState.Loading
        viewModelScope.launch {
            val cachedUser = readCachedUser()
            val itemResult = itemRepository.getItemById(itemId)
            if (itemResult.isFailure) {
                _state.value = UiState.Error(
                    itemResult.exceptionOrNull()?.message ?: "Failed to load item"
                )
                return@launch
            }
            val item = itemResult.getOrThrow()

            val related = loadRelated(item)

            _state.value = UiState.Success(
                ItemDetailData(
                    item = item,
                    relatedItems = related,
                    currentUserId = cachedUser?.id,
                    isAdmin = cachedUser?.isAdmin == true,
                )
            )
        }
    }

    private suspend fun loadRelated(item: ItemDto): List<ItemDto> {
        val result = itemRepository.getItems(
            campusId = item.campusId,
            category = item.category,
            status = "ACTIVE",
            size = 4,
        )
        return if (result.isSuccess) {
            result.getOrThrow().content.filter { it.id != item.id }.take(3)
        } else emptyList()
    }

    private suspend fun readCachedUser(): User? {
        val json = tokenManager.userJson.first() ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
