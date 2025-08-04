package com.desarrollodroide.adventurelog.feature.world.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CountriesRepository
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
    private val countriesRepository: CountriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldUiState())
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()
    
    init {
        loadCountries()
        loadVisitedData()
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = countriesRepository.refreshCountries()) {
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
        }
    }
    
    private fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = countriesRepository.getCountries()) {
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
            countriesRepository.getVisitedRegions()
            
            // Load visited cities
            countriesRepository.getVisitedCities()
        }
    }
    
    private fun calculateStatistics(countries: List<Country>) {
        val visitedCount = countries.count { it.numVisits > 0 && it.numVisits == it.numRegions }
        val partiallyVisitedCount = countries.count { it.numVisits > 0 && it.numVisits < it.numRegions }
        
        _uiState.update { currentState ->
            currentState.copy(
                visitedCountriesCount = visitedCount,
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