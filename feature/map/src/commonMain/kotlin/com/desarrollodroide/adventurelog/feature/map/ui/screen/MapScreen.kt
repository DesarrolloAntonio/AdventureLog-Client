package com.desarrollodroide.adventurelog.feature.map.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapContent
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
        uiState.locations.filter { adventure ->
            val matchesVisitFilter = (adventure.isVisited && uiState.filters.showVisited) || 
                                    (!adventure.isVisited && uiState.filters.showPlanned)
            
            val matchesActivityType = uiState.filters.selectedActivityTypes.isEmpty() ||
                                    adventure.tags.any { it in uiState.filters.selectedActivityTypes }

            val matchesCategory = uiState.filters.selectedCategories.isEmpty() ||
                                    adventure.category?.displayName in uiState.filters.selectedCategories

            matchesVisitFilter && matchesActivityType && matchesCategory
        }
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
