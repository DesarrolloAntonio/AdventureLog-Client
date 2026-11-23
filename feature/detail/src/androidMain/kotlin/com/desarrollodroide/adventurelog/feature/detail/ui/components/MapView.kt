package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.desarrollodroide.adventurelog.feature.ui.map.rememberMapRendering
import com.desarrollodroide.adventurelog.feature.ui.map.toGoogleMapType
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Android implementation of MapView using Google Maps Compose.
 * Displays a map centered at the specified coordinates.
 */
@Composable
fun MapView(
    latitude: String,
    longitude: String,
    modifier: Modifier = Modifier
) {
    // Parse latitude and longitude
    val latLng = remember(latitude, longitude) {
        try {
            LatLng(latitude.toDouble(), longitude.toDouble())
        } catch (e: NumberFormatException) {
            LatLng(0.0, 0.0) // Default location if parsing fails
        }
    }
    
    // Remember camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 15f)
    }
    
    // The camera is only placed when the state is first created. On a tablet the detail pane stays
    // composed while the list picks another place, so the map kept showing the previous one (seen:
    // Trolltunga's page over a map of Kyoto). Follow the coordinates instead.
    LaunchedEffect(latLng) {
        if (cameraPositionState.position.target != latLng) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
        }
    }
    val markerState = remember(latLng) { MarkerState(position = latLng) }

    val rendering = rememberMapRendering()

    // Create Google Map with a marker
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = rendering.toGoogleMapType())
    ) {
        Marker(
            state = markerState,
            title = "Adventure Location"
        )
    }
}
