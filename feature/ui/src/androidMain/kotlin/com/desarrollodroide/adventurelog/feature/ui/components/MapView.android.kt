package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.ui.map.rememberMapRendering
import com.desarrollodroide.adventurelog.feature.ui.map.toGoogleMapType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun MapView(
    originLat: Double?,
    originLng: Double?,
    destinationLat: Double?,
    destinationLng: Double?,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier
) {
    val origin = remember(originLat, originLng) {
        if (originLat != null && originLng != null) LatLng(originLat, originLng) else null
    }
    val destination = remember(destinationLat, destinationLng) {
        if (destinationLat != null && destinationLng != null) {
            LatLng(destinationLat, destinationLng)
        } else {
            null
        }
    }

    // Nothing to draw yet - say so rather than showing an empty ocean somewhere off Africa,
    // which is where (0, 0) lands.
    if (origin == null && destination == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Search for a place above and it will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(origin ?: destination!!, 9f)
    }

    // With both ends known the useful view is the one that holds them both, however far apart
    // they are; with one, centre on it.
    LaunchedEffect(origin, destination) {
        if (origin != null && destination != null) {
            val bounds = LatLngBounds.builder().include(origin).include(destination).build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 96))
        } else {
            val single = origin ?: destination!!
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(single, 9f)
                )
            )
        }
    }

    val rendering = rememberMapRendering()

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = rendering.toGoogleMapType()),
        onMapClick = { onMapClick(it.latitude, it.longitude) }
    ) {
        origin?.let { Marker(state = MarkerState(position = it), title = "From") }
        destination?.let { Marker(state = MarkerState(position = it), title = "To") }

        if (origin != null && destination != null) {
            Polyline(
                points = listOf(origin, destination),
                color = MaterialTheme.colorScheme.primary,
                width = 6f
            )
        }
    }
}
