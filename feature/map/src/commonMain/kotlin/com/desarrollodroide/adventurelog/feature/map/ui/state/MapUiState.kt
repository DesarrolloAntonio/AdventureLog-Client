package com.desarrollodroide.adventurelog.feature.map.ui.state

import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category

data class MapUiState(
    val isLoading: Boolean = false,
    val adventures: List<Adventure> = emptyList(),
    val activityTypes: List<String> = emptyList(),
    val error: String? = null,
    val filters: MapFilters = MapFilters()
)

data class MapFilters(
    val showVisited: Boolean = true,
    val showPlanned: Boolean = true,
    val showRegions: Boolean = false,
    val selectedActivityTypes: Set<String> = emptySet(),
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
