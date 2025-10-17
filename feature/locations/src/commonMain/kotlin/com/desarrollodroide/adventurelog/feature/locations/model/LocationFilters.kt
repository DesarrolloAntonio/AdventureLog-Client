package com.desarrollodroide.adventurelog.feature.locations.model

import com.desarrollodroide.adventurelog.core.model.SortDirection

data class LocationFilters(
    val categoryNames: List<String> = emptyList(),
    val sortField: LocationSortField = LocationSortField.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val visitedFilter: VisitedFilter = VisitedFilter.ALL,
    val includeCollections: Boolean = false
)

enum class LocationSortField(val apiValue: String) {
    UPDATED_AT("updated_at"),
    CREATED_AT("created_at"),
    NAME("name"),
    RATING("rating")
}

enum class VisitedFilter {
    ALL,
    VISITED,
    NOT_VISITED
}
