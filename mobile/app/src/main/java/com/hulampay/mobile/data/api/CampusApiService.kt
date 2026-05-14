package com.hulampay.mobile.data.api

import com.hulampay.mobile.data.model.CampusStatDto
import retrofit2.Response
import retrofit2.http.GET

interface CampusApiService {

    /** GET /api/campuses/stats — list of campuses with their active-item counts. */
    @GET("campuses/stats")
    suspend fun getCampusStats(): Response<List<CampusStatDto>>
}
