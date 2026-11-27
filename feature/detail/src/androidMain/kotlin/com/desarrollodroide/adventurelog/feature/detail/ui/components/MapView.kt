package com.desarrollodroide.adventurelog.feature.detail.ui.components

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
import com.desarrollodroide.adventurelog.feature.ui.map.AppMap
import com.desarrollodroide.adventurelog.feature.ui.map.GeoPoint
import com.desarrollodroide.adventurelog.feature.ui.map.MapMarker
import com.desarrollodroide.adventurelog.feature.ui.map.rememberAppMapCamera

/**
 * The place on a small map, with its pin: the Map tab's pin, not Google's red drop - a rounded
 * square on a short point, inked once visited, with the category's emoji.
 */
@Composable
fun MapView(
    latitude: String,
    longitude: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    visited: Boolean = true,
) {
    val point = remember(latitude, longitude) {
        val lat = latitude.toDoubleOrNull()
        val lng = longitude.toDoubleOrNull()
        if (lat != null && lng != null) GeoPoint(lat, lng) else GeoPoint(0.0, 0.0)
    }
    val camera = rememberAppMapCamera(center = point, zoom = 15f)

    // The camera is only placed when the map is first created. On a tablet the detail pane stays
    // composed while the list picks another place, so the map kept showing the previous one (seen:
    // Trolltunga's page over a map of Kyoto). Follow the coordinates instead.
    LaunchedEffect(point) {
        camera.moveTo(point, zoom = 15f, animate = false)
    }

    AppMap(
        camera = camera,
        modifier = modifier.fillMaxSize(),
        markers = listOf(
            MapMarker(
                key = "place",
                position = point,
                title = "Adventure Location",
                iconKey = (emoji ?: "") to visited,
                content = { PlacePin(emoji = emoji, visited = visited) },
            )
        ),
    )
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
