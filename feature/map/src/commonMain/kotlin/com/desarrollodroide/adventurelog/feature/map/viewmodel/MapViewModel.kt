package com.desarrollodroide.adventurelog.feature.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetAllAdventures
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val getAllAdventuresForMapUseCase: GetAllAdventures
) : ViewModel() {
    
    private val logger = Logger.withTag("MapViewModel")
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadAllAdventures()
    }
    
    private fun loadAllAdventures() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            logger.d { "📍 Loading all adventures for map..." }
            
            when (val result = getAllAdventuresForMapUseCase()) {
                is Either.Left -> {
                    logger.e { "❌ Error loading adventures: ${result.value}" }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load adventures"
                        )
                    }
                }
                is Either.Right -> {
                    val adventures = result.value
                    logger.d { "✅ Successfully loaded ${adventures.size} adventures for map" }
                    
                    // Filter adventures with valid location
                    val adventuresWithLocation = adventures.filter { 
                        it.latitude.isNotBlank() && it.longitude.isNotBlank() &&
                        it.latitude != "0.0" && it.longitude != "0.0"
                    }
                    
                    // Log ALL visited adventures to debug
                    logger.d { "🔍 DEBUG - All visited adventures (before location filter):" }
                    adventures.filter { it.isVisited }.forEach { adventure ->
                        logger.d { "  - ${adventure.name}: lat='${adventure.latitude}', lon='${adventure.longitude}', region=${adventure.region?.name}" }
                    }
                    
                    // Calculate stats locally from the adventures
                    val visitedCount = adventuresWithLocation.count { it.isVisited }
                    val plannedCount = adventuresWithLocation.count { !it.isVisited }
                    
                    // Calculate unique regions from visited adventures
                    val visitedWithLocation = adventuresWithLocation.filter { it.isVisited }
                    logger.d { "  - Visited adventures WITH valid location: ${visitedWithLocation.size}" }
                    
                    visitedWithLocation.forEach { adventure ->
                        logger.d { "    • ${adventure.name}: region=${adventure.region?.name ?: "NO REGION"} (id: ${adventure.region?.id})" }
                    }
                    
                    val visitedRegions = visitedWithLocation
                        .mapNotNull { it.region?.id }
                        .distinct()
                        .size
                    
                    logger.d { "📊 Map statistics:" }
                    logger.d { "  - Total adventures: ${adventures.size}" }
                    logger.d { "  - Adventures with location: ${adventuresWithLocation.size}" }
                    logger.d { "  - Visited (with location): $visitedCount" }
                    logger.d { "  - Planned (with location): $plannedCount" }
                    logger.d { "  - Visited regions: $visitedRegions" }
                    
                    val adventuresWithoutLocation = adventures.size - adventuresWithLocation.size
                    if (adventuresWithoutLocation > 0) {
                        logger.d { "  ⚠️ ${adventuresWithoutLocation} adventures hidden (no coordinates)" }
                    }
                    
                    // Get unique activity types for filters
                    val activityTypes = adventuresWithLocation
                        .flatMap { it.activityTypes }
                        .distinct()
                        .sorted()
                    
                    logger.d { "📊 Stats calculated:" }
                    logger.d { "  - Total with location: ${adventuresWithLocation.size}" }
                    logger.d { "  - Visited: $visitedCount" }
                    logger.d { "  - Planned: $plannedCount" }
                    logger.d { "  - Visited regions: $visitedRegions" }
                    
                    _uiState.update { state ->
                        state.copy(
                            adventures = adventuresWithLocation,
                            activityTypes = activityTypes,
                            filters = state.filters.copy(
                                visitedCount = visitedCount,
                                plannedCount = plannedCount,
                                regionCount = visitedRegions
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }
    }
    
    fun toggleVisitedFilter() {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.copy(
                    showVisited = !state.filters.showVisited
                )
            )
        }
    }
    
    fun togglePlannedFilter() {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.copy(
                    showPlanned = !state.filters.showPlanned
                )
            )
        }
    }
    
    fun toggleShowRegions() {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.copy(
                    showRegions = !state.filters.showRegions
                )
            )
        }
    }
    
    fun toggleActivityTypeFilter(activityType: String) {
        _uiState.update { state ->
            val selectedActivityTypes = state.filters.selectedActivityTypes.toMutableSet()
            if (activityType in selectedActivityTypes) {
                selectedActivityTypes.remove(activityType)
            } else {
                selectedActivityTypes.add(activityType)
            }
            
            state.copy(
                filters = state.filters.copy(
                    selectedActivityTypes = selectedActivityTypes
                )
            )
        }
    }
    
    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.copy(
                    selectedActivityTypes = emptySet()
                )
            )
        }
    }
    
    fun refresh() {
        loadAllAdventures()
    }
}
