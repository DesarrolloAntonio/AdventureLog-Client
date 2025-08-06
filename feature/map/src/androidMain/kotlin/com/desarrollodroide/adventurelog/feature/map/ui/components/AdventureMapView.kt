package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@Composable
actual fun AdventureMapView(
    adventures: List<Adventure>,
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(40.416775, -3.703790), 
            5f
        )
    }
    
    val bounds = remember(adventures) {
        val adventuresWithLocation = adventures.filter { adventure ->
            val lat = adventure.latitude.toDoubleOrNull()
            val lng = adventure.longitude.toDoubleOrNull()
            lat != null && lng != null && lat != 0.0 && lng != 0.0
        }
        
        if (adventuresWithLocation.isEmpty()) {
            null
        } else {
            val builder = LatLngBounds.Builder()
            adventuresWithLocation.forEach { adventure ->
                val lat = adventure.latitude.toDoubleOrNull()!!
                val lng = adventure.longitude.toDoubleOrNull()!!
                builder.include(LatLng(lat, lng))
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
