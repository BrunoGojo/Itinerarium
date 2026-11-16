package com.itinerarium.viewmodel

import com.itinerarium.data.model.Route

/**
 * Representa os estados da interface de usuário (UI) na busca e exibição de itinerários.
 */
sealed class UiState {

    /**
     * Estado inicial inativo, aguardando que o usuário informe origem e destino.
     */
    data object Idle : UiState()

    /**
     * Estado de carregamento enquanto as rotas estão sendo calculadas.
     */
    data object Loading : UiState()

    /**
     * Estado de sucesso indicando que as rotas foram calculadas e estão prontas para exibição.
     */
    data class Success(
        val routes: List<Route>,
        val selectedRoute: Route,
        val origin: String,
        val destination: String
    ) : UiState()

    /**
     * Estado de erro exibindo uma mensagem descritiva em caso de falha de validação, rede ou servidor.
     */
    data class Error(
        val message: String
    ) : UiState()
}
