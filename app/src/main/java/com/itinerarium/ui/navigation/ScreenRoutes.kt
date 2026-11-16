package com.itinerarium.ui.navigation

/**
 * Definições das rotas de navegação das telas do aplicativo Itinerarium.
 */
sealed class ScreenRoutes(val route: String) {
    data object Home : ScreenRoutes("home_screen")
    data object Loading : ScreenRoutes("loading_screen")
    data object Resultado : ScreenRoutes("resultado_screen")
    data object Error : ScreenRoutes("error_screen")
}
