package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@Composable
actual fun AdventureMapView(
    adventures: List<Adventure>,
    visitedRegions: List<VisitedRegion>,
    showRegions: Boolean,
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(40.416775, -3.703790), 
            5f
        )
    }
    
    val bounds = remember(adventures, visitedRegions, showRegions) {
        val adventuresWithLocation = adventures.filter { adventure ->
            val lat = adventure.latitude.toDoubleOrNull()
            val lng = adventure.longitude.toDoubleOrNull()
            lat != null && lng != null && lat != 0.0 && lng != 0.0
        }
        
        val regionsWithLocation = if (showRegions) {
            visitedRegions.filter { region ->
                region.latitude != null && region.longitude != null
            }
        } else {
            emptyList()
        }
        
        if (adventuresWithLocation.isEmpty() && regionsWithLocation.isEmpty()) {
            null
        } else {
            val builder = LatLngBounds.Builder()
            
            adventuresWithLocation.forEach { adventure ->
                val lat = adventure.latitude.toDoubleOrNull()!!
                val lng = adventure.longitude.toDoubleOrNull()!!
                builder.include(LatLng(lat, lng))
            }
            
            regionsWithLocation.forEach { region ->
                region.latitude?.let { lat ->
                    region.longitude?.let { lng ->
                        builder.include(LatLng(lat, lng))
                    }
                }
            }
            
            try {
                builder.build()
            } catch (_: Exception) {
                null
            }
        }
    }
    
    LaunchedEffect(bounds) {
        bounds?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(it, 100),
                durationMs = 1000
            )
        }
    }
    
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        )
    }
    
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }
    
    val adventuresWithLocation = remember(adventures) {
        adventures.filter { adventure ->
            val lat = adventure.latitude.toDoubleOrNull()
            val lng = adventure.longitude.toDoubleOrNull()
            lat != null && lng != null && lat != 0.0 && lng != 0.0
        }
    }
    
    GoogleMap(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp)),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings
    ) {
        // Draw visited regions with custom markers if enabled
        if (showRegions) {
            visitedRegions.forEach { region ->
                region.latitude?.let { lat ->
                    region.longitude?.let { lng ->
                        val markerState = rememberMarkerState(position = LatLng(lat, lng))
                        
                        MarkerComposable(
                            state = markerState,
                        ) {
                            RegionMarker()
                        }
                    }
                }
            }
        }
        
        // Draw adventure markers
        adventuresWithLocation.forEach { adventure ->
            val lat = adventure.latitude.toDoubleOrNull() ?: return@forEach
            val lng = adventure.longitude.toDoubleOrNull() ?: return@forEach
            val markerState = rememberMarkerState(position = LatLng(lat, lng))
            
            MarkerComposable(
                state = markerState,
                title = adventure.name,
                snippet = adventure.location,
                onClick = {
                    onAdventureClick(adventure.id)
                    false
                }
            ) {
                AdventureMarker(
                    adventure = adventure,
                    isVisited = adventure.isVisited
                )
            }
        }
    }
}

@Composable
private fun AdventureMarker(
    adventure: Adventure,
    isVisited: Boolean,
    modifier: Modifier = Modifier
) {
    val markerColor = if (isVisited) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    
    SimpleMapMarker(
        color = markerColor,
        emoji = adventure.category?.icon,
        modifier = modifier
    )
}

@Composable
private fun RegionMarker(
    modifier: Modifier = Modifier
) {
    val regionColor = Color(0xFF4CAF50) // Verde similar al de la web
    
    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle with border
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerOffset = androidx.compose.ui.geometry.Offset(
                x = size.width / 2f,
                y = size.height / 2f
            )
            
            // Subtle shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.15f),
                radius = size.minDimension / 2f,
                center = androidx.compose.ui.geometry.Offset(
                    x = centerOffset.x + 1.dp.toPx(),
                    y = centerOffset.y + 2.dp.toPx()
                )
            )
            
            // Main circle with lighter fill
            drawCircle(
                color = regionColor.copy(alpha = 0.25f),
                radius = size.minDimension / 2f,
                center = centerOffset
            )
            
            // Inner circle (filled)
            drawCircle(
                color = regionColor.copy(alpha = 0.6f),
                radius = 5.dp.toPx(),
                center = centerOffset
            )
            
            // Center dot (bright)
            drawCircle(
                color = regionColor,
                radius = 3.dp.toPx(),
                center = centerOffset
            )
        }
    }
}
