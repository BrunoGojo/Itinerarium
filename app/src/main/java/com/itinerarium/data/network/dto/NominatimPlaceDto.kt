package com.itinerarium.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object que representa um local retornado pela API do Nominatim (OpenStreetMap).
 */
data class NominatimPlaceDto(
    @SerializedName("place_id") val placeId: Long? = null,
    @SerializedName("lat") val lat: String = "",
    @SerializedName("lon") val lon: String = "",
    @SerializedName("display_name") val displayName: String? = null
)

fun NominatimPlaceDto.toCleanDisplayName(): String {
    val raw = displayName ?: return ""
    val parts = raw.split(",").map { it.trim() }
    val macroRegions = setOf(
        "Região Nordeste", "Região Sudeste", "Região Sul", "Região Norte", "Região Centro-Oeste",
        "Nordeste", "Sudeste", "Centro-Oeste", "Sul", "Norte"
    )
    val filteredParts = parts.filter { part ->
        if (part.equals("Brasil", ignoreCase = true) || part.equals("Brazil", ignoreCase = true)) return@filter false
        if (macroRegions.any { reg -> part.equals(reg, ignoreCase = true) }) return@filter false
        if (part.matches(Regex("\\d{5}-?\\d{3}"))) return@filter false
        true
    }
    if (filteredParts.isEmpty()) return raw
    return filteredParts.take(3).joinToString(", ")
}
