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
import com.desarrollodroide.adventurelog.feature.ui.map.AppMap
import com.desarrollodroide.adventurelog.feature.ui.map.GeoBounds
import com.desarrollodroide.adventurelog.feature.ui.map.GeoPoint
import com.desarrollodroide.adventurelog.feature.ui.map.MapMarker
import com.desarrollodroide.adventurelog.feature.ui.map.MapPolyline
import com.desarrollodroide.adventurelog.feature.ui.map.rememberAppMapCamera

/**
 * The two ends of a journey on a map: either, both, or neither may be known yet.
 *
 * A transportation is the one thing in the app with two places rather than one, which is why it
 * does not reuse the single-point map the rest of the app draws.
 */
@Composable
fun MapView(
    originLat: Double?,
    originLng: Double?,
    destinationLat: Double?,
    destinationLng: Double?,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val origin = remember(originLat, originLng) {
        if (originLat != null && originLng != null) GeoPoint(originLat, originLng) else null
    }
    val destination = remember(destinationLat, destinationLng) {
        if (destinationLat != null && destinationLng != null) GeoPoint(destinationLat, destinationLng) else null
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

    val camera = rememberAppMapCamera(center = origin ?: destination!!, zoom = 9f)

    // With both ends known the useful view is the one that holds them both, however far apart
    // they are; with one, centre on it.
    LaunchedEffect(origin, destination) {
        if (origin != null && destination != null) {
            camera.fit(GeoBounds.of(listOf(origin, destination))!!, padding = 36.dp)
        } else {
            camera.moveTo(origin ?: destination!!, zoom = 9f)
        }
    }

    val markers = listOfNotNull(
        origin?.let { MapMarker(key = "from", position = it, title = "From") },
        destination?.let { MapMarker(key = "to", position = it, title = "To") },
    )
    val line = if (origin != null && destination != null) {
        listOf(MapPolyline(listOf(origin, destination), MaterialTheme.colorScheme.primary, 2.5.dp))
    } else {
        emptyList()
    }

    AppMap(
        camera = camera,
        modifier = modifier.fillMaxSize(),
        markers = markers,
        polylines = line,
        onMapClick = { onMapClick(it.latitude, it.longitude) },
        zoomControls = true,
    )
}
