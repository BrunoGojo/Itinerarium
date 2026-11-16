package com.itinerarium.data.network

import com.itinerarium.data.network.dto.OrsDirectionsRequest
import com.itinerarium.data.network.dto.OrsDirectionsResponse
import com.itinerarium.data.network.dto.OrsGeocodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit para endpoints de comunicação com a API OpenRouteService (ORS).
 */
interface RouteApiService {

    /**
     * Endpoint para obter direções/rotas entre coordenadas de início e fim.
     *
     * Usa POST (e não GET) porque somente o POST aceita o parâmetro `radiuses`, necessário
     * para evitar o erro 2010 ("Could not find routable point within a radius of 350.0 meters").
     *
     * @param profile Perfil de transporte (ex: driving-car, cycling-regular, foot-walking)
     * @param apiKey Chave de API da OpenRouteService
     * @param body Coordenadas ([longitude, latitude]), raios de busca e idioma
     */
    @POST("v2/directions/{profile}/geojson")
    suspend fun getDirections(
        @Path("profile") profile: String,
        @Query("api_key") apiKey: String,
        @Body body: OrsDirectionsRequest
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
