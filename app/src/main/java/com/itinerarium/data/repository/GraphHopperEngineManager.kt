package com.itinerarium.data.repository

import android.content.Context
import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.config.Profile
import com.graphhopper.util.Instruction
import com.graphhopper.util.InstructionList
import com.graphhopper.util.PointList
import com.itinerarium.data.model.LatLngData
import com.itinerarium.data.model.Route
import com.itinerarium.data.model.RouteSegment
import com.itinerarium.data.model.TransportMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Gerenciador responsável pela inicialização do motor offline GraphHopper
 * e cálculo de rotas usando dados do grafo armazenados localmente no dispositivo.
 */
class GraphHopperEngineManager(private val context: Context) {

    private var hopper: GraphHopper? = null
    private var isInitialized = false

    /**
     * Retorna a pasta onde os arquivos de grafo do GraphHopper devem estar salvos.
     * Procura primeiro em context.filesDir/graphhopper e depois em context.getExternalFilesDir(null)/graphhopper.
     */
    fun getGraphDirectory(): File {
        val internalDir = File(context.filesDir, "graphhopper")
        if (internalDir.exists() && (internalDir.list()?.isNotEmpty() == true)) {
            return internalDir
        }
        val externalDir = context.getExternalFilesDir(null)?.let { File(it, "graphhopper") }
        if (externalDir != null && externalDir.exists() && (externalDir.list()?.isNotEmpty() == true)) {
            return externalDir
        }
        return internalDir
    }

    /**
     * Verifica se os arquivos do grafo offline estão disponíveis no dispositivo.
     */
    fun isGraphAvailable(): Boolean {
        val dir = getGraphDirectory()
        return dir.exists() && (dir.list()?.isNotEmpty() == true)
    }

    /**
     * Inicializa o motor GraphHopper com perfis multimodais (car, bike, foot, motorcycle)
     * se os arquivos de dados do grafo existirem.
     */
    @Synchronized
    fun initializeEngine(): Boolean {
        if (isInitialized && hopper != null) return true
        if (!isGraphAvailable()) return false

        return try {
            val graphDir = getGraphDirectory()
            val gh = GraphHopper()
            gh.graphHopperLocation = graphDir.absolutePath

            // Configura perfis multimodais de transporte
            gh.profiles = listOf(
                Profile("car"),
                Profile("bike"),
                Profile("foot"),
                Profile("motorcycle")
            )

            gh.importOrLoad()
            hopper = gh
            isInitialized = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = false
            hopper = null
            false
        }
    }

    /**
     * Calcula as rotas offline para a origem e destino informados.
     * Executado em Dispatchers.IO.
     */
    suspend fun calculateRoute(
        origin: LatLngData,
        destination: LatLngData,
        mode: TransportMode? = null
    ): Result<List<Route>> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            val initialized = initializeEngine()
            if (!initialized || hopper == null) {
                return@withContext Result.failure(
                    IllegalStateException("Grafo do GraphHopper não encontrado ou não inicializado.")
                )
            }
        }

        val ghEngine = hopper ?: return@withContext Result.failure(
            IllegalStateException("Motor GraphHopper indisponível.")
        )

        val modesToCalculate = if (mode != null) {
            listOf(mode)
        } else {
            listOf(
                TransportMode.ELECTRIC_CAR,
                TransportMode.CAR,
                TransportMode.ELECTRIC_MOTORCYCLE,
                TransportMode.MOTORCYCLE,
                TransportMode.ELECTRIC_BICYCLE,
                TransportMode.BICYCLE,
                TransportMode.WALKING
            )
        }

        val routes = mutableListOf<Route>()

        for (transportMode in modesToCalculate) {
            try {
                val profileName = mapTransportModeToProfile(transportMode)
                val request = GHRequest(origin.latitude, origin.longitude, destination.latitude, destination.longitude)
                request.profile = profileName

                val response: GHResponse = ghEngine.route(request)

                if (!response.hasErrors()) {
                    val path = response.best
                    if (path != null) {
                        val route = convertResponseToRoute(path, origin, destination, transportMode)
                        routes.add(route)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (routes.isNotEmpty()) {
            Result.success(routes)
        } else {
            Result.failure(Exception("Não foi possível calcular rotas via GraphHopper."))
        }
    }

    /**
     * Mapeia o TransportMode para o nome do perfil configurado no GraphHopper.
     */
    private fun mapTransportModeToProfile(mode: TransportMode): String {
        return when (mode) {
            TransportMode.CAR, TransportMode.ELECTRIC_CAR -> "car"
            TransportMode.MOTORCYCLE, TransportMode.ELECTRIC_MOTORCYCLE -> "motorcycle"
            TransportMode.BICYCLE, TransportMode.ELECTRIC_BICYCLE -> "bike"
            TransportMode.WALKING -> "foot"
        }
    }

    /**
     * Converte o caminho retornado pelo GraphHopper (ResponsePath) para o modelo Route do app.
     */
    private fun convertResponseToRoute(
        path: com.graphhopper.ResponsePath,
        origin: LatLngData,
        destination: LatLngData,
        transportMode: TransportMode
    ): Route {
        val distanceKm = path.distance / 1000.0
        val durationMinutes = (path.time / 60000.0).toInt().coerceAtLeast(1)

        val pointsList: PointList = path.points
        val pathPoints = mutableListOf<LatLngData>()
        val totalPointsCount = pointsList.getPointCount()
        for (i in 0 until totalPointsCount) {
            pathPoints.add(LatLngData(pointsList.getLat(i), pointsList.getLon(i)))
        }

        val segments = mutableListOf<RouteSegment>()
        val instructionList: InstructionList = path.instructions
        val instructionsCount = instructionList.size
        for (i in 0 until instructionsCount) {
            val inst: Instruction = instructionList[i]
            val instPoints = inst.points
            val instPointsCount = instPoints.getPointCount()
            val startPt = if (instPointsCount > 0) {
                LatLngData(instPoints.getLat(0), instPoints.getLon(0))
            } else {
                pathPoints.firstOrNull() ?: origin
            }
            val endPt = if (instPointsCount > 0) {
                LatLngData(instPoints.getLat(instPointsCount - 1), instPoints.getLon(instPointsCount - 1))
            } else {
                pathPoints.lastOrNull() ?: destination
            }

            val segDistanceKm = inst.distance / 1000.0
            val segDurationMin = (inst.time / 60000.0).toInt().coerceAtLeast(1)

            val instText = inst.name?.ifBlank { "Siga em frente" } ?: "Siga em frente"

            segments.add(
                RouteSegment(
                    id = "gh_seg_${transportMode.name.lowercase()}_$i",
                    instruction = instText,
                    distanceKm = segDistanceKm,
                    durationMinutes = segDurationMin,
                    transportMode = transportMode,
                    startLatLng = startPt,
                    endLatLng = endPt
                )
            )
        }

        return Route(
            id = "gh_route_${transportMode.name.lowercase()}",
            title = "Rota ${transportMode.label} (Offline GraphHopper)",
            transportMode = transportMode,
            totalDistanceKm = distanceKm,
            totalDurationMinutes = durationMinutes,
            estimatedCost = transportMode.calculateEstimatedCost(distanceKm),
            co2EmissionsKg = transportMode.calculateCo2EmissionsKg(distanceKm),
            pathPoints = if (pathPoints.isNotEmpty()) pathPoints else listOf(origin, destination),
            segments = segments
        )
    }

    private fun PointList.getPointCount(): Int {
        return try {
            (this as Any).javaClass.getMethod("size").invoke(this) as Int
        } catch (_: Exception) {
            0
        }
    }
}
