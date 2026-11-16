package com.itinerarium.data.repository

import com.itinerarium.data.model.Route
import com.itinerarium.data.model.TransportMode

/**
 * Interface que define as operações do repositório de rotas do Itinerarium.
 */
interface RouteRepository {

    /**
     * Calcula as rotas possíveis entre a origem e o destino fornecidos,
     * podendo opcionalmente filtrar ou priorizar um determinado modal de transporte.
     */
    suspend fun calculateRoutes(
        origin: String,
        destination: String,
        selectedMode: TransportMode? = null
    ): Result<List<Route>>
}
