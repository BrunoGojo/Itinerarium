package com.itinerarium.data.repository

import com.itinerarium.data.model.LatLngData
import com.itinerarium.data.model.Route
import com.itinerarium.data.model.RouteSegment
import com.itinerarium.data.model.TransportMode
import kotlinx.coroutines.delay

/**
 * Implementação mockada do repositório de rotas com dados enriquecidos e fidedignos.
 * Contém coordenadas polilinha realistas, instruções de navegação passo a passo,
 * custos estimados e dados de pegada de carbono (CO2) para múltiplos modais de transporte.
 */
class MockRouteRepository : RouteRepository {

    override suspend fun calculateRoutes(
        origin: String,
        destination: String,
        selectedMode: TransportMode?
    ): Result<List<Route>> {
        // Simula latência de rede realista para testes da UI no estado de carregamento
        delay(800)

        if (origin.isBlank() || destination.isBlank()) {
            return Result.failure(IllegalArgumentException("Origem e destino devem ser informados."))
        }

        val allRoutes = generateMockRoutes(origin, destination)

        val filteredRoutes = if (selectedMode != null) {
            // Se um modal específico foi selecionado, coloca essa rota em primeiro lugar
            // e mantém os demais como alternativas comparativas
            allRoutes.sortedByDescending { it.transportMode == selectedMode }
        } else {
            allRoutes
        }

        return Result.success(filteredRoutes)
    }

    private fun generateMockRoutes(origin: String, destination: String): List<Route> {
        // Coordenadas simuladas realistas de uma rota urbana (ex: Praça da Sé -> Avenida Paulista, São Paulo)
        val path1 = listOf(
            LatLngData(-23.550520, -46.633308), // Ponto Inicial (Praça da Sé)
            LatLngData(-23.552891, -46.636112), // Viaduto do Chá
            LatLngData(-23.555210, -46.640201), // R. da Consolação
            LatLngData(-23.558300, -46.647900), // Consolação x M. de Itu
            LatLngData(-23.561414, -46.655881)  // Ponto Final (Av. Paulista x Consolação)
        )

        val path2 = listOf(
            LatLngData(-23.550520, -46.633308),
            LatLngData(-23.553910, -46.638510), // Av. Brigadeiro Luís Antônio
            LatLngData(-23.559810, -46.649210), // Brigadeiro x Bela Cintra
            LatLngData(-23.561414, -46.655881)
        )

        val pathCycle = listOf(
            LatLngData(-23.550520, -46.633308),
            LatLngData(-23.551800, -46.634900), // Ciclofaixa Centro
            LatLngData(-23.554500, -46.642100), // Ciclovia Consolação
            LatLngData(-23.557900, -46.649900), // Ciclovia Paulista
            LatLngData(-23.561414, -46.655881)
        )

        val distanceKm = 4.8

        return listOf(
            // 1. CARRO ELÉTRICO (Eco-friendly, rápido e baixo custo operacional)
            Route(
                id = "route_electric_car",
                title = "Rota Ecológica Expressa ($origin → $destination)",
                transportMode = TransportMode.ELECTRIC_CAR,
                totalDistanceKm = distanceKm,
                totalDurationMinutes = TransportMode.ELECTRIC_CAR.estimateDurationMinutes(distanceKm),
                estimatedCost = TransportMode.ELECTRIC_CAR.calculateEstimatedCost(distanceKm),
                co2EmissionsKg = TransportMode.ELECTRIC_CAR.calculateCo2EmissionsKg(distanceKm),
                pathPoints = path1,
                segments = listOf(
                    RouteSegment(
                        id = "seg_ec_1",
                        instruction = "Siga em direção à R. da Consolação via Viaduto do Chá",
                        distanceKm = 2.3,
                        durationMinutes = 6,
                        transportMode = TransportMode.ELECTRIC_CAR,
                        startLatLng = path1[0],
                        endLatLng = path1[2]
                    ),
                    RouteSegment(
                        id = "seg_ec_2",
                        instruction = "Continue pela R. da Consolação até a Av. Paulista",
                        distanceKm = 2.5,
                        durationMinutes = 5,
                        transportMode = TransportMode.ELECTRIC_CAR,
                        startLatLng = path1[2],
                        endLatLng = path1[4]
                    )
                )
            ),

            // 2. CARRO A COMBUSTÃO (Rota Padrão)
            Route(
                id = "route_car",
                title = "Rota Direta por Vias Principais",
                transportMode = TransportMode.CAR,
                totalDistanceKm = 5.2,
                totalDurationMinutes = TransportMode.CAR.estimateDurationMinutes(5.2),
                estimatedCost = TransportMode.CAR.calculateEstimatedCost(5.2),
                co2EmissionsKg = TransportMode.CAR.calculateCo2EmissionsKg(5.2),
                pathPoints = path2,
                segments = listOf(
                    RouteSegment(
                        id = "seg_car_1",
                        instruction = "Siga pela Av. Brigadeiro Luís Antônio em direção à Paulista",
                        distanceKm = 3.2,
                        durationMinutes = 8,
                        transportMode = TransportMode.CAR,
                        startLatLng = path2[0],
                        endLatLng = path2[2]
                    ),
                    RouteSegment(
                        id = "seg_car_2",
                        instruction = "Vire à direita na Av. Paulista e siga até o destino",
                        distanceKm = 2.0,
                        durationMinutes = 5,
                        transportMode = TransportMode.CAR,
                        startLatLng = path2[2],
                        endLatLng = path2[3]
                    )
                )
            ),

            // 3. MOTO ELÉTRICA (Ágil e sustentável)
            Route(
                id = "route_electric_motorcycle",
                title = "Atalho Ágil Elétrico",
                transportMode = TransportMode.ELECTRIC_MOTORCYCLE,
                totalDistanceKm = 4.5,
                totalDurationMinutes = TransportMode.ELECTRIC_MOTORCYCLE.estimateDurationMinutes(4.5),
                estimatedCost = TransportMode.ELECTRIC_MOTORCYCLE.calculateEstimatedCost(4.5),
                co2EmissionsKg = TransportMode.ELECTRIC_MOTORCYCLE.calculateCo2EmissionsKg(4.5),
                pathPoints = path1,
                segments = listOf(
                    RouteSegment(
                        id = "seg_em_1",
                        instruction = "Trajeto otimizado por corredores urbanos e vias secundárias",
                        distanceKm = 4.5,
                        durationMinutes = 8,
                        transportMode = TransportMode.ELECTRIC_MOTORCYCLE,
                        startLatLng = path1[0],
                        endLatLng = path1[4]
                    )
                )
            ),

            // 4. MOTOCICLETA A COMBUSTÃO
            Route(
                id = "route_motorcycle",
                title = "Rota Ágil de Motocicleta",
                transportMode = TransportMode.MOTORCYCLE,
                totalDistanceKm = 4.6,
                totalDurationMinutes = TransportMode.MOTORCYCLE.estimateDurationMinutes(4.6),
                estimatedCost = TransportMode.MOTORCYCLE.calculateEstimatedCost(4.6),
                co2EmissionsKg = TransportMode.MOTORCYCLE.calculateCo2EmissionsKg(4.6),
                pathPoints = path2,
                segments = listOf(
                    RouteSegment(
                        id = "seg_moto_1",
                        instruction = "Siga pelo corredor de motos da Av. Brigadeiro",
                        distanceKm = 4.6,
                        durationMinutes = 8,
                        transportMode = TransportMode.MOTORCYCLE,
                        startLatLng = path2[0],
                        endLatLng = path2[3]
                    )
                )
            ),

            // 5. BICICLETA ELÉTRICA (Pedal assistido em ciclovia)
            Route(
                id = "route_electric_bicycle",
                title = "Ciclovia Segura com E-Bike",
                transportMode = TransportMode.ELECTRIC_BICYCLE,
                totalDistanceKm = 4.2,
                totalDurationMinutes = TransportMode.ELECTRIC_BICYCLE.estimateDurationMinutes(4.2),
                estimatedCost = TransportMode.ELECTRIC_BICYCLE.calculateEstimatedCost(4.2),
                co2EmissionsKg = TransportMode.ELECTRIC_BICYCLE.calculateCo2EmissionsKg(4.2),
                pathPoints = pathCycle,
                segments = listOf(
                    RouteSegment(
                        id = "seg_ebike_1",
                        instruction = "Acesse a ciclovia protegida na R. da Consolação",
                        distanceKm = 2.5,
                        durationMinutes = 7,
                        transportMode = TransportMode.ELECTRIC_BICYCLE,
                        startLatLng = pathCycle[0],
                        endLatLng = pathCycle[2]
                    ),
                    RouteSegment(
                        id = "seg_ebike_2",
                        instruction = "Suba pela ciclovia da Av. Paulista utilizando o motor elétrico assistido",
                        distanceKm = 1.7,
                        durationMinutes = 5,
                        transportMode = TransportMode.ELECTRIC_BICYCLE,
                        startLatLng = pathCycle[2],
                        endLatLng = pathCycle[4]
                    )
                )
            ),

            // 6. BICICLETA CONVENCIONAL (Zero emissão, custo R$0,00)
            Route(
                id = "route_bicycle",
                title = "Trajeto Segregado em Ciclovia",
                transportMode = TransportMode.BICYCLE,
                totalDistanceKm = 4.2,
                totalDurationMinutes = TransportMode.BICYCLE.estimateDurationMinutes(4.2),
                estimatedCost = 0.0,
                co2EmissionsKg = 0.0,
                pathPoints = pathCycle,
                segments = listOf(
                    RouteSegment(
                        id = "seg_bike_1",
                        instruction = "Siga 100% por ciclovias e ciclofaixas protegidas do tráfego",
                        distanceKm = 4.2,
                        durationMinutes = 16,
                        transportMode = TransportMode.BICYCLE,
                        startLatLng = pathCycle[0],
                        endLatLng = pathCycle[4]
                    )
                )
            ),

            // 7. CAMINHADA (Saudável e Ativa)
            Route(
                id = "route_walking",
                title = "Caminhada Segura para Pedestres",
                transportMode = TransportMode.WALKING,
                totalDistanceKm = 3.8,
                totalDurationMinutes = TransportMode.WALKING.estimateDurationMinutes(3.8),
                estimatedCost = 0.0,
                co2EmissionsKg = 0.0,
                pathPoints = path1,
                segments = listOf(
                    RouteSegment(
                        id = "seg_walk_1",
                        instruction = "Caminhe pelas calçadas arborizadas e travessias de pedestres com semáforo",
                        distanceKm = 3.8,
                        durationMinutes = 46,
                        transportMode = TransportMode.WALKING,
                        startLatLng = path1[0],
                        endLatLng = path1[4]
                    )
                )
            )
        )
    }
}
