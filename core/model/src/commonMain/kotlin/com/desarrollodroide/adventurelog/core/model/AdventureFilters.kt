package com.desarrollodroide.adventurelog.core.model

data class AdventureFilters(
    val categoryNames: List<String> = emptyList(), // Changed from categoryIds to categoryNames
    val sortField: SortField = SortField.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val visitedFilter: VisitedFilter = VisitedFilter.ALL,
    val includeCollections: Boolean = false
)

enum class SortField(val apiValue: String) {
    UPDATED_AT("updated_at"),
    CREATED_AT("created_at"),
    NAME("name"),
    RATING("rating")
}

enum class SortDirection(val apiValue: String) {
    ASCENDING("asc"),
    DESCENDING("desc")
}

enum class VisitedFilter {
    ALL,
    VISITED,
    NOT_VISITED
}
