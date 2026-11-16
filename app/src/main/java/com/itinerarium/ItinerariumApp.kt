package com.itinerarium

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itinerarium.ui.navigation.NavGraph
import com.itinerarium.ui.theme.ItinerariumTheme
import com.itinerarium.viewmodel.ItinerariumViewModel

/**
 * Composable raiz do aplicativo Itinerarium.
 * Une o tema do app, Surface container, ViewModel e o grafo de navegação.
 */
@Composable
fun ItinerariumApp(
    viewModel: ItinerariumViewModel = viewModel()
) {
    ItinerariumTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavGraph(viewModel = viewModel)
        }
    }
}
