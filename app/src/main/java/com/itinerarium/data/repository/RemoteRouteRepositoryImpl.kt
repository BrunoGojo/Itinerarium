package com.itinerarium.data.repository

import com.itinerarium.BuildConfig
import com.itinerarium.data.model.LatLngData
import com.itinerarium.data.model.Route
import com.itinerarium.data.model.RouteSegment
import com.itinerarium.data.model.TransportMode
import com.itinerarium.data.network.ApiClient
import com.itinerarium.data.network.NominatimApiService
import com.itinerarium.data.network.RouteApiService
import com.itinerarium.data.network.dto.OrsStep
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Implementação remota do repositório de rotas utilizando a API OpenRouteService (ORS)
 * e a API do Nominatim (OpenStreetMap) para geocodificação de endereços.
 */
class RemoteRouteRepositoryImpl(
    private val apiService: RouteApiService,
    private val nominatimApiService: NominatimApiService = ApiClient.nominatimApiService,
    private val apiKey: String = BuildConfig.ORS_API_KEY
) : RouteRepository {

    override suspend fun calculateRoutes(
        origin: String,
        destination: String,
        selectedMode: TransportMode?
    ): Result<List<Route>> {
        if (origin.isBlank() || destination.isBlank()) {
            return Result.failure(IllegalArgumentException("Origem e destino devem ser informados."))
        }

        val cleanedOrigin = cleanLocationQuery(origin).ifBlank { origin }
        val cleanedDestination = cleanLocationQuery(destination).ifBlank { destination }

        val originVariations = getQueryVariations(cleanedOrigin)
        val destinationVariations = getQueryVariations(cleanedDestination)
        val maxAttempts = maxOf(originVariations.size, destinationVariations.size)

        var lastError: Throwable? = null

        for (i in 0 until maxAttempts) {
            val currentOriginQuery = originVariations.getOrElse(i) { originVariations.last() }
            val currentDestinationQuery = destinationVariations.getOrElse(i) { destinationVariations.last() }

            val startCoords = resolveCoordinates(currentOriginQuery)
            val endCoords = resolveCoordinates(currentDestinationQuery)

            if (startCoords == null || endCoords == null) {
                if (i == maxAttempts - 1) {
                    return Result.failure(
                        Exception("Não foi possível localizar o endereço informado para origem ou destino. Verifique os dados digitados ou sua conexão.")
                    )
                }
                continue
            }

            val primaryMode = selectedMode ?: TransportMode.CAR
            val profile = mapTransportModeToProfile(primaryMode)

            try {
                val response = apiService.getDirections(
                    profile = profile,
                    apiKey = apiKey,
                    start = startCoords,
                    end = endCoords
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val feature = body?.features?.firstOrNull()
                    if (feature != null) {
                        val coordinates = feature.geometry.coordinates
                        val pathPoints = coordinates.map { coord ->
                            LatLngData(latitude = coord[1], longitude = coord[0])
                        }

                        val summary = feature.properties.summary
                        val distanceKm = summary.distance / 1000.0
                        val durationMinutes = kotlin.math.max(1, (summary.duration / 60.0).roundToInt())

                        val segmentsDto = feature.properties.segments
                        val rawSteps = segmentsDto?.flatMap { it.steps ?: emptyList() } ?: emptyList()

                        val routes = TransportMode.entries.map { mode ->
                            val modeProfile = mapTransportModeToProfile(mode)
                            val modeDuration = if (modeProfile == profile) {
                                durationMinutes
                            } else {
                                mode.estimateDurationMinutes(distanceKm)
                            }

                            val segments = if (rawSteps.isNotEmpty()) {
                                mapOrsStepsToRouteSegments(rawSteps, pathPoints, mode)
                            } else {
                                listOf(
                                    RouteSegment(
                                        id = "seg_${mode.name.lowercase()}_1",
                                        instruction = "Siga da origem até o destino",
                                        distanceKm = distanceKm,
                                        durationMinutes = modeDuration,
                                        transportMode = mode,
                                        startLatLng = pathPoints.firstOrNull() ?: LatLngData(0.0, 0.0),
                                        endLatLng = pathPoints.lastOrNull() ?: LatLngData(0.0, 0.0)
                                    )
                                )
                            }

                            Route(
                                id = "ors_route_${mode.name.lowercase()}",
                                title = "Rota via ORS (${mode.label})",
                                transportMode = mode,
                                totalDistanceKm = distanceKm,
                                totalDurationMinutes = modeDuration,
                                estimatedCost = mode.calculateEstimatedCost(distanceKm),
                                co2EmissionsKg = mode.calculateCo2EmissionsKg(distanceKm),
                                pathPoints = pathPoints,
                                segments = segments
                            )
                        }

                        val sortedRoutes = if (selectedMode != null) {
                            routes.sortedByDescending { it.transportMode == selectedMode }
                        } else {
                            routes
                        }

                        return Result.success(sortedRoutes)
                    } else {
                        lastError = Exception("Nenhuma rota encontrada para o trajeto solicitado.")
                    }
                } else {
                    val code = response.code()
                    val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido no servidor ORS."
                    if (isRoutablePointError(code, errorMsg)) {
                        lastError = Exception("Não foi possível traçar rota para este ponto. Tente inserir um endereço, bairro ou rua mais específico do que apenas o nome da cidade.")
                        if (i < maxAttempts - 1) continue
                    } else {
                        return Result.failure(Exception("Falha na API OpenRouteService (Código $code): $errorMsg"))
                    }
                }
            } catch (e: IOException) {
                return Result.failure(
                    Exception("Falha de conexão com a internet ao consultar o OpenRouteService. Verifique sua rede.", e)
                )
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: e.message()
                val code = e.code()
                if (isRoutablePointError(code, errorBody)) {
                    lastError = Exception("Não foi possível traçar rota para este ponto. Tente inserir um endereço, bairro ou rua mais específico do que apenas o nome da cidade.")
                    if (i < maxAttempts - 1) continue
                } else {
                    return Result.failure(
                        Exception("Erro HTTP na comunicação com o OpenRouteService (${e.code()}): ${e.message()}", e)
                    )
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: ""
                if (isRoutablePointError(400, msg)) {
                    lastError = Exception("Não foi possível traçar rota para este ponto. Tente inserir um endereço, bairro ou rua mais específico do que apenas o nome da cidade.")
                    if (i < maxAttempts - 1) continue
                } else {
                    return Result.failure(
                        Exception("Erro inesperado ao calcular rota online: ${e.localizedMessage ?: "Erro desconhecido"}", e)
                    )
                }
            }
        }

        return Result.failure(
            lastError ?: Exception("Não foi possível traçar rota para este ponto. Tente inserir um endereço, bairro ou rua mais específico do que apenas o nome da cidade.")
        )
    }

    private fun isRoutablePointError(code: Int, message: String): Boolean {
        return code == 400 && (
            message.contains("routable", ignoreCase = true) ||
            message.contains("2010", ignoreCase = true) ||
            message.contains("Could not find routable point", ignoreCase = true)
        )
    }

    private fun getQueryVariations(location: String): List<String> {
        val trimmed = location.trim()
        if (trimmed.isBlank()) return emptyList()

        if (parseRawCoordinates(trimmed) != null) {
            return listOf(trimmed)
        }

        val variations = mutableListOf<String>()
        variations.add(trimmed)

        if (!trimmed.contains("rua", ignoreCase = true) &&
            !trimmed.contains("centro", ignoreCase = true) &&
            !trimmed.contains("avenida", ignoreCase = true) &&
            !trimmed.contains("travessa", ignoreCase = true) &&
            !trimmed.contains("estrada", ignoreCase = true)) {
            variations.add("$trimmed rua principal")
            variations.add("$trimmed centro")
        } else {
            variations.add("$trimmed centro")
        }

        return variations.distinct()
    }

    /**
     * Remove termos de macro-regiões do Brasil que causam confusão na geocodificação e no roteamento,
     * limpando vírgulas duplicadas, espaços em excesso e pontuações sobrando.
     */
    fun cleanLocationQuery(query: String): String {
        if (query.isBlank()) return query
        val macroRegionRegex = Regex("(?i)\\b(regi[ãa]o\\s+)?(nordeste|sudeste|sul|norte|centro[- ]oeste)\\b")
        var cleaned = macroRegionRegex.replace(query, "")
        cleaned = cleaned.replace(Regex(",\\s*,"), ",")
        cleaned = cleaned.replace(Regex("\\s+"), " ")
        cleaned = cleaned.replace(Regex("^[,\\s]+|[,\\s]+$"), "")
        cleaned = cleaned.replace(Regex(",\\s*,"), ",")
        return cleaned.trim()
    }

    /**
     * Mapeia um [TransportMode] para o perfil correspondente na API OpenRouteService.
     */
    fun mapTransportModeToProfile(mode: TransportMode): String {
        return when (mode) {
            TransportMode.CAR,
            TransportMode.ELECTRIC_CAR,
            TransportMode.MOTORCYCLE,
            TransportMode.ELECTRIC_MOTORCYCLE -> "driving-car"
            TransportMode.BICYCLE,
            TransportMode.ELECTRIC_BICYCLE -> "cycling-regular"
            TransportMode.WALKING -> "foot-walking"
        }
    }

    /**
     * Resolve uma string de localização (nome de cidade/local ou coordenadas brutas)
     * retornando uma string no formato "longitude,latitude" aceito pelo ORS.
     */
    private suspend fun resolveCoordinates(location: String): String? {
        val trimmed = location.trim()
        if (trimmed.isBlank()) return null

        // 1. Tenta analisar como coordenadas brutas (lat,lon ou lon,lat)
        val rawCoords = parseRawCoordinates(trimmed)
        if (rawCoords != null) {
            return rawCoords
        }

        // 2. Tenta Geocoding via ORS API
        try {
            val response = apiService.searchGeocode(apiKey = apiKey, text = trimmed, size = 1)
            if (response.isSuccessful) {
                val features = response.body()?.features
                if (!features.isNullOrEmpty()) {
                    val coords = features.first().geometry.coordinates
                    if (coords.size >= 2) {
                        val lon = coords[0]
                        val lat = coords[1]
                        return "$lon,$lat"
                    }
                }
            }
        } catch (_: Exception) {
            // Segue para o fallback via Nominatim API
        }

        // 3. Fallback para Nominatim API (OpenStreetMap)
        return resolveViaNominatim(trimmed)
    }

    private fun parseRawCoordinates(text: String): String? {
        val parts = text.split(",")
        if (parts.size == 2) {
            val d1 = parts[0].trim().toDoubleOrNull()
            val d2 = parts[1].trim().toDoubleOrNull()
            if (d1 != null && d2 != null) {
                // Checa se d1 é latitude (-90 a 90) e d2 é longitude (-180 a 180)
                if (d1 in -90.0..90.0 && d2 in -180.0..180.0 && (d1 !in -180.0..-90.0 && d1 !in 90.0..180.0)) {
                    // Formato lat, lon -> ORS quer lon, lat
                    return "$d2,$d1"
                }
                // Se já estiver em lon, lat
                if (d1 in -180.0..180.0 && d2 in -90.0..90.0) {
                    return "$d1,$d2"
                }
            }
        }
        return null
    }

    private suspend fun resolveViaNominatim(query: String): String? {
        return try {
            val response = nominatimApiService.search(query = query, format = "json", limit = 1)
            if (response.isSuccessful) {
                val place = response.body()?.firstOrNull()
                if (place != null && place.lat.isNotBlank() && place.lon.isNotBlank()) {
                    val latDouble = place.lat.toDoubleOrNull()
                    val lonDouble = place.lon.toDoubleOrNull()
                    if (latDouble != null && lonDouble != null) {
                        "$lonDouble,$latDouble"
                    } else null
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun mapOrsStepsToRouteSegments(
        steps: List<OrsStep>,
        pathPoints: List<LatLngData>,
        mode: TransportMode
    ): List<RouteSegment> {
        val count = steps.size
        return steps.mapIndexed { index, step ->
            val stepDist = step.distance / 1000.0
            val stepDur = kotlin.math.max(1, (step.duration / 60.0).roundToInt())
            val instructionText = if (step.instruction.isNotBlank()) {
                step.instruction
            } else if (!step.name.isNullOrBlank()) {
                "Siga por ${step.name}"
            } else {
                "Trecho ${index + 1}"
            }

            val startIndex = ((index.toDouble() / count) * pathPoints.size).toInt().coerceIn(0, pathPoints.size - 1)
            val endIndex = (((index + 1).toDouble() / count) * pathPoints.size).toInt().coerceIn(0, pathPoints.size - 1)

            val startPt = pathPoints.getOrNull(startIndex) ?: LatLngData(0.0, 0.0)
            val endPt = pathPoints.getOrNull(endIndex) ?: LatLngData(0.0, 0.0)

            RouteSegment(
                id = "step_${mode.name.lowercase()}_$index",
                instruction = instructionText,
                distanceKm = stepDist,
                durationMinutes = stepDur,
                transportMode = mode,
                startLatLng = startPt,
                endLatLng = endPt
            )
        }
    }
}
