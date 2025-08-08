package com.desarrollodroide.adventurelog.feature.map.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
    
    val filteredAdventures = remember(uiState.adventures, uiState.filters) {
        uiState.adventures.filter { adventure ->
            val matchesVisitFilter = (adventure.isVisited && uiState.filters.showVisited) || 
                                    (!adventure.isVisited && uiState.filters.showPlanned)
            
            val matchesActivityType = uiState.filters.selectedActivityTypes.isEmpty() ||
                                    adventure.activityTypes.any { it in uiState.filters.selectedActivityTypes }
            
            matchesVisitFilter && matchesActivityType
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        MapContent(
            adventures = filteredAdventures,
            visitedRegions = uiState.visitedRegions,
            showRegions = uiState.filters.showRegions,
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
            onToggleVisited = viewModel::toggleVisitedFilter,
            onTogglePlanned = viewModel::togglePlannedFilter,
            onToggleShowRegions = viewModel::toggleShowRegions,
            onDismiss = { showFilterSheet = false }
        )
    }
}
