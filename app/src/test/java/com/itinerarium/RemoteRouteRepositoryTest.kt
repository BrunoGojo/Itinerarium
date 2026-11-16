package com.itinerarium

import com.itinerarium.data.model.TransportMode
import com.itinerarium.data.network.NominatimApiService
import com.itinerarium.data.network.RouteApiService
import com.itinerarium.data.network.dto.NominatimPlaceDto
import com.itinerarium.data.network.dto.OrsDirectionsRequest
import com.itinerarium.data.network.dto.OrsDirectionsResponse
import com.itinerarium.data.network.dto.OrsFeature
import com.itinerarium.data.network.dto.OrsGeocodeFeature
import com.itinerarium.data.network.dto.OrsGeocodeProperties
import com.itinerarium.data.network.dto.OrsGeocodeResponse
import com.itinerarium.data.network.dto.OrsGeometry
import com.itinerarium.data.network.dto.OrsProperties
import com.itinerarium.data.network.dto.OrsSegment
import com.itinerarium.data.network.dto.OrsStep
import com.itinerarium.data.network.dto.OrsSummary
import com.itinerarium.data.repository.MockRouteRepository
import com.itinerarium.data.repository.RemoteRouteRepositoryImpl
import com.itinerarium.data.repository.RepositoryProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RemoteRouteRepositoryTest {

    private class FakeRouteApiService(
        private val geocodeSuccess: Boolean = true
    ) : RouteApiService {
        var lastLanguage: String? = null

        override suspend fun getDirections(
            profile: String,
            apiKey: String,
            body: OrsDirectionsRequest
        ): Response<OrsDirectionsResponse> {
            lastLanguage = body.language
            val summary = OrsSummary(distance = 5000.0, duration = 600.0) // 5 km, 10 min
            val steps = listOf(
                OrsStep(distance = 2500.0, duration = 300.0, instruction = "Siga em frente na Av Paulista", name = "Av Paulista"),
                OrsStep(distance = 2500.0, duration = 300.0, instruction = "Vire à direita na Rua Consolação", name = "Rua Consolação")
            )
            val segments = listOf(OrsSegment(distance = 5000.0, duration = 600.0, steps = steps))
            val properties = OrsProperties(summary = summary, segments = segments)
            val geometry = OrsGeometry(
                coordinates = listOf(
                    listOf(-46.655881, -23.561414),
                    listOf(-46.633308, -23.550520)
                )
            )
            val feature = OrsFeature(geometry = geometry, properties = properties)
            val responseDto = OrsDirectionsResponse(features = listOf(feature))
            return Response.success(responseDto)
        }

        override suspend fun searchGeocode(
            apiKey: String,
            text: String,
            size: Int
        ): Response<OrsGeocodeResponse> {
            return if (geocodeSuccess) {
                val geometry = OrsGeometry(coordinates = listOf(listOf(-46.655881, -23.561414)))
                val properties = OrsGeocodeProperties(label = "Avenida Paulista, São Paulo", name = "Avenida Paulista")
                val feature = OrsGeocodeFeature(geometry = geometry, properties = properties)
                Response.success(OrsGeocodeResponse(features = listOf(feature)))
            } else {
                Response.success(OrsGeocodeResponse(features = emptyList()))
            }
        }
    }

    private class FakeNominatimApiService(
        private val shouldReturnPlace: Boolean = true,
        private val lat: String = "-23.550520",
        private val lon: String = "-46.633308"
    ) : NominatimApiService {
        override suspend fun search(
            query: String,
            format: String,
            limit: Int,
            countryCodes: String,
            language: String,
            addressDetails: Int,
            userAgent: String
        ): Response<List<NominatimPlaceDto>> {
            return if (shouldReturnPlace) {
                val place = NominatimPlaceDto(
                    placeId = 1001L,
                    lat = lat,
                    lon = lon,
                    displayName = "Resultado Nominatim para $query"
                )
                Response.success(listOf(place))
            } else {
                Response.success(emptyList())
            }
        }
    }

    @Test
    fun mapTransportModeToProfile_returnsCorrectOrsProfiles() {
        val repository = RemoteRouteRepositoryImpl(FakeRouteApiService(), FakeNominatimApiService(), "fake_key")

        assertEquals("driving-car", repository.mapTransportModeToProfile(TransportMode.CAR))
        assertEquals("driving-car", repository.mapTransportModeToProfile(TransportMode.ELECTRIC_CAR))
        assertEquals("driving-car", repository.mapTransportModeToProfile(TransportMode.MOTORCYCLE))
        assertEquals("driving-car", repository.mapTransportModeToProfile(TransportMode.ELECTRIC_MOTORCYCLE))
        assertEquals("cycling-regular", repository.mapTransportModeToProfile(TransportMode.BICYCLE))
        assertEquals("cycling-regular", repository.mapTransportModeToProfile(TransportMode.ELECTRIC_BICYCLE))
        assertEquals("foot-walking", repository.mapTransportModeToProfile(TransportMode.WALKING))
    }

    @Test
    fun calculateRoutes_convertsOrsDtoToDomainRoutesSuccessfully() = runTest {
        val fakeRouteService = FakeRouteApiService()
        val repository = RemoteRouteRepositoryImpl(fakeRouteService, FakeNominatimApiService(), "test_api_key")

        val result = repository.calculateRoutes(
            origin = "São Paulo",
            destination = "Campinas",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isSuccess)
        val routes = result.getOrNull()
        assertNotNull(routes)
        assertTrue(routes!!.isNotEmpty())

        val carRoute = routes.first { it.transportMode == TransportMode.CAR }
        assertEquals(5.0, carRoute.totalDistanceKm, 0.01)
        assertEquals(10, carRoute.totalDurationMinutes)
        assertEquals(2, carRoute.pathPoints.size)
        // Coordenadas lon, lat -> lat, lon
        assertEquals(-23.561414, carRoute.pathPoints[0].latitude, 0.0001)
        assertEquals(-46.655881, carRoute.pathPoints[0].longitude, 0.0001)
        assertTrue(carRoute.segments.isNotEmpty())
        assertEquals("pt", fakeRouteService.lastLanguage)
    }

    @Test
    fun calculateRoutes_usesNominatimWhenOrsGeocodeFails() = runTest {
        val orsService = FakeRouteApiService(geocodeSuccess = false)
        val nominatimService = FakeNominatimApiService(shouldReturnPlace = true)
        val repository = RemoteRouteRepositoryImpl(orsService, nominatimService, "test_api_key")

        val result = repository.calculateRoutes(
            origin = "Rua Desconhecida ORS 1",
            destination = "Rua Desconhecida ORS 2",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isSuccess)
        val routes = result.getOrNull()
        assertNotNull(routes)
        assertTrue(routes!!.isNotEmpty())
    }

    @Test
    fun calculateRoutes_failsWhenBothOrsAndNominatimGeocodeFail() = runTest {
        val orsService = FakeRouteApiService(geocodeSuccess = false)
        val nominatimService = FakeNominatimApiService(shouldReturnPlace = false)
        val repository = RemoteRouteRepositoryImpl(orsService, nominatimService, "test_api_key")

        val result = repository.calculateRoutes(
            origin = "Local Inexistente 1",
            destination = "Local Inexistente 2",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isFailure)
    }

    private class FakeRoutableErrorRouteApiService : RouteApiService {
        override suspend fun getDirections(
            profile: String,
            apiKey: String,
            body: OrsDirectionsRequest
        ): Response<OrsDirectionsResponse> {
            val errorBody = okhttp3.ResponseBody.create(
                null,
                "{\"error\":{\"code\":2010,\"message\":\"Could not find routable point 1: ...\"}}"
            )
            return Response.error(400, errorBody)
        }

        override suspend fun searchGeocode(
            apiKey: String,
            text: String,
            size: Int
        ): Response<OrsGeocodeResponse> {
            val geometry = OrsGeometry(coordinates = listOf(listOf(-38.97, -8.61)))
            val properties = OrsGeocodeProperties(label = "Floresta, PE", name = "Floresta")
            val feature = OrsGeocodeFeature(geometry = geometry, properties = properties)
            return Response.success(OrsGeocodeResponse(features = listOf(feature)))
        }
    }

    @Test
    fun calculateRoutes_returnsFriendlyMessageOnRoutablePointError() = runTest {
        val orsService = FakeRoutableErrorRouteApiService()
        val repository = RemoteRouteRepositoryImpl(orsService, FakeNominatimApiService(), "test_api_key")

        val result = repository.calculateRoutes(
            origin = "Floresta, PE",
            destination = "Recife, PE",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("Não foi possível traçar rota para este ponto. Tente inserir um endereço, bairro ou rua mais específico"))
    }

    private class FakeRoutable404RouteApiService : RouteApiService {
        var lastBody: OrsDirectionsRequest? = null

        override suspend fun getDirections(
            profile: String,
            apiKey: String,
            body: OrsDirectionsRequest
        ): Response<OrsDirectionsResponse> {
            lastBody = body
            val errorBody = okhttp3.ResponseBody.create(
                null,
                "{\"error\":{\"code\":2010,\"message\":\"Could not find routable point within a radius of 350.0 meters\"}}"
            )
            return Response.error(404, errorBody)
        }

        override suspend fun searchGeocode(
            apiKey: String,
            text: String,
            size: Int
        ): Response<OrsGeocodeResponse> {
            val geometry = OrsGeometry(coordinates = listOf(listOf(-38.45, -8.63)))
            val properties = OrsGeocodeProperties(label = "Teste", name = "Teste")
            val feature = OrsGeocodeFeature(geometry = geometry, properties = properties)
            return Response.success(OrsGeocodeResponse(features = listOf(feature)))
        }
    }

    @Test
    fun calculateRoutes_treatsHttp404Code2010AsRoutablePointError_andSendsUnlimitedRadiuses() = runTest {
        val orsService = FakeRoutable404RouteApiService()
        val repository = RemoteRouteRepositoryImpl(orsService, FakeNominatimApiService(), "test_api_key")

        val result = repository.calculateRoutes(
            origin = "Floresta, PE",
            destination = "Recife, PE",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Não foi possível traçar rota para este ponto"))
        assertEquals(listOf(-1.0, -1.0), orsService.lastBody?.radiuses)
    }

    private class FakeRefiningRouteApiService : RouteApiService {
        var callCount = 0

        override suspend fun getDirections(
            profile: String,
            apiKey: String,
            body: OrsDirectionsRequest
        ): Response<OrsDirectionsResponse> {
            callCount++
            if (callCount == 1) {
                val errorBody = okhttp3.ResponseBody.create(
                    null,
                    "{\"error\":{\"code\":2010,\"message\":\"Could not find routable point\"}}"
                )
                return Response.error(400, errorBody)
            } else {
                val summary = OrsSummary(distance = 5000.0, duration = 600.0)
                val steps = listOf(
                    OrsStep(distance = 5000.0, duration = 600.0, instruction = "Siga em frente", name = "Rua Principal")
                )
                val segments = listOf(OrsSegment(distance = 5000.0, duration = 600.0, steps = steps))
                val properties = OrsProperties(summary = summary, segments = segments)
                val geometry = OrsGeometry(
                    coordinates = listOf(
                        listOf(-38.97, -8.61),
                        listOf(-34.87, -8.05)
                    )
                )
                val feature = OrsFeature(geometry = geometry, properties = properties)
                return Response.success(OrsDirectionsResponse(features = listOf(feature)))
            }
        }

        override suspend fun searchGeocode(
            apiKey: String,
            text: String,
            size: Int
        ): Response<OrsGeocodeResponse> {
            val geometry = OrsGeometry(coordinates = listOf(listOf(-38.97, -8.61)))
            val properties = OrsGeocodeProperties(label = text, name = text)
            val feature = OrsGeocodeFeature(geometry = geometry, properties = properties)
            return Response.success(OrsGeocodeResponse(features = listOf(feature)))
        }
    }

    @Test
    fun calculateRoutes_retriesAndSucceedsWithRefinementOnRoutablePointError() = runTest {
        val orsService = FakeRefiningRouteApiService()
        val repository = RemoteRouteRepositoryImpl(orsService, FakeNominatimApiService(), "test_api_key")

        val result = repository.calculateRoutes(
            origin = "Floresta, PE",
            destination = "Recife, PE",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isSuccess)
        val routes = result.getOrNull()
        assertNotNull(routes)
        assertTrue(routes!!.isNotEmpty())
    }

    @Test
    fun cleanLocationQuery_removesBrazilianMacroRegions() {
        val repository = RemoteRouteRepositoryImpl(FakeRouteApiService(), FakeNominatimApiService(), "fake_key")

        assertEquals("Floresta, Pernambuco", repository.cleanLocationQuery("Floresta, Região Nordeste, Pernambuco"))
        assertEquals("São Paulo, SP", repository.cleanLocationQuery("São Paulo, Região Sudeste, SP"))
        assertEquals("Curitiba, Paraná", repository.cleanLocationQuery("Curitiba, Região Sul, Paraná"))
        assertEquals("Manaus, Amazonas", repository.cleanLocationQuery("Manaus, Região Norte, Amazonas"))
        assertEquals("Cuiabá, Mato Grosso", repository.cleanLocationQuery("Cuiabá, Região Centro-Oeste, Mato Grosso"))
        assertEquals("Salvador, Bahia", repository.cleanLocationQuery("Salvador, Nordeste, Bahia"))
        assertEquals("Campinas", repository.cleanLocationQuery("Campinas"))
    }

    @Test
    fun repositoryProvider_returnsRemoteRouteRepositoryImpl() {
        RepositoryProvider.USE_MOCK = false
        val repo = RepositoryProvider.getRouteRepository()
        assertTrue(repo is RemoteRouteRepositoryImpl)
    }

    @Test
    fun repositoryProvider_useMockTrue_returnsMockRouteRepository() {
        RepositoryProvider.USE_MOCK = true
        val repo = RepositoryProvider.getRouteRepository()
        assertTrue(repo is MockRouteRepository)
    }
}
