package com.hulampay.mobile.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.api.AppGson
import com.hulampay.mobile.data.model.ClaimDto
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

/**
 * View-model state for ClaimDetailScreen.
 *
 * `posterId`/`posterName` resolve to the original item reporter (NOT necessarily
 * the same as `claim.finderId`: for LOST items the finder is the claimant, while
 * the poster is the owner who lost the item).
 */
data class ClaimDetailData(
    val claim: ClaimDto,
    val posterId: String,
    val posterName: String,
    val currentUserId: String?,
) {
    val isClaimant: Boolean
        get() = currentUserId != null && currentUserId == claim.claimantId

    val isPoster: Boolean
        get() = currentUserId != null && currentUserId == posterId

    /** For FOUND items the poster is the finder; for LOST items the claimant is. */
    val isFinder: Boolean
        get() = currentUserId != null && currentUserId == claim.finderId

    val isOwner: Boolean
        get() = currentUserId != null && currentUserId != claim.finderId &&
            (currentUserId == posterId || currentUserId == claim.claimantId)
}

@HiltViewModel
class ClaimDetailViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val itemRepository: ItemRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val gson = AppGson.instance

    private val _state = MutableStateFlow<UiState<ClaimDetailData>>(UiState.Idle)
    val state: StateFlow<UiState<ClaimDetailData>> = _state

    private val _actionInFlight = MutableStateFlow(false)
    val actionInFlight: StateFlow<Boolean> = _actionInFlight

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError

    fun load(claimId: String) {
        if (claimId.isBlank()) {
            _state.value = UiState.Error("Missing claim id")
            return
        }
        _state.value = UiState.Loading
        viewModelScope.launch {
            val cachedUser = readCachedUser()
            val claimResult = claimRepository.getClaimById(claimId)
            if (claimResult.isFailure) {
                _state.value = UiState.Error(
                    claimResult.exceptionOrNull()?.message ?: "Failed to load claim"
                )
                return@launch
            }
            val claim = claimResult.getOrThrow()
            val poster = resolveItemPoster(claim)
            _state.value = UiState.Success(
                ClaimDetailData(
                    claim = claim,
                    posterId = poster.first,
                    posterName = poster.second,
                    currentUserId = cachedUser?.id,
                )
            )
        }
    }

    private suspend fun resolveItemPoster(claim: ClaimDto): Pair<String, String> {
        // For FOUND items the finder *is* the poster — short-circuit to avoid an item fetch.
        if (claim.itemType.equals("FOUND", ignoreCase = true)) {
            return claim.finderId to claim.finderName
        }
        val itemResult = itemRepository.getItemById(claim.itemId)
        val item: ItemDto? = itemResult.getOrNull()
        val posterId = item?.reporter?.id ?: item?.reporterId ?: ""
        val posterName = item?.reporter?.fullName.takeUnless { it.isNullOrBlank() } ?: ""
        return posterId to posterName
    }

    fun acceptClaim() = runAction { id -> claimRepository.acceptClaim(id) }
    fun rejectClaim() = runAction { id -> claimRepository.rejectClaim(id) }
    fun cancelClaim() = runAction { id -> claimRepository.cancelClaim(id) }
    fun markReturned() = runAction { id -> claimRepository.markReturned(id) }
    fun confirmReceived() = runAction { id -> claimRepository.confirmReceived(id) }
    fun disputeHandover() = runAction { id -> claimRepository.disputeHandover(id) }

    fun consumeActionError() {
        _actionError.value = null
    }

    private fun runAction(block: suspend (String) -> Result<ClaimDto>) {
        val current = _state.value
        val data = (current as? UiState.Success)?.data ?: return
        if (_actionInFlight.value) return
        _actionInFlight.value = true
        viewModelScope.launch {
            val result = block(data.claim.id)
            _actionInFlight.value = false
            if (result.isSuccess) {
                _state.value = UiState.Success(data.copy(claim = result.getOrThrow()))
            } else {
                _actionError.value = result.exceptionOrNull()?.message ?: "Action failed"
            }
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
}
