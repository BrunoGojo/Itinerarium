package com.itinerarium.data.model

/**
 * Enum representativo dos modais de transporte suportados pelo aplicativo Itinerarium.
 * Contém informações sobre rótulo, descrição/ícone, velocidade média, emissão relativa de CO2 e custo aproximado por km.
 */
enum class TransportMode(
    val label: String,
    val description: String,
    val avgSpeedKmH: Double,
    val co2EmissionsKgPerKm: Double,
    val costPerKm: Double,
    val isEcoFriendly: Boolean
) {
    CAR(
        label = "Carro",
        description = "Automóvel a combustão (Gasolina/Etanol)",
        avgSpeedKmH = 45.0,
        co2EmissionsKgPerKm = 0.120,
        costPerKm = 0.70,
        isEcoFriendly = false
    ),
    ELECTRIC_CAR(
        label = "Carro Elétrico",
        description = "Automóvel 100% elétrico (Zero emissões diretas)",
        avgSpeedKmH = 45.0,
        co2EmissionsKgPerKm = 0.015,
        costPerKm = 0.25,
        isEcoFriendly = true
    ),
    MOTORCYCLE(
        label = "Moto",
        description = "Motocicleta a combustão",
        avgSpeedKmH = 40.0,
        co2EmissionsKgPerKm = 0.065,
        costPerKm = 0.35,
        isEcoFriendly = false
    ),
    ELECTRIC_MOTORCYCLE(
        label = "Moto Elétrica",
        description = "Scooter ou moto elétrica",
        avgSpeedKmH = 38.0,
        co2EmissionsKgPerKm = 0.008,
        costPerKm = 0.12,
        isEcoFriendly = true
    ),
    BICYCLE(
        label = "Bicicleta",
        description = "Bicicleta convencional - Custo zero",
        avgSpeedKmH = 16.0,
        co2EmissionsKgPerKm = 0.0,
        costPerKm = 0.0,
        isEcoFriendly = true
    ),
    ELECTRIC_BICYCLE(
        label = "Bicicleta Elétrica",
        description = "E-bike com pedal assistido",
        avgSpeedKmH = 22.0,
        co2EmissionsKgPerKm = 0.003,
        costPerKm = 0.04,
        isEcoFriendly = true
    ),
    WALKING(
        label = "Caminhada",
        description = "A pé - Mobilidade ativa saudável",
        avgSpeedKmH = 5.0,
        co2EmissionsKgPerKm = 0.0,
        costPerKm = 0.0,
        isEcoFriendly = true
    );

    /**
     * Calcula o tempo estimado em minutos para uma determinada distância em km.
     */
    fun estimateDurationMinutes(distanceKm: Double): Int {
        if (avgSpeedKmH <= 0) return 0
        val hours = distanceKm / avgSpeedKmH
        return kotlin.math.max(1, kotlin.math.round(hours * 60).toInt())
    }

    /**
     * Calcula a emissão de CO2 em kg para uma dada distância em km.
     */
    fun calculateCo2EmissionsKg(distanceKm: Double): Double {
        return distanceKm * co2EmissionsKgPerKm
    }

    /**
     * Calcula o custo estimado em BRL para uma dada distância em km.
     */
    fun calculateEstimatedCost(distanceKm: Double): Double {
        return distanceKm * costPerKm
    }
}
