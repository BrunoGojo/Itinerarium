package com.itinerarium.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricBike
import androidx.compose.material.icons.outlined.ElectricCar
import androidx.compose.material.icons.outlined.ElectricMoped
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector
import com.itinerarium.data.model.TransportMode

/**
 * Mapeia cada [TransportMode] para o seu ícone correspondente do Material Design.
 */
val TransportMode.icon: ImageVector
    get() = when (this) {
        TransportMode.CAR -> Icons.Outlined.DirectionsCar
        TransportMode.ELECTRIC_CAR -> Icons.Outlined.ElectricCar
        TransportMode.MOTORCYCLE -> Icons.Outlined.TwoWheeler
        TransportMode.ELECTRIC_MOTORCYCLE -> Icons.Outlined.ElectricMoped
        TransportMode.BICYCLE -> Icons.AutoMirrored.Outlined.DirectionsBike
        TransportMode.ELECTRIC_BICYCLE -> Icons.Outlined.ElectricBike
        TransportMode.WALKING -> Icons.AutoMirrored.Outlined.DirectionsWalk
    }
