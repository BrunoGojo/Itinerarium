package com.itinerarium

import com.itinerarium.data.network.NominatimApiService
import com.itinerarium.data.network.dto.NominatimPlaceDto
import com.itinerarium.data.repository.MockRouteRepository
import com.itinerarium.viewmodel.ItinerariumViewModel
import com.itinerarium.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ItinerariumViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockRouteRepository
    private lateinit var fakeNominatimService: FakeNominatimApiService
    private lateinit var viewModel: ItinerariumViewModel

    private class FakeNominatimApiService : NominatimApiService {
        override suspend fun search(
            query: String,
            format: String,
            limit: Int,
            countryCodes: String,
            language: String,
            addressDetails: Int,
            userAgent: String
        ): Response<List<NominatimPlaceDto>> {
            val place = NominatimPlaceDto(
                placeId = 100L,
                lat = "-8.0476",
                lon = "-34.8770",
                displayName = "Recife, PE, Brasil"
            )
            return Response.success(listOf(place))
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockRouteRepository()
        fakeNominatimService = FakeNominatimApiService()
        viewModel = ItinerariumViewModel(mockRepository, fakeNominatimService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        assertEquals(UiState.Idle, viewModel.uiState.value)
        assertTrue(viewModel.originSuggestions.value.isEmpty())
        assertTrue(viewModel.destinationSuggestions.value.isEmpty())
        assertNull(viewModel.selectedOriginCoords)
        assertNull(viewModel.selectedDestinationCoords)
    }

    @Test
    fun calculateRoute_emptyFields_returnsError() {
        viewModel.calculateRoute(origin = "", destination = "Avenida Paulista")
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "Por favor, preencha tanto a origem quanto o destino para calcular o itinerário.",
            (state as UiState.Error).message
        )
    }

    @Test
    fun calculateRoute_sameOriginAndDestination_returnsError() {
        viewModel.calculateRoute(origin = "Centro", destination = "centro")
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "A origem e o destino não podem ser idênticos. Por favor, informe locais diferentes.",
            (state as UiState.Error).message
        )
    }

    @Test
    fun calculateRoute_validInput_returnsSuccess() = runTest {
        viewModel.calculateRoute(origin = "Praça da Sé", destination = "Avenida Paulista")

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val successState = state as UiState.Success
        assertEquals("Praça da Sé", successState.origin)
        assertEquals("Avenida Paulista", successState.destination)
        assertTrue(successState.routes.isNotEmpty())
    }

    @Test
    fun onOriginQueryChanged_shortQuery_clearsSuggestions() = runTest {
        viewModel.onOriginQueryChanged("Rec")
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.originSuggestions.value.isNotEmpty())

        viewModel.onOriginQueryChanged("Re")
        assertTrue(viewModel.originSuggestions.value.isEmpty())
    }

    @Test
    fun onOriginQueryChanged_validQuery_fetchesSuggestionsAfterDebounce() = runTest {
        viewModel.onOriginQueryChanged("Recife")
        assertTrue(viewModel.originSuggestions.value.isEmpty())

        testDispatcher.scheduler.advanceTimeBy(250)
        assertTrue(viewModel.originSuggestions.value.isEmpty())

        testDispatcher.scheduler.advanceTimeBy(260)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.originSuggestions.value.size)
        assertEquals("Recife, PE, Brasil", viewModel.originSuggestions.value.first().displayName)
    }

    @Test
    fun onOriginSuggestionSelected_setsCoordsAndClearsSuggestions() = runTest {
        viewModel.onOriginQueryChanged("Recife")
        testDispatcher.scheduler.advanceTimeBy(550)
        testDispatcher.scheduler.advanceUntilIdle()

        val selectedPlace = viewModel.originSuggestions.value.first()
        viewModel.onOriginSuggestionSelected(selectedPlace)

        assertEquals("-34.8770,-8.0476", viewModel.selectedOriginCoords)
        assertTrue(viewModel.originSuggestions.value.isEmpty())
    }

    @Test
    fun onDestinationQueryChanged_validQuery_fetchesSuggestionsAfterDebounce() = runTest {
        viewModel.onDestinationQueryChanged("Olinda")
        testDispatcher.scheduler.advanceTimeBy(550)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.destinationSuggestions.value.size)
    }

    @Test
    fun onDestinationSuggestionSelected_setsCoordsAndClearsSuggestions() = runTest {
        viewModel.onDestinationQueryChanged("Olinda")
        testDispatcher.scheduler.advanceTimeBy(550)
        testDispatcher.scheduler.advanceUntilIdle()

        val selectedPlace = viewModel.destinationSuggestions.value.first()
        viewModel.onDestinationSuggestionSelected(selectedPlace)

        assertEquals("-34.8770,-8.0476", viewModel.selectedDestinationCoords)
        assertTrue(viewModel.destinationSuggestions.value.isEmpty())
    }

    @Test
    fun selectRoute_updatesSelectedRouteInSuccessState() = runTest {
        viewModel.calculateRoute(origin = "Praça da Sé", destination = "Avenida Paulista")
        testDispatcher.scheduler.advanceUntilIdle()

        val initialSuccess = viewModel.uiState.value as UiState.Success
        val secondRoute = initialSuccess.routes.getOrNull(1) ?: initialSuccess.routes.first()

        viewModel.selectRoute(secondRoute)

        val updatedSuccess = viewModel.uiState.value as UiState.Success
        assertEquals(secondRoute, updatedSuccess.selectedRoute)
    }

    @Test
    fun resetToIdle_resetsStateToIdle() = runTest {
        viewModel.calculateRoute(origin = "Praça da Sé", destination = "Avenida Paulista")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetToIdle()

        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun cleanSuggestionText_removesMacroRegionsAndCountry() {
        val input = "Floresta, Pernambuco, Região Nordeste, Brasil"
        val expected = "Floresta, Pernambuco"
        assertEquals(expected, viewModel.cleanSuggestionText(input))

        assertEquals("Recife, Pernambuco", viewModel.cleanSuggestionText("Recife, Pernambuco, Região Nordeste, Brazil"))
        assertEquals("São Paulo, SP", viewModel.cleanSuggestionText("São Paulo, SP, Região Sudeste, Brasil"))
        assertEquals("Campinas", viewModel.cleanSuggestionText("Campinas"))
    }

    @Test
    fun onOriginSuggestionSelected_usesCleanedTextAndStoresCoordinates() = runTest {
        val place = NominatimPlaceDto(
            placeId = 1L,
            lat = "-8.61",
            lon = "-38.97",
            displayName = "Floresta, Pernambuco, Região Nordeste, Brasil"
        )
        val cleaned = viewModel.onOriginSuggestionSelected(place)
        assertEquals("Floresta, Pernambuco", cleaned)
        assertEquals("-38.97,-8.61", viewModel.selectedOriginCoords)
        assertTrue(viewModel.originSuggestions.value.isEmpty())
    }

    @Test
    fun onDestinationSuggestionSelected_usesCleanedTextAndStoresCoordinates() = runTest {
        val place = NominatimPlaceDto(
            placeId = 2L,
            lat = "-8.05",
            lon = "-34.87",
            displayName = "Recife, PE, Região Nordeste, Brasil"
        )
        val cleaned = viewModel.onDestinationSuggestionSelected(place)
        assertEquals("Recife, PE", cleaned)
        assertEquals("-34.87,-8.05", viewModel.selectedDestinationCoords)
        assertTrue(viewModel.destinationSuggestions.value.isEmpty())
    }

    @Test
    fun calculateRoute_cleansOriginAndDestinationInputs() = runTest {
        viewModel.calculateRoute(
            origin = "Floresta, Pernambuco, Região Nordeste, Brasil",
            destination = "Recife, PE, Região Nordeste, Brasil"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val successState = state as UiState.Success
        assertEquals("Floresta, Pernambuco", successState.origin)
        assertEquals("Recife, PE", successState.destination)
    }
}
