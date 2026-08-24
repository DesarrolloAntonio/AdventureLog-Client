package com.desarrollodroide.adventurelog.feature.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val getAllLocationsUseCase: GetAllLocationsUseCase,
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
            val username = userSession?.username
            if (username == null) {
                logger.w { "No username found in session, cannot observe stats" }
                return@launch
            }
            
            observeUserStatsUseCase(username).collect { stats ->
                logger.d { "📊 User stats updated:" }
                logger.d { "  - Visited regions: ${stats.visitedRegionCount}" }
                logger.d { "  - Visited countries: ${stats.visitedCountryCount}" }

                // Only the region count comes from here. Visited used to be filled with
                // tripsCount - the number of collections - so the map reported 33 visited places
                // for an account with two.
                _uiState.update { state ->
                    state.copy(
                        filters = state.filters.copy(
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
            
            when (val result = getAllLocationsUseCase()) {
                is Either.Left -> {
                    logger.e { "❌ Error loading adventures: ${result.value}" }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load places"
                        )
                    }
                }
                is Either.Right -> {
                    val adventures = result.value
                    logger.d { "✅ Successfully loaded ${adventures.size} adventures for map" }
                    
                    // Filter adventures with valid location
                    val adventuresWithLocation = adventures.filter { 
                        !it.latitude.isNullOrBlank() && !it.longitude.isNullOrBlank() &&
                        it.latitude != "0.0" && it.longitude != "0.0"
                    }
                    
                    // Both counts come from the same set - the places the map actually draws -
                    // so they add up to what is on screen, which is what the card's footnote
                    // promises.
                    val visitedCount = adventuresWithLocation.count { it.isVisited }
                    val plannedCount = adventuresWithLocation.count { !it.isVisited }
                    
                    // Counted over the places the map draws, so the numbers on the chips match
                    // what selecting one actually leaves behind.
                    val categoryCounts = adventuresWithLocation
                        .mapNotNull { it.category?.displayName?.takeIf(String::isNotBlank) }
                        .groupingBy { it }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }

                    // Get unique activity types for filters
                    val activityTypes = adventuresWithLocation
                        .flatMap { it.tags }
                        .distinct()
                        .sorted()
                    
                    logger.d { "📊 Map statistics:" }
                    logger.d { "  - Total adventures: ${adventures.size}" }
                    logger.d { "  - Locations with location: ${adventuresWithLocation.size}" }
                    logger.d { "  - On the map: $visitedCount visited, $plannedCount planned" }
                    
                    val adventuresWithoutLocation = adventures.size - adventuresWithLocation.size
                    if (adventuresWithoutLocation > 0) {
                        logger.d { "  ⚠️ ${adventuresWithoutLocation} adventures hidden (no coordinates)" }
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            locations = adventuresWithLocation,
                            activityTypes = activityTypes,
                            categoryCounts = categoryCounts,
                            filters = state.filters.copy(
                                visitedCount = visitedCount,
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

    fun toggleCategory(category: String) {
        _uiState.update { state ->
            val selected = state.filters.selectedCategories
            state.copy(
                filters = state.filters.copy(
                    selectedCategories = if (category in selected) {
                        selected - category
                    } else {
                        selected + category
                    }
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
                