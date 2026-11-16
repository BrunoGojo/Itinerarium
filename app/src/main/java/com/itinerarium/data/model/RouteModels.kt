package com.itinerarium.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa coordenadas geográficas (latitude e longitude) independentes da API do Google Maps.
 */
data class LatLngData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

/**
 * Representa um segmento ou trecho individual de uma rota.
 */
data class RouteSegment(
    @SerializedName("id") val id: String,
    @SerializedName("instruction") val instruction: String,
    @SerializedName("distanceKm") val distanceKm: Double,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("transportMode") val transportMode: TransportMode,
    @SerializedName("startLatLng") val startLatLng: LatLngData,
    @SerializedName("endLatLng") val endLatLng: LatLngData
)

/**
 * Representa uma rota completa calculada entre origem e destino para um determinado modal de transporte.
 */
data class Route(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("transportMode") val transportMode: TransportMode,
    @SerializedName("totalDistanceKm") val totalDistanceKm: Double,
    @SerializedName("totalDurationMinutes") val totalDurationMinutes: Int,
    @SerializedName("estimatedCost") val estimatedCost: Double,
    @SerializedName("co2EmissionsKg") val co2EmissionsKg: Double,
    @SerializedName("pathPoints") val pathPoints: List<LatLngData>,
    @SerializedName("segments") val segments: List<RouteSegment>
)

/**
 * Objeto de requisição enviado à API de rotas para cálculo do itinerário.
 */
data class RouteRequest(
    @SerializedName("origin") val origin: String,
    @SerializedName("destination") val destination: String,
    @SerializedName("selectedMode") val selectedMode: TransportMode? = null
)
