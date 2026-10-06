package com.desarrollodroide.adventurelog.feature.map.ui.components

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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
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
                // Map with rounded corners and shadow
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    AdventureMapView(
                        locations = locations,
                        visitedCities = visitedCities,
                        showCities = showCities,
                        visitedRegions = visitedRegions,
                        showRegions = showRegions,
                        onAdventureClick = onAdventureClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
