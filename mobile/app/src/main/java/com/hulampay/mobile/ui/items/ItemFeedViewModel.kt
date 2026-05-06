package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemFeedViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
) : ViewModel() {

    private val _itemsState = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Idle)
    val itemsState: StateFlow<UiState<List<ItemDto>>> = _itemsState

    init {
        loadItems()
    }

    fun loadItems() {
        _itemsState.value = UiState.Loading
        viewModelScope.launch {
            val result = itemRepository.getItems(page = 0, size = 50)
            if (result.isSuccess) {
                _itemsState.value = UiState.Success(result.getOrThrow().content)
            } else {
                _itemsState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load items"
                )
            }
        }
    }
}
