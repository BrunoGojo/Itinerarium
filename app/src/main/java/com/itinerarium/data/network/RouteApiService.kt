package com.itinerarium.data.network

import com.itinerarium.data.network.dto.OrsDirectionsResponse
import com.itinerarium.data.network.dto.OrsGeocodeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit para endpoints de comunicação com a API OpenRouteService (ORS).
 */
interface RouteApiService {

    /**
     * Endpoint para obter direções/rotas entre coordenadas de início e fim.
     * @param profile Perfil de transporte (ex: driving-car, cycling-regular, foot-walking)
     * @param apiKey Chave de API da OpenRouteService
     * @param start Coordenadas de início no formato "longitude,latitude"
     * @param end Coordenadas de fim no formato "longitude,latitude"
     * @param language Idioma das instruções (padrão "pt" para Português)
     */
    @GET("v2/directions/{profile}")
    suspend fun getDirections(
        @Path("profile") profile: String,
        @Query("api_key") apiKey: String,
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("language") language: String = "pt"
    ): Response<OrsDirectionsResponse>

    /**
     * Endpoint para busca de geocodificação (converter nome/endereço em coordenadas GeoJSON).
     * @param apiKey Chave de API da OpenRouteService
     * @param text Texto de busca/endereço
     * @param size Quantidade de resultados desejada (padrão 1)
     */
    @GET("geocode/search")
    suspend fun searchGeocode(
        @Query("api_key") apiKey: String,
        @Query("text") text: String,
        @Query("size") size: Int = 1
    ): Response<OrsGeocodeResponse>
}
