package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.clustering.Clustering
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.desarrollodroide.adventurelog.feature.ui.map.rememberMapRendering
import com.desarrollodroide.adventurelog.feature.ui.map.toGoogleMapType
import com.google.maps.android.compose.*

@Composable
actual fun AdventureMapView(
    locations: List<Location>,
    visitedRegions: List<VisitedRegion>,
    visitedCities: List<VisitedCity>,
    showCities: Boolean,
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
    
    val bounds = remember(locations, visitedRegions, showRegions) {
        val adventuresWithLocation = locations.filter { adventure ->
            val lat = adventure.latitude?.toDoubleOrNull()
            val lng = adventure.longitude?.toDoubleOrNull()
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
                val lat = adventure.latitude?.toDoubleOrNull()!!
                val lng = adventure.longitude?.toDoubleOrNull()!!
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
    
    val rendering = rememberMapRendering()
    val mapProperties = remember(rendering) {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = rendering.toGoogleMapType()
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
    
    val adventuresWithLocation = remember(locations) {
        locations.filter { adventure ->
            val lat = adventure.latitude?.toDoubleOrNull()
            val lng = adventure.longitude?.toDoubleOrNull()
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
        
        // Visited cities sit under the places, as a quieter layer.
        if (showCities) {
            visitedCities.forEach { city ->
                val lat = city.latitude
                val lng = city.longitude
                if (lat != null && lng != null) {
                    val cityState = rememberMarkerState(position = LatLng(lat, lng))
                    MarkerComposable(state = cityState, title = city.name) {
                        CityMarker()
                    }
                }
            }
        }

        // Clustered rather than one pin per place: two hundred markers over one country pile
        // into an unreadable blob, which is why the web clusters too. Zooming in splits them.
        val clusterItems = remember(adventuresWithLocation) {
            adventuresWithLocation.mapNotNull { adventure ->
                val lat = adventure.latitude?.toDoubleOrNull()
                val lng = adventure.longitude?.toDoubleOrNull()
                if (lat == null || lng == null) null else LocationClusterItem(adventure, lat, lng)
            }
        }

        Clustering(
            items = clusterItems,
            onClusterItemClick = { item ->
                onAdventureClick(item.location.id)
                false
            },
            clusterContent = { cluster -> ClusterBubble(count = cluster.size) },
            clusterItemContent = { item ->
                AdventureMarker(
                    location = item.location,
                    isVisited = item.location.isVisited
                )
            }
        )
    }
}

/** Wraps a location so the clustering library can place it. */
private data class LocationClusterItem(
    val location: Location,
    private val lat: Double,
    private val lng: Double
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat, lng)
    override fun getTitle(): String = location.name
    override fun getSnippet(): String = location.location.orEmpty()
}

/** How many places are stacked at this point. Grows a little with the count, as the web's does. */
@Composable
private fun ClusterBubble(count: Int, modifier: Modifier = Modifier) {
    val size = when {
        count >= 100 -> 56.dp
        count >= 25 -> 48.dp
        else -> 40.dp
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdventureMarker(
    location: Location,
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
        emoji = location.category?.icon,
        modifier = modifier
    )
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
