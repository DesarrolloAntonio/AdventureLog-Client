package com.desarrollodroide.adventurelog.feature.map.ui.state

import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

data class MapUiState(
    val isLoading: Boolean = false,
    val locations: List<Location> = emptyList(),
    val visitedRegions: List<VisitedRegion> = emptyList(),
    val visitedCities: List<VisitedCity> = emptyList(),
    val activityTypes: List<String> = emptyList(),
    /** Category display name to how many of the mapped places carry it. */
    val categoryCounts: List<Pair<String, Int>> = emptyList(),
    val error: String? = null,
    val filters: MapFilters = MapFilters()
)

data class MapFilters(
    val showVisited: Boolean = true,
    val showPlanned: Boolean = true,
    val showRegions: Boolean = false,
    val showCities: Boolean = false,
    val selectedActivityTypes: Set<String> = emptySet(),
    /** Category names to keep. Empty means every category, as the web's "all" does. */
    val selectedCategories: Set<String> = emptySet(),
    val visitedCount: Int = 0,
    val plannedCount: Int = 0,
    val regionCount: Int = 0,
    val cityCount: Int = 0
)

data class MapStatistics(
    val visitedCount: Int = 0,
    val plannedCount: Int = 0,
    val totalCount: Int = 0,
    val regionCount: Int = 0
) {
    val completionPercentage: Int
        get() = if (totalCount > 0) {
            (visitedCount * 100) / totalCount
        } else 0
}

/**
 * The places the map should draw under these filters.
 *
 * This lived inside the screen's `remember`, which is the one place a filter cannot be checked
 * without a device and a map key. It is a predicate over a list; it belongs next to the filters
 * it reads.
 *
 * The three groups are independent and combine with AND: a visit state, a set of categories and
 * a set of activity tags. An empty set means "every one of them", the way the web's *all* chip
 * behaves - not "none", which would empty the map the moment a sheet was opened.
 */
fun List<Location>.mapMarkers(filters: MapFilters): List<Location> = filter { place ->
    val matchesVisitState =
        (place.isVisited && filters.showVisited) || (!place.isVisited && filters.showPlanned)

    val matchesActivity = filters.selectedActivityTypes.isEmpty() ||
        place.tags.any { it in filters.selectedActivityTypes }

    val matchesCategory = filters.selectedCategories.isEmpty() ||
        place.category?.displayName in filters.selectedCategories

    matchesVisitState && matchesActivity && matchesCategory
}
