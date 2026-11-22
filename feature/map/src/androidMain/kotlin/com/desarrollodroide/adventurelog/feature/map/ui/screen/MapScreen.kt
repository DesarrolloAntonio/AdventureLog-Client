package com.desarrollodroide.adventurelog.feature.map.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import com.desarrollodroide.adventurelog.feature.ui.components.OpenInMapsSheet
import com.desarrollodroide.adventurelog.feature.map.ui.components.togglesFor
import com.desarrollodroide.adventurelog.feature.map.ui.components.mapVisibilityOf
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapVisibilityGroup
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapPlaceCard
import com.desarrollodroide.adventurelog.core.model.Location
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapContent
import com.desarrollodroide.adventurelog.feature.map.ui.state.mapMarkers
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapFilterSheet
import com.desarrollodroide.adventurelog.feature.map.viewmodel.MapViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MapScreen(
    onAdventureClick: (adventureId: String) -> Unit,
    onAddAdventureClick: () -> Unit
) {
    val viewModel: MapViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val filteredAdventures = remember(uiState.locations, uiState.filters) {
        uiState.locations.mapMarkers(uiState.filters)
    }

    // Coming back to a map that failed to load tries again; without it the error stayed for the
    // rest of the session (measured). A map that loaded is left alone - it is the heaviest read
    // in the app.
    LifecycleStartEffect(uiState.error) {
        if (uiState.error != null) viewModel.refresh()
        onStopOrDispose { }
    }

    // The pin last tapped: its card floats at the bottom of the map until closed or opened.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var directionsFor by remember { mutableStateOf<Location?>(null) }
    val selected = uiState.locations.firstOrNull { it.id == selectedId }

    // The map fills the tab, as the canvas draws it, and its controls float over it: what to show
    // with the counts, and the layers. (The stats used to stand in a card above the map, which
    // the canvas folds into the counts on these buttons.)
    Box(modifier = Modifier.fillMaxSize()) {
        MapContent(
            locations = filteredAdventures,
            visitedRegions = uiState.visitedRegions,
            showRegions = uiState.filters.showRegions,
            visitedCities = uiState.visitedCities,
            showCities = uiState.filters.showCities,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onRetry = viewModel::refresh,
            onAdventureClick = { selectedId = it },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapVisibilityGroup(
                    visitedCount = uiState.filters.visitedCount.takeUnless { uiState.isLoading || uiState.error != null },
                    plannedCount = uiState.filters.plannedCount.takeUnless { uiState.isLoading || uiState.error != null },
                    selected = mapVisibilityOf(uiState.filters.showVisited, uiState.filters.showPlanned),
                    onSelect = { target ->
                        val (flipVisited, flipPlanned) =
                            togglesFor(uiState.filters.showVisited, uiState.filters.showPlanned, target)
                        if (flipVisited) viewModel.toggleVisitedFilter()
                        if (flipPlanned) viewModel.togglePlannedFilter()
                    },
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Map layers")
                }
            }
            // The regions figure the stats card carried, and why the map may have fewer pins than
            // the counts: places without coordinates.
            val regions = uiState.filters.regionCount
            Text(
                text = listOfNotNull(
                    if (uiState.regionsLoaded) "$regions ${if (regions == 1) "region" else "regions"} visited" else null,
                    "only places with coordinates are shown"
                ).joinToString(" \u00b7 ").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }

        selected?.let { place ->
            val hasCoordinates = !place.latitude.isNullOrBlank() && !place.longitude.isNullOrBlank()
            MapPlaceCard(
                location = place,
                onOpen = { selectedId = null; onAdventureClick(place.id) },
                onDirections = if (hasCoordinates) ({ directionsFor = place }) else null,
                onDismiss = { selectedId = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Above the floating toolbar and the gesture bar.
                    .navigationBarsPadding()
                    .padding(start = 12.dp, end = 12.dp, bottom = 104.dp)
            )
        }
    }

    directionsFor?.let { place ->
        OpenInMapsSheet(
            latitude = place.latitude.orEmpty(),
            longitude = place.longitude.orEmpty(),
            placeName = place.name,
            shareUrl = place.link?.takeIf { it.isNotBlank() },
            onDismiss = { directionsFor = null }
        )
    }
    
    if (showFilterSheet) {
        MapFilterSheet(
            filters = uiState.filters,
            categoryCounts = uiState.categoryCounts,
            onToggleCategory = viewModel::toggleCategory,
            onToggleVisited = viewModel::toggleVisitedFilter,
            onTogglePlanned = viewModel::togglePlannedFilter,
            onToggleShowRegions = viewModel::toggleShowRegions,
            onToggleShowCities = viewModel::toggleShowCities,
            onDismiss = { showFilterSheet = false }
        )
    }
}
