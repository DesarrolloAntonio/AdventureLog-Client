package com.desarrollodroide.adventurelog.feature.map.ui.screen

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
import com.desarrollodroide.adventurelog.feature.map.ui.components.ClearStatsSection
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
            onAdventureClick = onAdventureClick
        )
        
        ClearStatsSection(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp, start = 16.dp, end = 16.dp),
            visitedCount = uiState.filters.visitedCount,
            plannedCount = uiState.filters.plannedCount,
            regionCount = uiState.filters.regionCount,
            onFilterClick = { showFilterSheet = true }
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
