package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.api.ClaimApiService
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.model.ClaimRequest
import com.hulampay.mobile.data.model.PageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaimRepository @Inject constructor(
    private val claimApiService: ClaimApiService,
) {

    suspend fun submitClaim(request: ClaimRequest): Result<ClaimDto> =
        runCatching {
            val response = claimApiService.submitClaim(request)
            response.bodyOrThrow("Failed to submit claim")
        }

    suspend fun getMyClaims(page: Int = 0, size: Int = 50): Result<PageDto<ClaimDto>> =
        runCatching {
            val response = claimApiService.getMyClaims(page, size)
            response.bodyOrThrow("Failed to load your claims")
        }

    suspend fun getIncomingClaims(page: Int = 0, size: Int = 50): Result<PageDto<ClaimDto>> =
        runCatching {
            val response = claimApiService.getIncomingClaims(page, size)
            response.bodyOrThrow("Failed to load incoming claims")
        }

    suspend fun getClaimsForItem(itemId: String, page: Int = 0, size: Int = 50): Result<PageDto<ClaimDto>> =
        runCatching {
            val response = claimApiService.getClaimsForItem(itemId, page, size)
            response.bodyOrThrow("Failed to load claims for item")
        }

    suspend fun getClaimById(id: String): Result<ClaimDto> =
        runCatching {
            val response = claimApiService.getClaimById(id)
            if (response.code() == 404) throw NoSuchElementException("Claim not found")
            response.bodyOrThrow("Failed to load claim")
        }

    suspend fun acceptClaim(id: String): Result<ClaimDto> =
        runCatching { claimApiService.acceptClaim(id).bodyOrThrow("Failed to accept claim") }

    suspend fun rejectClaim(id: String): Result<ClaimDto> =
        runCatching { claimApiService.rejectClaim(id).bodyOrThrow("Failed to reject claim") }

    suspend fun cancelClaim(id: String): Result<ClaimDto> =
        runCatching { claimApiService.cancelClaim(id).bodyOrThrow("Failed to cancel claim") }

    suspend fun markReturned(id: String): Result<ClaimDto> =
        runCatching { claimApiService.markReturned(id).bodyOrThrow("Failed to mark item returned") }

    suspend fun confirmReceived(id: String): Result<ClaimDto> =
        runCatching { claimApiService.confirmReceived(id).bodyOrThrow("Failed to confirm receipt") }

    suspend fun disputeHandover(id: String): Result<ClaimDto> =
        runCatching { claimApiService.disputeHandover(id).bodyOrThrow("Failed to dispute handover") }
}

private fun <T> retrofit2.Response<T>.bodyOrThrow(fallbackMessage: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException(fallbackMessage)
    }
    val errorMessage = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: fallbackMessage
    throw IllegalStateException(errorMessage)
}
