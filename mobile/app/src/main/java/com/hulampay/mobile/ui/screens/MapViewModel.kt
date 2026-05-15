package com.hulampay.mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
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

/** Lifecycle of the device-location signal that drives the My-Location FAB. */
enum class LocationStatus { UNKNOWN, GRANTED, DENIED, UNAVAILABLE }

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

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _locationStatus = MutableStateFlow(LocationStatus.UNKNOWN)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus

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

    fun onLocationPermission(granted: Boolean) {
        _locationStatus.value = if (granted) LocationStatus.GRANTED else LocationStatus.DENIED
        if (!granted) _userLocation.value = null
    }

    fun setUserLocation(latLng: LatLng?) {
        _userLocation.value = latLng
        if (latLng != null && _locationStatus.value != LocationStatus.GRANTED) {
            _locationStatus.value = LocationStatus.GRANTED
        }
    }

    fun markLocationUnavailable() {
        _locationStatus.value = LocationStatus.UNAVAILABLE
        _userLocation.value = null
    }
}
