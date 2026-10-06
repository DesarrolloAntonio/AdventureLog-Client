package com.desarrollodroide.adventurelog.feature.world.viewmodel

import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCountriesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RefreshCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedCitiesUseCase
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.feature.world.ui.state.WorldUiState
import com.desarrollodroide.adventurelog.feature.world.ui.state.FilterMode
import com.desarrollodroide.adventurelog.feature.world.ui.state.WorldRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorldViewModel(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val refreshCountriesUseCase: RefreshCountriesUseCase,
    private val getVisitedRegionsUseCase: GetVisitedRegionsUseCase,
    private val getVisitedCitiesUseCase: GetVisitedCitiesUseCase,
    private val observeCountriesUseCase: ObserveCountriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldUiState())
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()

    /**
     * While a pull is in flight. The screen used to set its own flag to true and back to false in
     * the same call, before the request even started, so the indicator was never drawn (QA 05, WO-01).
     */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadCountries()
        loadVisitedData()
        // The list follows the cache, so a region ticked on a country's page shows here on the way
        // back instead of after a pull (QA 05, WO-02).
        viewModelScope.launch {
            observeCountriesUseCase().collect { countries ->
                if (countries.isEmpty()) return@collect
                _uiState.update { it.copy(countries = countries, totalCountriesCount = countries.size) }
                calculateStatistics(countries)
                filterCountries()
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterCountries()
    }
    
    fun onRegionSelected(region: WorldRegion) {
        _uiState.update { it.copy(selectedRegion = region) }
        filterCountries()
    }
    
    fun onFilterModeChanged(filterMode: FilterMode) {
        _uiState.update { it.copy(filterMode = filterMode) }
        filterCountries()
    }
    
    fun onRefresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = refreshCountriesUseCase()) {
                is Either.Right -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            countries = result.value,
                            isLoading = false,
                            error = null
                        )
                    }
                    calculateStatistics(result.value)
                    filterCountries()
                }
                is Either.Left -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.value
                        )
                    }
                }
            }
            _isRefreshing.value = false
        }
    }
    
    private fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = getCountriesUseCase()) {
                is Either.Right -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            countries = result.value,
                            totalCountriesCount = result.value.size,
                            isLoading = false,
                            error = null
                        )
                    }
                    calculateStatistics(result.value)
                    filterCountries()
                }
                is Either.Left -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.value
                        )
                    }
                }
            }
        }
    }
    
    private fun loadVisitedData() {
        viewModelScope.launch {
            // Load visited regions
            getVisitedRegionsUseCase()
            
            // Load visited cities
            getVisitedCitiesUseCase()
        }
    }
    
    private fun calculateStatistics(countries: List<Country>) {
        // Three separate figures, as the web has them. Visited used to be computed with the
        // formula for complete, so an account that had set foot in one country read "Visited 0".
        val visitedCount = countries.count { it.numVisits > 0 }
        val completeCount = countries.count { it.numRegions > 0 && it.numVisits == it.numRegions }
        val partiallyVisitedCount = countries.count { it.numVisits > 0 && it.numVisits < it.numRegions }

        _uiState.update { currentState ->
            currentState.copy(
                visitedCountriesCount = visitedCount,
                completeCountriesCount = completeCount,
                partiallyVisitedCount = partiallyVisitedCount
            )
        }
    }
    
    private fun filterCountries() {
        val currentState = _uiState.value
        val allCountries = currentState.countries
        
        var filtered = allCountries
        
        // Filter by region
        if (currentState.selectedRegion != WorldRegion.ALL) {
            filtered = filtered.filter { country ->
                country.subregion == currentState.selectedRegion.displayName
            }
        }
        
        // Filter by visit status
        filtered = when (currentState.filterMode) {
            FilterMode.ALL -> filtered
            FilterMode.COMPLETE -> filtered.filter { it.numVisits > 0 && it.numVisits == it.numRegions }
            FilterMode.PARTIAL -> filtered.filter { it.numVisits > 0 && it.numVisits < it.numRegions }
            FilterMode.NOT_VISITED -> filtered.filter { it.numVisits == 0 }
        }
        
        // Filter by search query
        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { country ->
                country.name.contains(currentState.searchQuery, ignoreCase = true) ||
                country.capital?.contains(currentState.searchQuery, ignoreCase = true) == true
            }
        }
        
        _uiState.update {
            it.copy(filteredCountries = filtered)
        }
    }
}