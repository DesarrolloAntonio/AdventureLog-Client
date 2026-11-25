package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.feature.locations.model.LocationFilters
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.locations.model.countryGroups
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * "Sort by country" is the one order the server cannot give, so the list groups the whole library
 * itself: one heading per country, its places by name, and the same filters the server would have
 * applied, or the grouped list would show what the plain list hides.
 */
class CountryGroupsTest {

    private val byCountry = LocationFilters(sortField = LocationSortField.COUNTRY, sortDirection = SortDirection.ASCENDING)

    private val library = listOf(
        testLocation("Trolltunga", country = "Norway", isVisited = true, category = "Hiking"),
        testLocation("Fushimi Inari", country = "Japan", isVisited = true, category = "General"),
        testLocation("Geirangerfjord", country = "Norway", category = "General"),
        testLocation("Arashiyama", country = "Japan", category = "Hiking"),
        testLocation("Somewhere odd"),
    )

    @Test
    fun countriesAreHeadingsInOrderAndTheirPlacesByName() {
        val groups = countryGroups(library, byCountry, query = "")

        assertEquals(listOf("Japan", "Norway", null), groups.map { it.country })
        assertEquals(listOf("Arashiyama", "Fushimi Inari"), groups[0].locations.map { it.name })
        assertEquals(listOf("Geirangerfjord", "Trolltunga"), groups[1].locations.map { it.name })
    }

    @Test
    fun descendingTurnsTheCountriesRoundButPlacesWithoutOneStayLast() {
        val groups = countryGroups(library, byCountry.copy(sortDirection = SortDirection.DESCENDING), query = "")

        assertEquals(listOf("Norway", "Japan", null), groups.map { it.country })
    }

    @Test
    fun theFiltersTheServerWouldApplyStillApply() {
        val visitedHiking = byCountry.copy(visitedFilter = VisitedFilter.VISITED, categoryNames = listOf("hiking"))

        val groups = countryGroups(library, visitedHiking, query = "")

        assertEquals(listOf("Norway"), groups.map { it.country })
        assertEquals(listOf("Trolltunga"), groups.single().locations.map { it.name })
    }

    @Test
    fun aSearchNarrowsTheGroupsAndDropsTheEmptyOnes() {
        val groups = countryGroups(library, byCountry, query = "fushimi")

        assertEquals(listOf("Japan"), groups.map { it.country })
    }
}
