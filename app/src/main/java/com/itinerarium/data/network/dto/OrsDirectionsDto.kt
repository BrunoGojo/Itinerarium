package com.itinerarium.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Resposta GeoJSON retornada pelo endpoint `v2/directions/{profile}` da API OpenRouteService (ORS).
 */
data class OrsDirectionsResponse(
    @SerializedName("features") val features: List<OrsFeature>
)

data class OrsFeature(
    @SerializedName("geometry") val geometry: OrsGeometry,
    @SerializedName("properties") val properties: OrsProperties
)

data class OrsGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>
)

data class OrsProperties(
    @SerializedName("summary") val summary: OrsSummary,
    @SerializedName("segments") val segments: List<OrsSegment>? = null
)

data class OrsSummary(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double
)

data class OrsSegment(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("steps") val steps: List<OrsStep>? = null
)

data class OrsStep(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("instruction") val instruction: String,
    @SerializedName("name") val name: String? = null
)
