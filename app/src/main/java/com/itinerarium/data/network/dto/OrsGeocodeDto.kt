package com.itinerarium.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Resposta GeoJSON retornada pelo endpoint `geocode/search` da API OpenRouteService (ORS).
 */
data class OrsGeocodeResponse(
    @SerializedName("features") val features: List<OrsGeocodeFeature>
)

data class OrsGeocodeFeature(
    @SerializedName("geometry") val geometry: OrsGeometry,
    @SerializedName("properties") val properties: OrsGeocodeProperties
)

data class OrsGeocodeProperties(
    @SerializedName("label") val label: String,
    @SerializedName("name") val name: String? = null
)
