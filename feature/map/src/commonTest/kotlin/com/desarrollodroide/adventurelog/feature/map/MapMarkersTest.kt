package com.desarrollodroide.adventurelog.feature.map

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapFilters
import com.desarrollodroide.adventurelog.feature.map.ui.state.mapMarkers
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Which pins the map draws.
 *
 * Every mistake this can make looks identical from the outside - an empty map - and none of them
 * throw, so the only way to tell a filter apart from a failed request is to check the predicate.
 */
class MapMarkersTest {

    private val user = UserDetails(uuid = "u", username = "claude", dateJoined = "2024-01-01")

    private fun place(
        name: String,
        visited: Boolean,
        category: String? = null,
        tags: List<String> = emptyList()
    ) = Location(
        id = name,
        name = name,
        createdAt = "2024-01-01",
        updatedAt = "2024-01-01",
        isVisited = visited,
        tags = tags,
        category = category?.let {
            Category(id = it, name = it.lowercase(), displayName = it, icon = "", numAdventures = "1")
        },
        user = user
    )

    private val museum = place("Prado", visited = true, category = "Museum", tags = listOf("art"))
    private val hike = place("Peñalara", visited = false, category = "Hike", tags = listOf("walking"))
    private val cafe = place("Café", visited = true, category = "Food")

    private val all = listOf(museum, hike, cafe)

    private fun names(filters: MapFilters) = all.mapMarkers(filters).map { it.name }

    @Test
    fun bothVisitStatesAreShownByDefault() {
        assertEquals(listOf("Prado", "Peñalara", "Café"), names(MapFilters()))
    }

    @Test
    fun hidingPlannedLeavesOnlyWhatHasBeenVisited() {
        assertEquals(listOf("Prado", "Café"), names(MapFilters(showPlanned = false)))
    }

    @Test
    fun hidingVisitedLeavesOnlyWhatIsStillPlanned() {
        assertEquals(listOf("Peñalara"), names(MapFilters(showVisited = false)))
    }

    @Test
    fun turningBothOffEmptiesTheMap() {
        // Not a bug to fix in the predicate: the sheet shows both as off, so an empty map is the
        // honest answer. The predicate lying about it would be worse.
        assertEquals(emptyList(), names(MapFilters(showVisited = false, showPlanned = false)))
    }

    @Test
    fun noCategoriesChosenMeansEveryCategory() {
        // The opposite reading - none chosen, none drawn - is the bug that empties a map the
        // moment the filter sheet is opened.
        assertEquals(3, all.mapMarkers(MapFilters(selectedCategories = emptySet())).size)
    }

    @Test
    fun aChosenCategoryKeepsOnlyThatCategory() {
        assertEquals(listOf("Prado"), names(MapFilters(selectedCategories = setOf("Museum"))))
    }

    @Test
    fun categoriesMatchOnTheNameShownOnTheChip() {
        // The chips are built from displayName, so matching on `name` would silently keep nothing.
        assertEquals(emptyList(), names(MapFilters(selectedCategories = setOf("museum"))))
    }

    @Test
    fun aPlaceWithNoCategoryIsHiddenOnceAnyCategoryIsChosen() {
        val uncategorised = place("Somewhere", visited = true)
        assertEquals(
            emptyList(),
            listOf(uncategorised).mapMarkers(MapFilters(selectedCategories = setOf("Museum")))
                .map { it.name }
        )
    }

    @Test
    fun anActivityTagKeepsOnlyPlacesCarryingIt() {
        assertEquals(listOf("Prado"), names(MapFilters(selectedActivityTypes = setOf("art"))))
    }

    @Test
    fun theGroupsCombineWithAnd() {
        // Visited AND a museum: the café is visited but not a museum, so it goes.
        assertEquals(
            listOf("Prado"),
            names(MapFilters(showPlanned = false, selectedCategories = setOf("Museum", "Hike")))
        )
    }
}
