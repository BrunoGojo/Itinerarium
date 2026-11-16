package com.itinerarium.data.repository

import android.content.Context
import com.itinerarium.data.network.ApiClient

/**
 * Provedor de instâncias de Repositório do aplicativo Itinerarium.
 * Por padrão fornece [RemoteRouteRepositoryImpl] para o cálculo de rotas online via OpenRouteService (ORS) e Nominatim.
 */
object RepositoryProvider {

    /**
     * Flag configurada por padrão como 'false' para utilizar o repositório remoto ORS.
     * Quando ativada como 'true', força o uso direto do [MockRouteRepository].
     */
    var USE_MOCK: Boolean = false

    /**
     * Retorna a implementação ativa do [RouteRepository].
     * Por padrão, retorna [RemoteRouteRepositoryImpl] utilizando o cliente Retrofit.
     */
    fun getRouteRepository(context: Context? = null): RouteRepository {
        if (USE_MOCK) {
            return MockRouteRepository()
        }

        return RemoteRouteRepositoryImpl(
            apiService = ApiClient.routeApiService,
            nominatimApiService = ApiClient.nominatimApiService
        )
    }
}
