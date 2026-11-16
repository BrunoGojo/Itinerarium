package com.itinerarium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itinerarium.data.model.Route
import com.itinerarium.data.model.TransportMode
import com.itinerarium.data.network.ApiClient
import com.itinerarium.data.network.NominatimApiService
import com.itinerarium.data.network.dto.NominatimPlaceDto
import com.itinerarium.data.repository.RepositoryProvider
import com.itinerarium.data.repository.RouteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsável por gerenciar a lógica de negócios e os estados da interface do Itinerarium.
 */
class ItinerariumViewModel(
    private val repository: RouteRepository = RepositoryProvider.getRouteRepository(),
    private val nominatimApiService: NominatimApiService = ApiClient.nominatimApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _originSuggestions = MutableStateFlow<List<NominatimPlaceDto>>(emptyList())
    val originSuggestions: StateFlow<List<NominatimPlaceDto>> = _originSuggestions.asStateFlow()

    private val _destinationSuggestions = MutableStateFlow<List<NominatimPlaceDto>>(emptyList())
    val destinationSuggestions: StateFlow<List<NominatimPlaceDto>> = _destinationSuggestions.asStateFlow()

    var selectedOriginCoords: String? = null
        private set

    var selectedDestinationCoords: String? = null
        private set

    private var originSearchJob: Job? = null
    private var destinationSearchJob: Job? = null

    // Armazena os parâmetros da última consulta para suporte ao método retry()
    private var lastOrigin: String = ""
    private var lastDestination: String = ""
    private var lastTransportMode: TransportMode? = null

    /**
     * Atualiza a consulta de origem e busca sugestões no Nominatim com debounce de 500ms.
     */
    fun onOriginQueryChanged(query: String) {
        selectedOriginCoords = null
        originSearchJob?.cancel()

        val cleanQuery = query.trim()
        if (cleanQuery.length < 3) {
            _originSuggestions.value = emptyList()
            return
        }

        originSearchJob = viewModelScope.launch {
            delay(500)
            try {
                val response = nominatimApiService.search(
                    query = cleanQuery,
                    format = "json",
                    limit = 5,
                    countryCodes = "br",
                    language = "pt-BR"
                )
                if (response.isSuccessful) {
                    _originSuggestions.value = response.body() ?: emptyList()
                } else {
                    _originSuggestions.value = emptyList()
                }
            } catch (_: Exception) {
                _originSuggestions.value = emptyList()
            }
        }
    }

    /**
     * Atualiza a consulta de destino e busca sugestões no Nominatim com debounce de 500ms.
     */
    fun onDestinationQueryChanged(query: String) {
        selectedDestinationCoords = null
        destinationSearchJob?.cancel()

        val cleanQuery = query.trim()
        if (cleanQuery.length < 3) {
            _destinationSuggestions.value = emptyList()
            return
        }

        destinationSearchJob = viewModelScope.launch {
            delay(500)
            try {
                val response = nominatimApiService.search(
                    query = cleanQuery,
                    format = "json",
                    limit = 5,
                    countryCodes = "br",
                    language = "pt-BR"
                )
                if (response.isSuccessful) {
                    _destinationSuggestions.value = response.body() ?: emptyList()
                } else {
                    _destinationSuggestions.value = emptyList()
                }
            } catch (_: Exception) {
                _destinationSuggestions.value = emptyList()
            }
        }
    }

    /**
     * Processa o nome retornado pelas sugestões do Nominatim, removendo termos redundantes,
     * macro-regiões do Brasil (ex: ", Região Nordeste", ", Região Sudeste", etc.) e o sufixo do país (ex: ", Brasil", ", Brazil").
     * Exemplo: de "Floresta, Pernambuco, Região Nordeste, Brasil" para "Floresta, Pernambuco".
     */
    fun cleanSuggestionText(displayName: String): String {
        if (displayName.isBlank()) return displayName
        var cleaned = displayName

        val countryRegex = Regex("(?i),\\s*(brasil|brazil)\\b")
        cleaned = countryRegex.replace(cleaned, "")

        val macroRegionRegex = Regex("(?i),\\s*(regi[ãa]o\\s+)?(nordeste|sudeste|sul|norte|centro[- ]oeste)\\b")
        cleaned = macroRegionRegex.replace(cleaned, "")

        val macroRegionRegex2 = Regex("(?i)\\b(regi[ãa]o\\s+)?(nordeste|sudeste|sul|norte|centro[- ]oeste)\\b")
        cleaned = macroRegionRegex2.replace(cleaned, "")

        val countryRegex2 = Regex("(?i)\\b(brasil|brazil)\\b")
        cleaned = countryRegex2.replace(cleaned, "")

        cleaned = cleaned.replace(Regex(",\\s*,"), ",")
        cleaned = cleaned.replace(Regex("\\s+"), " ")
        cleaned = cleaned.replace(Regex("^[,\\s]+|[,\\s]+$"), "")
        cleaned = cleaned.replace(Regex(",\\s*,"), ",")
        return cleaned.trim()
    }

    /**
     * Define o local de origem selecionado a partir de uma sugestão do Nominatim,
     * limpando o texto do nome, salvando as coordenadas exatas ("lon,lat") e limpando a lista de sugestões.
     */
    fun onOriginSuggestionSelected(place: NominatimPlaceDto): String {
        selectedOriginCoords = "${place.lon},${place.lat}"
        _originSuggestions.value = emptyList()
        originSearchJob?.cancel()
        val cleaned = cleanSuggestionText(place.displayName ?: "")
        lastOrigin = cleaned
        return cleaned
    }

    /**
     * Define o local de destino selecionado a partir de uma sugestão do Nominatim,
     * limpando o texto do nome, salvando as coordenadas exatas ("lon,lat") e limpando a lista de sugestões.
     */
    fun onDestinationSuggestionSelected(place: NominatimPlaceDto): String {
        selectedDestinationCoords = "${place.lon},${place.lat}"
        _destinationSuggestions.value = emptyList()
        destinationSearchJob?.cancel()
        val cleaned = cleanSuggestionText(place.displayName ?: "")
        lastDestination = cleaned
        return cleaned
    }

    /**
     * Limpa manualmente as sugestões de origem.
     */
    fun clearOriginSuggestions() {
        _originSuggestions.value = emptyList()
        originSearchJob?.cancel()
    }

    /**
     * Limpa manualmente as sugestões de destino.
     */
    fun clearDestinationSuggestions() {
        _destinationSuggestions.value = emptyList()
        destinationSearchJob?.cancel()
    }

    /**
     * Valida os campos de entrada e solicita o cálculo das rotas ao repositório.
     *
     * @param origin Endereço ou ponto de origem.
     * @param destination Endereço ou ponto de destino.
     * @param transportMode Modal de transporte preferencial (opcional).
     */
    fun calculateRoute(
        origin: String,
        destination: String,
        transportMode: TransportMode? = null
    ) {
        val cleanOrigin = cleanSuggestionText(origin).trim()
        val cleanDestination = cleanSuggestionText(destination).trim()

        // Validação de entradas
        if (cleanOrigin.isBlank() || cleanDestination.isBlank()) {
            _uiState.value = UiState.Error(
                "Por favor, preencha tanto a origem quanto o destino para calcular o itinerário."
            )
            return
        }

        if (cleanOrigin.equals(cleanDestination, ignoreCase = true)) {
            _uiState.value = UiState.Error(
                "A origem e o destino não podem ser idênticos. Por favor, informe locais diferentes."
            )
            return
        }

        // Salva parâmetros para retry
        lastOrigin = cleanOrigin
        lastDestination = cleanDestination
        lastTransportMode = transportMode

        _uiState.value = UiState.Loading

        val originToUse = selectedOriginCoords ?: cleanOrigin
        val destinationToUse = selectedDestinationCoords ?: cleanDestination

        viewModelScope.launch {
            val result = repository.calculateRoutes(
                origin = originToUse,
                destination = destinationToUse,
                selectedMode = transportMode
            )

            result.fold(
                onSuccess = { routes ->
                    if (routes.isNotEmpty()) {
                        // Seleciona preferencialmente a rota correspondente ao modal solicitado, ou a primeira disponível
                        val selected = routes.find { it.transportMode == transportMode }
                            ?: routes.first()

                        _uiState.value = UiState.Success(
                            routes = routes,
                            selectedRoute = selected,
                            origin = cleanOrigin,
                            destination = cleanDestination
                        )
                    } else {
                        _uiState.value = UiState.Error(
                            "Nenhuma rota encontrada para o trajeto de '$cleanOrigin' até '$cleanDestination'."
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.value = UiState.Error(
                        throwable.localizedMessage ?: "Ocorreu um erro ao calcular as rotas."
                    )
                }
            )
        }
    }

    /**
     * Atualiza a rota selecionada no estado da UI quando o usuário clica em um card ou modal diferente.
     */
    fun selectRoute(route: Route) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            _uiState.value = currentState.copy(selectedRoute = route)
        }
    }

    /**
     * Tenta refazer a última solicitação de cálculo de rota efetuada.
     */
    fun retry() {
        if (lastOrigin.isNotBlank() && lastDestination.isNotBlank()) {
            calculateRoute(lastOrigin, lastDestination, lastTransportMode)
        } else {
            resetToIdle()
        }
    }

    /**
     * Reseta o estado da UI para inativo (Idle).
     */
    fun resetToIdle() {
        _uiState.value = UiState.Idle
    }
}
