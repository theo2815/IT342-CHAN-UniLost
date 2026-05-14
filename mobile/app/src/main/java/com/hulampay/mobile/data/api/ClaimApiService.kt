package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.model.ClaimRequest
import com.hulampay.mobile.data.model.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClaimApiService {

    /** POST /api/claims — submit a claim on an item. */
    @POST("claims")
    suspend fun submitClaim(@Body request: ClaimRequest): Response<ClaimDto>

    /** GET /api/claims/my - claims I (the caller) submitted, paginated DESC by createdAt. */
    @GET("claims/my")
    suspend fun getMyClaims(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<PageDto<ClaimDto>>

    /** GET /api/claims/incoming — claims on items I posted, paginated. */
    @GET("claims/incoming")
    suspend fun getIncomingClaims(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<PageDto<ClaimDto>>

    /** GET /api/claims/item/{itemId} — all claims for one item (finder only). */
    @GET("claims/item/{itemId}")
    suspend fun getClaimsForItem(
        @Path("itemId") itemId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<PageDto<ClaimDto>>

    /** GET /api/claims/{id} */
    @GET("claims/{id}")
    suspend fun getClaimById(@Path("id") id: String): Response<ClaimDto>

    @PUT("claims/{id}/accept")
    suspend fun acceptClaim(@Path("id") id: String): Response<ClaimDto>

    @PUT("claims/{id}/reject")
    suspend fun rejectClaim(@Path("id") id: String): Response<ClaimDto>

    @PUT("claims/{id}/cancel")
    suspend fun cancelClaim(@Path("id") id: String): Response<ClaimDto>

    /** Finder marks the item physically returned; item → PENDING_OWNER_CONFIRMATION. */
    @PUT("claims/{id}/mark-returned")
    suspend fun markReturned(@Path("id") id: String): Response<ClaimDto>

    /** Owner confirms receipt; claim → COMPLETED, item → RETURNED, karma awarded. */
    @PUT("claims/{id}/confirm-received")
    suspend fun confirmReceived(@Path("id") id: String): Response<ClaimDto>

    @PUT("claims/{id}/dispute-handover")
    suspend fun disputeHandover(@Path("id") id: String): Response<ClaimDto>
}
