package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.feature.locations.model.LocationFilters
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The filters the places list sends to the server.
 *
 * Both cases below are bugs that shipped. They are cheap to hold onto and neither is visible by
 * reading the screen: the list just quietly shows the wrong places, or the wrong order.
 */
class LocationFiltersTest {

    @Test
    fun collectionsAreIncludedByDefault() {
        // The server reads includeCollections=false as "only places filed under no collection",
        // so defaulting it off hid every place in a trip the moment any filter was touched.
        assertTrue(LocationFilters().includeCollections)
    }

    @Test
    fun everySortFieldIsOneTheServerActuallyAccepts() {
        // The server takes name, type, date, rating and updated_at, and silently falls back to
        // name for anything else - which is how created_at sorted alphabetically without a word.
        val accepted = setOf("name", "type", "date", "rating", "updated_at")
        LocationSortField.entries.forEach { field ->
            assertTrue(
                field.apiValue in accepted,
                "${field.name} sends '${field.apiValue}', which the server would ignore"
            )
        }
    }

    @Test
    fun theDefaultsAreTheOnesTheWebOpensWith() {
        val filters = LocationFilters()
        assertEquals(LocationSortField.UPDATED_AT, filters.sortField)
        assertEquals(VisitedFilter.ALL, filters.visitedFilter)
        assertTrue(filters.categoryNames.isEmpty())
    }
}
