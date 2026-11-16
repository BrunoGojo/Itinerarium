package com.itinerarium.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Corpo da requisição POST `v2/directions/{profile}/geojson` da API OpenRouteService (ORS).
 *
 * @property coordinates Lista de pontos no formato [longitude, latitude].
 * @property radiuses Raio máximo (em metros) para encaixar cada ponto na via mais próxima.
 *                    O valor -1 remove o limite (o padrão da ORS é apenas 350 m).
 * @property language Idioma das instruções.
 */
data class OrsDirectionsRequest(
    @SerializedName("coordinates") val coordinates: List<List<Double>>,
    @SerializedName("radiuses") val radiuses: List<Double>? = null,
    @SerializedName("language") val language: String = "pt",
    @SerializedName("instructions") val instructions: Boolean = true
)
