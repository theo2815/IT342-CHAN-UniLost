package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemFeedViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _itemsState = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Idle)
    val itemsState: StateFlow<UiState<List<ItemDto>>> = _itemsState

    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeType = MutableStateFlow("All")
    val activeType: StateFlow<String> = _activeType

    private val _activeCategory = MutableStateFlow("")
    val activeCategory: StateFlow<String> = _activeCategory

    private val _activeCampusId = MutableStateFlow("")
    val activeCampusId: StateFlow<String> = _activeCampusId

    private val _campuses = MutableStateFlow<List<School>>(emptyList())
    val campuses: StateFlow<List<School>> = _campuses

    private var currentPage = 0
    private var debounceJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.getCampuses().getOrNull()?.let { _campuses.value = it }
        }
        loadItems(append = false)
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(400)
            loadItems(append = false)
        }
    }

    fun setType(type: String) {
        if (_activeType.value == type) return
        _activeType.value = type
        loadItems(append = false)
    }

    fun setCategory(category: String) {
        val normalized = category.takeIf { it.isNotBlank() }.orEmpty()
        if (_activeCategory.value == normalized) return
        _activeCategory.value = normalized
        loadItems(append = false)
    }

    fun setCampus(campusId: String) {
        val normalized = campusId.takeIf { it.isNotBlank() }.orEmpty()
        if (_activeCampusId.value == normalized) return
        _activeCampusId.value = normalized
        loadItems(append = false)
    }

    fun loadMore() {
        val current = (_itemsState.value as? UiState.Success)?.data.orEmpty()
        if (_isLoadingMore.value) return
        if (current.size >= _totalItems.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage += 1
            fetchPage(currentPage, append = true)
            _isLoadingMore.value = false
        }
    }

    fun loadItems() = loadItems(append = false)

    private fun loadItems(append: Boolean) {
        if (!append) {
            currentPage = 0
            _itemsState.value = UiState.Loading
        }
        viewModelScope.launch {
            fetchPage(currentPage, append)
        }
    }

    private suspend fun fetchPage(page: Int, append: Boolean) {
        val typeParam = when (_activeType.value) {
            "Lost" -> "LOST"
            "Found" -> "FOUND"
            else -> null
        }
        val result = itemRepository.getItems(
            keyword = _searchQuery.value.takeIf { it.isNotBlank() },
            campusId = _activeCampusId.value.takeIf { it.isNotBlank() },
            category = _activeCategory.value.takeIf { it.isNotBlank() },
            type = typeParam,
            status = "ACTIVE",
            page = page,
            size = ITEMS_PER_PAGE,
        )
        if (result.isSuccess) {
            val pageData = result.getOrThrow()
            _totalItems.value = pageData.totalElements.toInt()
            val previous = (_itemsState.value as? UiState.Success)?.data.orEmpty()
            _itemsState.value = UiState.Success(
                if (append) previous + pageData.content else pageData.content
            )
        } else if (!append) {
            _itemsState.value = UiState.Error(
                result.exceptionOrNull()?.message ?: "Failed to load items"
            )
        }
    }

    companion object {
        private const val ITEMS_PER_PAGE = 9
    }
}
