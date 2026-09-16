package com.desarrollodroide.adventurelog.feature.map.viewmodel

import com.desarrollodroide.adventurelog.feature.map.ui.state.MapFilters
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedCitiesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.desarrollodroide.adventurelog.core.model.userTags

class MapViewModel(
    private val getAllLocationsUseCase: GetAllLocationsUseCase,
    private val getVisitedRegionsUseCase: GetVisitedRegionsUseCase,
    private val getVisitedCitiesUseCase: GetVisitedCitiesUseCase,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    
    private val logger = Logger.withTag("MapViewModel")
    private val _uiState = MutableStateFlow(MapUiState(filters = restoredFilters()))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /**
     * What the filter sheet had chosen, as it was before the process died. Chosen filters came back
     * as the defaults after a `ui.py kill` (QA 06, MP-03). Only the choices are kept: the counts are
     * facts and are loaded again.
     */
    private fun restoredFilters(): MapFilters {
        val defaults = MapFilters()
        return defaults.copy(
            showVisited = savedStateHandle.get<Boolean>(KEY_VISITED) ?: defaults.showVisited,
            showPlanned = savedStateHandle.get<Boolean>(KEY_PLANNED) ?: defaults.showPlanned,
            showRegions = savedStateHandle.get<Boolean>(KEY_REGIONS) ?: defaults.showRegions,
            showCities = savedStateHandle.get<Boolean>(KEY_CITIES) ?: defaults.showCities,
            selectedCategories = savedStateHandle.get<String>(KEY_CATEGORIES)
                ?.split(SEPARATOR)?.filter { it.isNotEmpty() }?.toSet() ?: defaults.selectedCategories
        )
    }

    private fun saveFilters(filters: MapFilters) {
        savedStateHandle[KEY_VISITED] = filters.showVisited
        savedStateHandle[KEY_PLANNED] = filters.showPlanned
        savedStateHandle[KEY_REGIONS] = filters.showRegions
        savedStateHandle[KEY_CITIES] = filters.showCities
        savedStateHandle[KEY_CATEGORIES] = filters.selectedCategories.joinToString(SEPARATOR)
    }

    private fun updateFilters(change: (MapFilters) -> MapFilters) {
        _uiState.update { it.copy(filters = change(it.filters)) }
        saveFilters(_uiState.value.filters)
    }

    private companion object {
        const val KEY_VISITED = "map_show_visited"
        const val KEY_PLANNED = "map_show_planned"
        const val KEY_REGIONS = "map_show_regions"
        const val KEY_CITIES = "map_show_cities"
        const val KEY_CATEGORIES = "map_categories"
        const val SEPARATOR = "\u001F"
    }
    
    init {
        loadAllAdventures()
        loadVisitedRegions()
        loadVisitedCities()
    }
    
    private fun loadVisitedCities() {
        viewModelScope.launch {
            when (val result = getVisitedCitiesUseCase()) {
                is Either.Left -> logger.e { "❌ Error loading visited cities: ${result.value}" }
                is Either.Right -> {
                    val cities = result.value
                    logger.d { "✅ Loaded ${cities.size} visited cities" }
                    _uiState.update { state ->
                        state.copy(
                            visitedCities = cities,
                            filters = state.filters.copy(cityCount = cities.size)
                        )
                    }
                }
            }
        }
    }

    fun toggleShowCities() = updateFilters { it.copy(showCities = !it.showCities) }

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
                    
                    // The count is these regions. It came from the account's stats, whose use case
                    // answers a failure with an all-zero UserStats, so a slow server showed
                    // "0 Regions" as a fact (QA 06, screenshot). These carry a real outcome.
                    _uiState.update { state ->
                        state.copy(
                            visitedRegions = visitedRegions,
                            regionsLoaded = true,
                            filters = state.filters.copy(regionCount = visitedRegions.size)
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
                        .flatMap { it.tags.userTags() }
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
    
    fun toggleVisitedFilter() = updateFilters { it.copy(showVisited = !it.showVisited) }
    
    fun togglePlannedFilter() = updateFilters { it.copy(showPlanned = !it.showPlanned) }
    
    fun toggleShowRegions() = updateFilters { it.copy(showRegions = !it.showRegions) }

    fun toggleCategory(category: String) = updateFilters { filters ->
        filters.copy(
            selectedCategories = if (category in filters.selectedCategories) {
                filters.selectedCategories - category
            } else {
                filters.selectedCategories + category
            }
        )
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
    
    /** Back to showing everything, which is not the same as every switch off. */
    fun clearFilters() = updateFilters {
        it.copy(
            selectedActivityTypes = emptySet(),
            selectedCategories = emptySet(),
            showVisited = true,
            showPlanned = true
        )
    }

    fun refresh() {
        loadAllAdventures()
        loadVisitedRegions()
        loadVisitedCities()
    }
}
                