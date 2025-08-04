package com.desarrollodroide.adventurelog.feature.world.ui.state

import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region

data class WorldUiState(
    val isLoading: Boolean = false,
    val countries: List<Country> = emptyList(),
    val filteredCountries: List<Country> = emptyList(),
    val visitedCountriesCount: Int = 0,
    val partiallyVisitedCount: Int = 0,
    val totalCountriesCount: Int = 250, // Default from API
    val selectedRegion: WorldRegion = WorldRegion.ALL,
    val searchQuery: String = "",
    val filterMode: FilterMode = FilterMode.ALL,
    val error: String? = null
)

enum class FilterMode {
    ALL,
    COMPLETE,
    PARTIAL,
    NOT_VISITED
}

enum class WorldRegion(val displayName: String) {
    ALL("All regions"),
    SOUTHERN_ASIA("Southern Asia"),
    NORTHERN_EUROPE("Northern Europe"),
    SOUTHERN_EUROPE("Southern Europe"),
    NORTHERN_AFRICA("Northern Africa"),
    POLYNESIA("Polynesia"),
    MIDDLE_AFRICA("Middle Africa"),
    CARIBBEAN("Caribbean"),
    SOUTH_AMERICA("South America"),
    WESTERN_ASIA("Western Asia"),
    AUSTRALIA_NEW_ZEALAND("Australia and New Zealand"),
    WESTERN_EUROPE("Western Europe"),
    EASTERN_EUROPE("Eastern Europe"),
    CENTRAL_AMERICA("Central America"),
    WESTERN_AFRICA("Western Africa"),
    NORTH_AMERICA("North America"),
    SOUTHERN_AFRICA("Southern Africa"),
    EASTERN_AFRICA("Eastern Africa"),
    SOUTH_EASTERN_ASIA("South-Eastern Asia"),
    EASTERN_ASIA("Eastern Asia"),
    CENTRAL_ASIA("Central Asia"),
    MELANESIA("Melanesia"),
    MICRONESIA("Micronesia")
}