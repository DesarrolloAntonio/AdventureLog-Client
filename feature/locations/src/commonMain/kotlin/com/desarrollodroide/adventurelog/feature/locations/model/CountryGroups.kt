package com.desarrollodroide.adventurelog.feature.locations.model

import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.SortDirection

/** One country's heading in the grouped list; [country] is null for places that have none. */
data class CountryGroup(
    val country: String?,
    val countryCode: String?,
    val locations: List<Location>,
)

/**
 * The library grouped under its countries, for "Sort by country": the filters and search the
 * server applies to the paged list are applied here too, countries in [LocationFilters.sortDirection]
 * order with the places that have no country last, and each country's places by name.
 */
fun countryGroups(locations: List<Location>, filters: LocationFilters, query: String): List<CountryGroup> {
    val needle = query.trim()
    val kept = locations.filter { location ->
        when (filters.visitedFilter) {
            VisitedFilter.ALL -> true
            VisitedFilter.VISITED -> location.isVisited
            VisitedFilter.NOT_VISITED -> !location.isVisited
        } &&
            (filters.categoryNames.isEmpty() || location.category?.name in filters.categoryNames) &&
            (needle.isEmpty() || location.name.contains(needle, ignoreCase = true) ||
                location.location?.contains(needle, ignoreCase = true) == true)
    }
    val (placed, unplaced) = kept.partition { it.country != null }
    val countries = placed
        .groupBy { it.country!!.name }
        .map { (name, items) ->
            CountryGroup(name, items.first().country?.countryCode, items.sortedBy { it.name.lowercase() })
        }
        .sortedBy { it.country!!.lowercase() }
        .let { if (filters.sortDirection == SortDirection.DESCENDING) it.reversed() else it }
    return if (unplaced.isEmpty()) countries
    else countries + CountryGroup(null, null, unplaced.sortedBy { it.name.lowercase() })
}
