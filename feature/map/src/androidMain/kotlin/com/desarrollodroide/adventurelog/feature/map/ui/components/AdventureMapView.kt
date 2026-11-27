package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.feature.map.ui.state.markerLabel
import com.desarrollodroide.adventurelog.feature.ui.map.AppMap
import com.desarrollodroide.adventurelog.feature.ui.map.GeoBounds
import com.desarrollodroide.adventurelog.feature.ui.map.GeoPoint
import com.desarrollodroide.adventurelog.feature.ui.map.MapMarker
import com.desarrollodroide.adventurelog.feature.ui.map.MarkerClusters
import com.desarrollodroide.adventurelog.feature.ui.map.rememberAppMapCamera

/**
 * The adventure map: the user's places, visited regions and visited cities.
 */
@Composable
fun AdventureMapView(
    locations: List<Location>,
    visitedRegions: List<VisitedRegion>,
    visitedCities: List<VisitedCity>,
    showCities: Boolean,
    showRegions: Boolean,
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier = Modifier,
    /** The pin tapped last, drawn as a label with its name. */
    selectedId: String? = null,
    /** Room the map keeps clear for controls floating over it: its logo and framing stay visible. */
    contentPadding: PaddingValues = PaddingValues(),
    /** False when the map runs edge to edge (the Map tab); true as a card inside a page. */
    rounded: Boolean = true,
    /** The area on screen whenever the camera settles, for a list of the places in view. */
    onVisibleBoundsChanged: ((GeoBounds) -> Unit)? = null,
) {
    val camera = rememberAppMapCamera(center = GeoPoint(40.416775, -3.703790), zoom = 5f)

    val placed = remember(locations) {
        locations.mapNotNull { place ->
            val lat = place.latitude?.toDoubleOrNull()
            val lng = place.longitude?.toDoubleOrNull()
            if (lat == null || lng == null || lat == 0.0 || lng == 0.0) null else place to GeoPoint(lat, lng)
        }
    }

    val bounds = remember(placed, visitedRegions, showRegions) {
        val regionPoints = if (showRegions) visitedRegions.mapNotNull { it.point() } else emptyList()
        GeoBounds.of(placed.map { it.second } + regionPoints)
    }
    LaunchedEffect(bounds) {
        bounds?.let { camera.fit(it, padding = 38.dp) }
    }

    val regionMarkers = remember(visitedRegions, showRegions) {
        if (!showRegions) emptyList() else visitedRegions.mapNotNull { region ->
            val point = region.point() ?: return@mapNotNull null
            val label = region.markerLabel()
            MapMarker(
                key = "region-${region.id}",
                position = point,
                title = label.title,
                snippet = label.snippet,
                iconKey = "region",
                content = { RegionMarker() },
            )
        }
    }
    // Visited cities sit under the places, as a quieter layer.
    val cityMarkers = remember(visitedCities, showCities) {
        if (!showCities) emptyList() else visitedCities.mapNotNull { city ->
            val point = city.point() ?: return@mapNotNull null
            val label = city.markerLabel()
            MapMarker(
                key = "city-${city.id}",
                position = point,
                title = label.title,
                snippet = label.snippet,
                iconKey = "city",
                content = { CityMarker() },
            )
        }
    }

    // Clustered rather than one pin per place: two hundred markers over one country pile
    // into an unreadable blob, which is why the web clusters too. Zooming in splits them.
    val onClick by rememberUpdatedState(onAdventureClick)
    val clusters = remember(placed, selectedId) {
        MarkerClusters(
            items = placed.map { (place, point) ->
                val selected = place.id == selectedId
                val emoji = place.category?.icon
                MapMarker(
                    key = place.id,
                    position = point,
                    title = place.name,
                    snippet = place.location.orEmpty(),
                    // The selected pin carries its name; the others look alike per category.
                    iconKey = PinLook(emoji, place.isVisited, if (selected) place.name else null),
                    onClick = { onClick(place.id) },
                    content = {
                        ExpressivePin(name = place.name, emoji = emoji, visited = place.isVisited, selected = selected)
                    },
                )
            },
            clusterContent = { count -> ExpressiveClusterBubble(count = count) },
        )
    }

    AppMap(
        camera = camera,
        modifier = modifier
            .fillMaxSize()
            .then(if (rounded) Modifier.clip(RoundedCornerShape(24.dp)) else Modifier),
        markers = regionMarkers + cityMarkers,
        clusters = clusters,
        contentPadding = contentPadding,
        onVisibleBoundsChanged = onVisibleBoundsChanged,
    )
}

/** What a place's pin looks like: places that share it share one drawn image. */
private data class PinLook(val emoji: String?, val visited: Boolean, val selectedName: String?)

private fun VisitedRegion.point(): GeoPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return GeoPoint(lat, lng)
}

private fun VisitedCity.point(): GeoPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return GeoPoint(lat, lng)
}

@Composable
private fun CityMarker(modifier: Modifier = Modifier) {
    SimpleMapMarker(
        color = Color(0xFF7E57C2),
        emoji = "🏙️",
        modifier = modifier
    )
}

@Composable
private fun RegionMarker(modifier: Modifier = Modifier) {
    val regionColor = Color(0xFF4CAF50) // the web's green

    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(x = size.width / 2f, y = size.height / 2f)

            // Subtle shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.15f),
                radius = size.minDimension / 2f,
                center = Offset(x = centerOffset.x + 1.dp.toPx(), y = centerOffset.y + 2.dp.toPx())
            )
            // Main circle with lighter fill
            drawCircle(color = regionColor.copy(alpha = 0.25f), radius = size.minDimension / 2f, center = centerOffset)
            // Inner circle (filled)
            drawCircle(color = regionColor.copy(alpha = 0.6f), radius = 5.dp.toPx(), center = centerOffset)
            // Center dot (bright)
            drawCircle(color = regionColor, radius = 3.dp.toPx(), center = centerOffset)
        }
    }
}
