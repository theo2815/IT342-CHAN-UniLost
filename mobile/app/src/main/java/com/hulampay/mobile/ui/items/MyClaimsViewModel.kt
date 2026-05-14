package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.repository.ClaimRepository
import com.hulampay.mobile.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyClaimsViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ClaimDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ClaimDto>>> = _state

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = claimRepository.getMyClaims(page = 0, size = 50)
            _state.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow().content)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load your claims")
            }
        }
    }
}
