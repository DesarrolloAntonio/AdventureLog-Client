package com.desarrollodroide.adventurelog.feature.world.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SetRegionVisitedUseCase
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A region of the chosen country, and whether it has been visited. */
data class RegionRow(val region: Region, val visited: Boolean)

data class CountryDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val country: Country? = null,
    val regions: List<RegionRow> = emptyList(),
    val searchQuery: String = "",
    val message: String? = null
) {
    val visitedCount: Int get() = regions.count { it.visited }
    val filtered: List<RegionRow>
        get() = if (searchQuery.isBlank()) regions
        else regions.filter { it.region.name.contains(searchQuery, ignoreCase = true) }
}

class CountryDetailViewModel(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getVisitedRegionsUseCase: GetVisitedRegionsUseCase,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val setRegionVisitedUseCase: SetRegionVisitedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountryDetailUiState())
    val uiState: StateFlow<CountryDetailUiState> = _uiState.asStateFlow()

    private var loadedFor: String? = null

    fun load(countryCode: String) {
        if (loadedFor == countryCode) return
        loadedFor = countryCode
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // The country itself comes from the list the world screen already holds, so opening a
            // country does not re-fetch two hundred and fifty of them.
            val country = when (val countries = getCountriesUseCase()) {
                is Either.Right -> countries.value.firstOrNull { it.countryCode == countryCode }
                is Either.Left -> null
            }

            when (val regions = getRegionsUseCase(countryCode)) {
                is Either.Left -> _uiState.update {
                    it.copy(isLoading = false, error = regions.value, country = country)
                }
                is Either.Right -> {
                    val visitedIds = when (val visited = getVisitedRegionsUseCase()) {
                        is Either.Right -> visited.value.map { v -> v.regionId }.toSet()
                        is Either.Left -> emptySet()
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            country = country,
                            regions = regions.value
                                .map { r -> RegionRow(r, r.id in visitedIds) }
                                .sortedBy { r -> r.region.name }
                        )
                    }
                }
            }
        }
    }

    /**
     * Ticks a region, optimistically.
     *
     * The tick follows the finger and is put back if the server refuses. Waiting for the round
     * trip on a list where someone is about to tick fifteen provinces in a row would make every
     * one of them feel broken.
     */
    fun onRegionToggled(regionId: String) {
        val current = _uiState.value.regions.firstOrNull { it.region.id == regionId } ?: return
        val target = !current.visited
        setVisitedLocally(regionId, target)

        viewModelScope.launch {
            when (val result = setRegionVisitedUseCase(regionId, target)) {
                is Either.Right -> Unit
                is Either.Left -> {
                    setVisitedLocally(regionId, !target)
                    _uiState.update { it.copy(message = result.value) }
                }
            }
        }
    }

    private fun setVisitedLocally(regionId: String, visited: Boolean) {
        _uiState.update { state ->
            state.copy(
                regions = state.regions.map { row ->
                    if (row.region.id == regionId) row.copy(visited = visited) else row
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun retry() {
        val code = loadedFor ?: return
        loadedFor = null
        load(code)
    }
}
