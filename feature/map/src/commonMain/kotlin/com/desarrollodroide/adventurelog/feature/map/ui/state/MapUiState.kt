package com.desarrollodroide.adventurelog.feature.map.ui.state

import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

data class MapUiState(
    val isLoading: Boolean = false,
    val locations: List<Location> = emptyList(),
    val visitedRegions: List<VisitedRegion> = emptyList(),
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
    val selectedActivityTypes: Set<String> = emptySet(),
    /** Category names to keep. Empty means every category, as the web's "all" does. */
    val selectedCategories: Set<String> = emptySet(),
    val visitedCount: Int = 0,
    val plannedCount: Int = 0,
    val regionCount: Int = 0
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
