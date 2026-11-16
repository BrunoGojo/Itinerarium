package com.itinerarium.data.network

import com.itinerarium.data.network.dto.NominatimPlaceDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Interface Retrofit para consulta de geocodificação no Nominatim (OpenStreetMap).
 */
interface NominatimApiService {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") countryCodes: String = "br",
        @Query("accept-language") language: String = "pt-BR",
        @Query("addressdetails") addressDetails: Int = 1,
        @Header("User-Agent") userAgent: String = "ItinerariumApp/1.0"
    ): Response<List<NominatimPlaceDto>>
}
