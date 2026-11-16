package com.itinerarium.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.itinerarium.ui.screens.ErrorScreen
import com.itinerarium.ui.screens.HomeScreen
import com.itinerarium.ui.screens.LoadingScreen
import com.itinerarium.ui.screens.ResultadoScreen
import com.itinerarium.viewmodel.ItinerariumViewModel
import com.itinerarium.viewmodel.UiState

@Composable
fun NavGraph(
    viewModel: ItinerariumViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Sincroniza a navegação com o UiState do ViewModel
    LaunchedEffect(uiState) {
        val targetRoute = when (uiState) {
            is UiState.Idle -> ScreenRoutes.Home.route
            is UiState.Loading -> ScreenRoutes.Loading.route
            is UiState.Success -> ScreenRoutes.Resultado.route
            is UiState.Error -> ScreenRoutes.Error.route
        }

        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                if (targetRoute == ScreenRoutes.Home.route) {
                    popUpTo(ScreenRoutes.Home.route) { inclusive = true }
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.Home.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(ScreenRoutes.Home.route) {
            val (initialOrigin, initialDestination) = when (val state = uiState) {
                is UiState.Success -> Pair(state.origin, state.destination)
                else -> Pair("", "")
            }

            HomeScreen(
                initialOrigin = initialOrigin,
                initialDestination = initialDestination,
                viewModel = viewModel,
                onCalculateRoute = { origin, destination, mode ->
                    viewModel.calculateRoute(origin, destination, mode)
                }
            )
        }

        composable(ScreenRoutes.Loading.route) {
            LoadingScreen()
        }

        composable(ScreenRoutes.Resultado.route) {
            when (val state = uiState) {
                is UiState.Success -> {
                    ResultadoScreen(
                        routes = state.routes,
                        selectedRoute = state.selectedRoute,
                        origin = state.origin,
                        destination = state.destination,
                        onSelectRoute = { viewModel.selectRoute(it) },
                        onBack = { viewModel.resetToIdle() }
                    )
                }
                else -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onCalculateRoute = { origin, destination, mode ->
                            viewModel.calculateRoute(origin, destination, mode)
                        }
                    )
                }
            }
        }

        composable(ScreenRoutes.Error.route) {
            val errorMessage = when (val state = uiState) {
                is UiState.Error -> state.message
                else -> "Ocorreu um erro inesperado ao processar o itinerário."
            }

            ErrorScreen(
                errorMessage = errorMessage,
                onRetry = { viewModel.retry() },
                onReset = { viewModel.resetToIdle() }
            )
        }
    }
}
