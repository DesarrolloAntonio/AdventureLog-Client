package com.desarrollodroide.adventurelog.feature.detail.ui.components

import com.desarrollodroide.adventurelog.feature.ui.map.rememberThemedMapProperties
import com.google.maps.android.compose.MarkerComposable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
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
    modifier: Modifier = Modifier,
    emoji: String? = null,
    visited: Boolean = true,
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
        properties = rememberThemedMapProperties(rendering),
        // A preview in a card: pinch still zooms, but Google's +/- buttons and its own
        // "open in Maps" toolbar crowded a 200dp map and repeated the page's toolbar.
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
    ) {
        // The Map tab's pin, not Google's red drop: a rounded square on a short point, inked
        // once visited, with the category's emoji.
        MarkerComposable(keys = arrayOf<Any>(emoji ?: "", visited), state = markerState, title = "Adventure Location") {
            PlacePin(emoji = emoji, visited = visited)
        }
    }
}

@Composable
private fun PlacePin(emoji: String?, visited: Boolean) {
    val colors = MaterialTheme.colorScheme
    val fill = if (visited) colors.primary else colors.surfaceContainerLowest
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(fill)
                .then(if (visited) Modifier else Modifier.border(2.dp, colors.primary, RoundedCornerShape(14.dp))),
            contentAlignment = Alignment.Center,
        ) {
            if (!emoji.isNullOrBlank()) Text(emoji, fontSize = 18.sp)
            else Icon(Icons.Default.Place, contentDescription = null, tint = if (visited) colors.onPrimary else colors.primary)
        }
        Box(Modifier.offset(y = (-6).dp).size(10.dp).rotate(45f).background(if (visited) fill else colors.primary))
    }
}
