package com.desarrollodroide.adventurelog.feature.map.ui.components

import com.google.android.gms.maps.model.LatLngBounds
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapContent(
    locations: List<Location>,
    visitedRegions: List<VisitedRegion>,
    showRegions: Boolean,
    visitedCities: List<VisitedCity> = emptyList(),
    showCities: Boolean = false,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit = {},
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    contentPadding: PaddingValues = PaddingValues(),
    onVisibleBoundsChanged: ((LatLngBounds) -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center).size(64.dp)
                )
            }
            
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading map data",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Without this the map was a dead screen for the rest of the session: nothing
                    // reloaded it, not even leaving the tab and coming back (measured).
                    Button(onClick = onRetry) {
                        Text("Try again")
                    }
                }
            }
            
            else -> {
                // Edge to edge, as the canvas draws the Map tab: the controls float over it.
                Box(Modifier.fillMaxSize()) {
                    AdventureMapView(
                        locations = locations,
                        visitedCities = visitedCities,
                        showCities = showCities,
                        visitedRegions = visitedRegions,
                        showRegions = showRegions,
                        onAdventureClick = onAdventureClick,
                        modifier = Modifier.fillMaxSize(),
                        selectedId = selectedId,
                        contentPadding = contentPadding,
                        rounded = false,
                        onVisibleBoundsChanged = onVisibleBoundsChanged,
                    )
                }
            }
        }
    }
}
