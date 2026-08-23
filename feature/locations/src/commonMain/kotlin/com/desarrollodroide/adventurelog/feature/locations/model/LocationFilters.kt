package com.desarrollodroide.adventurelog.feature.locations.model

import com.desarrollodroide.adventurelog.core.model.SortDirection

data class LocationFilters(
    val categoryNames: List<String> = emptyList(),
    val sortField: LocationSortField = LocationSortField.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val visitedFilter: VisitedFilter = VisitedFilter.ALL,
    // The server reads this as "only locations that belong to no collection" when false, so
    // defaulting it off hid every location filed under a collection the moment any filter was
    // applied. True matches both the server default and the web client.
    val includeCollections: Boolean = true
)

enum class LocationSortField(val apiValue: String) {
    UPDATED_AT("updated_at"),

    /**
     * Latest visit date. The server accepts only name/type/date/rating/updated_at and silently
     * falls back to name for anything else, so `created_at` sorted by name without saying so.
     */
    VISIT_DATE("date"),
    NAME("name"),
    RATING("rating")
}

enum class VisitedFilter {
    ALL,
    VISITED,
    NOT_VISITED
}
