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
