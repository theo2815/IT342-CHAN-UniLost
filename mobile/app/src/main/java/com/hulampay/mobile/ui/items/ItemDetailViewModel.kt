package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.model.ClaimRequest
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.data.repository.ClaimRepository
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
    val incomingClaims: List<ClaimDto>,
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

/**
 * Result of submitting a claim from the item-detail bottom sheet.
 * Distinct from the screen's main `state` so the sheet can show a success/error
 * UI independent of the item load.
 */
sealed class SubmitClaimState {
    object Idle : SubmitClaimState()
    object Submitting : SubmitClaimState()
    data class Success(val claim: ClaimDto) : SubmitClaimState()
    data class Error(val message: String) : SubmitClaimState()
}

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val claimRepository: ClaimRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _state = MutableStateFlow<UiState<ItemDetailData>>(UiState.Idle)
    val state: StateFlow<UiState<ItemDetailData>> = _state

    private val _submitState = MutableStateFlow<SubmitClaimState>(SubmitClaimState.Idle)
    val submitState: StateFlow<SubmitClaimState> = _submitState

    private val _incomingActionInFlight = MutableStateFlow(false)
    val incomingActionInFlight: StateFlow<Boolean> = _incomingActionInFlight

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
            val isPoster = cachedUser?.id != null && cachedUser.id ==
                (item.reporter?.id ?: item.reporterId)
            val incoming = if (isPoster) loadIncomingClaims(item.id) else emptyList()

            _state.value = UiState.Success(
                ItemDetailData(
                    item = item,
                    relatedItems = related,
                    incomingClaims = incoming,
                    currentUserId = cachedUser?.id,
                    isAdmin = cachedUser?.isAdmin == true,
                )
            )
        }
    }

    fun submitClaim(message: String, providedAnswer: String?) {
        val current = _state.value
        val data = (current as? UiState.Success)?.data ?: return
        if (_submitState.value is SubmitClaimState.Submitting) return
        _submitState.value = SubmitClaimState.Submitting
        viewModelScope.launch {
            val result = claimRepository.submitClaim(
                ClaimRequest(
                    itemId = data.item.id,
                    providedAnswer = providedAnswer?.takeIf { it.isNotBlank() },
                    message = message,
                )
            )
            _submitState.value = if (result.isSuccess) {
                SubmitClaimState.Success(result.getOrThrow())
            } else {
                SubmitClaimState.Error(result.exceptionOrNull()?.message ?: "Failed to submit claim")
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitClaimState.Idle
    }

    fun acceptIncomingClaim(claimId: String) =
        mutateIncoming(claimId) { claimRepository.acceptClaim(it) }

    fun rejectIncomingClaim(claimId: String) =
        mutateIncoming(claimId) { claimRepository.rejectClaim(it) }

    private fun mutateIncoming(claimId: String, block: suspend (String) -> Result<ClaimDto>) {
        val current = _state.value
        val data = (current as? UiState.Success)?.data ?: return
        if (_incomingActionInFlight.value) return
        _incomingActionInFlight.value = true
        viewModelScope.launch {
            val result = block(claimId)
            _incomingActionInFlight.value = false
            if (result.isSuccess) {
                val updated = result.getOrThrow()
                val newList = data.incomingClaims.map { if (it.id == updated.id) updated else it }
                _state.value = UiState.Success(data.copy(incomingClaims = newList))
            }
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

    private suspend fun loadIncomingClaims(itemId: String): List<ClaimDto> {
        val result = claimRepository.getClaimsForItem(itemId, page = 0, size = 50)
        return if (result.isSuccess) result.getOrThrow().content else emptyList()
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
