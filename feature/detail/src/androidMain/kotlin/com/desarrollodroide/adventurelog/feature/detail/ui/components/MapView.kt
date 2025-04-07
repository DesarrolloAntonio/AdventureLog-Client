package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Android implementation of MapView using Google Maps Compose.
 * Displays a map centered at the specified coordinates.
 */
@Composable
actual fun MapView(
    latitude: String,
    longitude: String,
    modifier: Modifier
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
    
    // Create Google Map with a marker
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = latLng),
            title = "Adventure Location"
        )
    }
}
