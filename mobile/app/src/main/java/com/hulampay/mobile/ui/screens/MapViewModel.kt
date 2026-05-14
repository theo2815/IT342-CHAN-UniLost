package com.hulampay.mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.data.repository.AuthRepository
import com.hulampay.mobile.data.repository.ItemRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ItemDto>>> = _state

    private val _campuses = MutableStateFlow<List<School>>(emptyList())
    val campuses: StateFlow<List<School>> = _campuses

    private val _filter = MutableStateFlow("ALL")
    val filter: StateFlow<String> = _filter

    private val _campusFilterId = MutableStateFlow<String?>(null)
    val campusFilterId: StateFlow<String?> = _campusFilterId

    private val _selectedCampusId = MutableStateFlow<String?>(null)
    val selectedCampusId: StateFlow<String?> = _selectedCampusId

    private val _selectedItemId = MutableStateFlow<String?>(null)
    val selectedItemId: StateFlow<String?> = _selectedItemId

    init {
        loadCampuses()
        load()
    }

    fun setFilter(value: String) {
        if (_filter.value == value) return
        _filter.value = value
        _selectedItemId.value = null
        _selectedCampusId.value = null
        load()
    }

    fun selectCampusMarker(id: String?) {
        _selectedCampusId.value = id
        if (id != null) _selectedItemId.value = null
    }

    fun selectItem(id: String?) {
        _selectedItemId.value = id
        if (id != null) _selectedCampusId.value = null
    }

    fun viewCampusItems(id: String) {
        _selectedCampusId.value = null
        _selectedItemId.value = null
        if (_campusFilterId.value == id) return
        _campusFilterId.value = id
        load()
    }

    fun clearCampusFilter() {
        if (_campusFilterId.value == null) return
        _campusFilterId.value = null
        _selectedItemId.value = null
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val typeParam = _filter.value.takeIf { it != "ALL" }
            val result = itemRepository.getMapItems(
                type = typeParam,
                campusId = _campusFilterId.value,
            )
            _state.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load map items")
            }
        }
    }

    private fun loadCampuses() {
        viewModelScope.launch {
            authRepository.getCampuses()
                .onSuccess { _campuses.value = it }
        }
    }
}
