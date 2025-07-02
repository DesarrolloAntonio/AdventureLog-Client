package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun LocationMapSection(
    latitude: String?,
    longitude: String?,
    onMapClick: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier
) {
    val defaultLocation = LatLng(40.4168, -3.7038) // Madrid as default
    
    val latLng = remember(latitude, longitude) {
        try {
            if (latitude != null && longitude != null) {
                LatLng(latitude.toDouble(), longitude.toDouble())
            } else {
                defaultLocation
            }
        } catch (e: NumberFormatException) {
            defaultLocation
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, if (latitude != null) 12f else 2f)
    }
    
    // Update camera when coordinates change
    LaunchedEffect(latLng) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(latLng, 12f),
            durationMs = 1000
        )
    }
    
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { clickedLatLng ->
            onMapClick(clickedLatLng.latitude, clickedLatLng.longitude)
        }
    ) {
        if (latitude != null && longitude != null) {
            Marker(
                state = MarkerState(position = latLng),
                title = "Selected Location"
            )
        }
    }
}
