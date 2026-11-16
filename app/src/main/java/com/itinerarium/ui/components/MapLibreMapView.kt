package com.itinerarium.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.itinerarium.data.model.LatLngData
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

const val osmStyleJson = """
{
  "version": 8,
  "name": "OSM Public Raster Style",
  "sources": {
    "osm-raster-source": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm-raster-layer",
      "type": "raster",
      "source": "osm-raster-source",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}
"""

@Composable
fun MapLibreMapView(
    modifier: Modifier = Modifier,
    pathPoints: List<LatLngData>,
    originTitle: String,
    destinationTitle: String,
    isEcoFriendly: Boolean
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            onStart()
            onResume()
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            try {
                mapView.onPause()
                mapView.onStop()
                mapView.onDestroy()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.getMapAsync { mapLibreMap ->
                val styleBuilder = Style.Builder().fromJson(osmStyleJson)
                val drawRouteAndFitBounds: () -> Unit = {
                    try {
                        mapLibreMap.clear()
                        if (pathPoints.isNotEmpty()) {
                            val latLngs = pathPoints.map { LatLng(it.latitude, it.longitude) }
                            val routeColor = if (isEcoFriendly) {
                                android.graphics.Color.parseColor("#2E7D32") // Eco Green
                            } else {
                                android.graphics.Color.parseColor("#0066FF") // Route Blue
                            }

                            val polylineOptions = PolylineOptions()
                                .addAll(latLngs)
                                .color(routeColor)
                                .width(6f)
                            mapLibreMap.addPolyline(polylineOptions)

                            mapLibreMap.addMarker(
                                MarkerOptions()
                                    .position(latLngs.first())
                                    .title(originTitle)
                                    .snippet("Origem")
                            )
                            mapLibreMap.addMarker(
                                MarkerOptions()
                                    .position(latLngs.last())
                                    .title(destinationTitle)
                                    .snippet("Destino")
                            )

                            if (latLngs.size > 1) {
                                val boundsBuilder = LatLngBounds.Builder()
                                latLngs.forEach { boundsBuilder.include(it) }
                                val bounds = boundsBuilder.build()
                                try {
                                    mapLibreMap.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 120),
                                        1000
                                    )
                                } catch (_: Exception) {
                                    try {
                                        mapLibreMap.moveCamera(
                                            CameraUpdateFactory.newLatLngBounds(bounds, 120)
                                        )
                                    } catch (_: Exception) {}
                                }
                            } else if (latLngs.size == 1) {
                                try {
                                    mapLibreMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14.0),
                                        1000
                                    )
                                } catch (_: Exception) {
                                    try {
                                        mapLibreMap.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14.0)
                                        )
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                if (mapLibreMap.style != null && mapLibreMap.style?.isFullyLoaded == true) {
                    drawRouteAndFitBounds()
                } else {
                    try {
                        mapLibreMap.setStyle(styleBuilder) { _ ->
                            drawRouteAndFitBounds()
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    )
}
