package com.desarrollodroide.adventurelog.feature.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val getAllAdventuresForMapUseCase: GetAllAdventuresUseCase,
    private val observeUserStatsUseCase: ObserveUserStatsUseCase,
    private val getVisitedRegionsUseCase: GetVisitedRegionsUseCase,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val logger = Logger.withTag("MapViewModel")
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadAllAdventures()
        observeUserStats()
        loadVisitedRegions()
    }
    
    private fun observeUserStats() {
        viewModelScope.launch {
            // Get current user session to obtain username
            val userSession = userRepository.getUserSessionOnce()
            if (userSession == null) {
                logger.w { "No user session found, cannot observe stats" }
                return@launch
            }
            
            observeUserStatsUseCase(userSession.username).collect { stats ->
                logger.d { "📊 User stats updated:" }
                logger.d { "  - Trips (visited): ${stats.tripsCount}" }
                logger.d { "  - Visited regions: ${stats.visitedRegionCount}" }
                logger.d { "  - Visited countries: ${stats.visitedCountryCount}" }
                
                _uiState.update { state ->
                    state.copy(
                        filters = state.filters.copy(
                            visitedCount = stats.tripsCount,
                            regionCount = stats.visitedRegionCount
                        )
                    )
                }
            }
        }
    }
    
    private fun loadVisitedRegions() {
        viewModelScope.launch {
            logger.d { "📍 Loading visited regions for map..." }
            
            when (val result = getVisitedRegionsUseCase()) {
                is Either.Left -> {
                    logger.e { "❌ Error loading visited regions: ${result.value}" }
                }
                is Either.Right -> {
                    val visitedRegions = result.value
                    logger.d { "✅ Successfully loaded ${visitedRegions.size} visited regions" }
                    
                    _uiState.update { state ->
                        state.copy(
                            visitedRegions = visitedRegions
                        )
                    }
                }
            }
        }
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
                    
                    // Calculate planned count locally (adventures with location that are not visited)
                    val plannedCount = adventuresWithLocation.count { !it.isVisited }
                    
                    // Get unique activity types for filters
                    val activityTypes = adventuresWithLocation
                        .flatMap { it.activityTypes }
                        .distinct()
                        .sorted()
                    
                    logger.d { "📊 Map statistics:" }
                    logger.d { "  - Total adventures: ${adventures.size}" }
                    logger.d { "  - Adventures with location: ${adventuresWithLocation.size}" }
                    logger.d { "  - Planned (with location): $plannedCount" }
                    
                    val adventuresWithoutLocation = adventures.size - adventuresWithLocation.size
                    if (adventuresWithoutLocation > 0) {
                        logger.d { "  ⚠️ ${adventuresWithoutLocation} adventures hidden (no coordinates)" }
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            adventures = adventuresWithLocation,
                            activityTypes = activityTypes,
                            filters = state.filters.copy(
                                plannedCount = plannedCount
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
        loadVisitedRegions()
    }
}
                