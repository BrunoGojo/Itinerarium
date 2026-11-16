package com.itinerarium.data.repository

import android.content.Context
import com.itinerarium.data.model.LatLngData
import com.itinerarium.data.model.Route
import com.itinerarium.data.model.TransportMode

/**
 * Repositório de rotas offline com motor GraphHopper e fallback gracioso para MockRouteRepository.
 */
class OfflineRouteRepositoryImpl(
    private val context: Context? = null,
    private val engineManager: GraphHopperEngineManager? = context?.let { GraphHopperEngineManager(it) },
    private val fallbackRepository: RouteRepository = MockRouteRepository()
) : RouteRepository {

    override suspend fun calculateRoutes(
        origin: String,
        destination: String,
        selectedMode: TransportMode?
    ): Result<List<Route>> {
        val manager = engineManager ?: return fallbackRepository.calculateRoutes(origin, destination, selectedMode)

        if (!manager.isGraphAvailable()) {
            return fallbackRepository.calculateRoutes(origin, destination, selectedMode)
        }

        val originLatLng = parseCoordinates(origin)
        val destLatLng = parseCoordinates(destination)

        if (originLatLng == null || destLatLng == null) {
            // Se as strings de origem e destino não forem coordenadas numéricas, utiliza o fallback gracioso
            return fallbackRepository.calculateRoutes(origin, destination, selectedMode)
        }

        val result = manager.calculateRoute(originLatLng, destLatLng, selectedMode)
        return if (result.isSuccess && result.getOrNull()?.isNotEmpty() == true) {
            result
        } else {
            fallbackRepository.calculateRoutes(origin, destination, selectedMode)
        }
    }

    /**
     * Tenta analisar uma string no formato "latitude, longitude" ou "lat,lon".
     */
    private fun parseCoordinates(locationStr: String): LatLngData? {
        return try {
            val parts = locationStr.split(",")
            if (parts.size == 2) {
                val lat = parts[0].trim().toDoubleOrNull()
                val lon = parts[1].trim().toDoubleOrNull()
                if (lat != null && lon != null) {
                    LatLngData(lat, lon)
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
