package com.desarrollodroide.adventurelog.feature.adventures.model

import com.desarrollodroide.adventurelog.core.model.SortDirection

data class AdventureFilters(
    val categoryNames: List<String> = emptyList(),
    val sortField: AdventureSortField = AdventureSortField.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val visitedFilter: VisitedFilter = VisitedFilter.ALL,
    val includeCollections: Boolean = false
)

enum class AdventureSortField(val apiValue: String) {
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
