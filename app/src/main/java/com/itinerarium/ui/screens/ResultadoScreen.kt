package com.itinerarium.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinerarium.data.model.LatLngData
import com.itinerarium.data.model.Route
import com.itinerarium.data.model.RouteSegment
import com.itinerarium.data.model.TransportMode
import com.itinerarium.ui.components.MapLibreMapView
import com.itinerarium.ui.theme.EcoGreen
import com.itinerarium.ui.theme.EcoGreenContainer
import com.itinerarium.ui.theme.ItinerariumTheme
import com.itinerarium.ui.theme.OnEcoGreenContainer
import com.itinerarium.ui.utils.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoScreen(
    routes: List<Route>,
    selectedRoute: Route,
    origin: String,
    destination: String,
    onSelectRoute: (Route) -> Unit,
    onBack: () -> Unit
) {
    var isDetailsExpanded by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$origin → $destination",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${routes.size} rotas encontradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar / Editar pesquisa"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. MapLibre MapView
            MapLibreMapView(
                modifier = Modifier.fillMaxSize(),
                pathPoints = selectedRoute.pathPoints,
                originTitle = origin,
                destinationTitle = destination,
                isEcoFriendly = selectedRoute.transportMode.isEcoFriendly
            )

            // Overlay Superior: Seletor Rápido de Modais / Rotas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(routes) { route ->
                        val isSelected = route.id == selectedRoute.id
                        RouteSelectionChip(
                            route = route,
                            isSelected = isSelected,
                            onClick = { onSelectRoute(route) }
                        )
                    }
                }
            }

            // Overlay Inferior: Card de Resumo Flutuante com Segmentos
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header do Card Flutuante
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDetailsExpanded = !isDetailsExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedRoute.transportMode.isEcoFriendly) EcoGreenContainer
                                        else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedRoute.transportMode.icon,
                                    contentDescription = selectedRoute.transportMode.label,
                                    tint = if (selectedRoute.transportMode.isEcoFriendly) OnEcoGreenContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedRoute.transportMode.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedRoute.transportMode.isEcoFriendly) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(EcoGreen)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ECO",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = selectedRoute.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(onClick = { isDetailsExpanded = !isDetailsExpanded }) {
                            Icon(
                                imageVector = if (isDetailsExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isDetailsExpanded) "Recolher detalhes" else "Expandir detalhes"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid de Métricas Principais (Duração, Distância, Custo, CO2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBadge(
                            icon = Icons.Outlined.Schedule,
                            label = "Duração",
                            value = "${selectedRoute.totalDurationMinutes} min",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            icon = Icons.Outlined.Route,
                            label = "Distância",
                            value = "${"%.1f".format(selectedRoute.totalDistanceKm)} km",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            icon = Icons.Outlined.AttachMoney,
                            label = "Custo Est.",
                            value = if (selectedRoute.estimatedCost == 0.0) "Grátis" else "R$ %.2f".format(selectedRoute.estimatedCost),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            icon = Icons.Default.Co2,
                            label = "Emissão",
                            value = if (selectedRoute.co2EmissionsKg == 0.0) "Zero" else "%.2f kg".format(selectedRoute.co2EmissionsKg),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Seção Detalhada de Etapas/Segmentos da Rota
                    AnimatedVisibility(visible = isDetailsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Etapas do Percurso",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedRoute.segments) { segment ->
                                    RouteSegmentItem(segment = segment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSelectionChip(
    route: Route,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isEco = route.transportMode.isEcoFriendly
    val bgColor = if (isSelected) {
        if (isEco) EcoGreenContainer else MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    }

    val contentColor = if (isSelected) {
        if (isEco) OnEcoGreenContainer else MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        if (isEco) EcoGreen else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = route.transportMode.icon,
                contentDescription = route.transportMode.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = route.transportMode.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = "${route.totalDurationMinutes} min • ${if (route.estimatedCost == 0.0) "R$0" else "R$%.1f".format(route.estimatedCost)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MetricBadge(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RouteSegmentItem(
    segment: RouteSegment
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = segment.transportMode.icon,
                    contentDescription = segment.transportMode.label,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = segment.instruction,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${"%.1f".format(segment.distanceKm)} km • ~${segment.durationMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultadoScreenPreview() {
    val sampleRoute = Route(
        id = "1",
        title = "Rota Ecológica Expressa",
        transportMode = TransportMode.ELECTRIC_CAR,
        totalDistanceKm = 4.8,
        totalDurationMinutes = 11,
        estimatedCost = 1.20,
        co2EmissionsKg = 0.07,
        pathPoints = listOf(
            LatLngData(-23.550520, -46.633308),
            LatLngData(-23.561414, -46.655881)
        ),
        segments = listOf(
            RouteSegment(
                id = "seg1",
                instruction = "Siga em direção à R. da Consolação via Viaduto do Chá",
                distanceKm = 2.3,
                durationMinutes = 6,
                transportMode = TransportMode.ELECTRIC_CAR,
                startLatLng = LatLngData(-23.550520, -46.633308),
                endLatLng = LatLngData(-23.561414, -46.655881)
            )
        )
    )

    ItinerariumTheme {
        ResultadoScreen(
            routes = listOf(sampleRoute),
            selectedRoute = sampleRoute,
            origin = "Praça da Sé",
            destination = "Avenida Paulista",
            onSelectRoute = {},
            onBack = {}
        )
    }
}

