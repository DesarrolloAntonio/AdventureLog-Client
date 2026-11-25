package com.desarrollodroide.adventurelog.feature.map

import com.desarrollodroide.adventurelog.feature.map.ui.components.MapVisibility
import com.desarrollodroide.adventurelog.feature.map.ui.components.mapVisibilityOf
import com.desarrollodroide.adventurelog.feature.map.ui.components.togglesFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** All / Visited / Planned over the two show-visited and show-planned switches the map keeps. */
class MapVisibilityTest {

    @Test
    fun `the switches read as one choice`() {
        assertEquals(MapVisibility.ALL, mapVisibilityOf(showVisited = true, showPlanned = true))
        assertEquals(MapVisibility.VISITED, mapVisibilityOf(showVisited = true, showPlanned = false))
        assertEquals(MapVisibility.PLANNED, mapVisibilityOf(showVisited = false, showPlanned = true))
        // Both off is possible from the layers sheet; then none of the three is chosen.
        assertNull(mapVisibilityOf(showVisited = false, showPlanned = false))
    }

    @Test
    fun `a choice flips only the switches it has to`() {
        // (flip visited, flip planned)
        assertEquals(false to true, togglesFor(showVisited = true, showPlanned = true, MapVisibility.VISITED))
        assertEquals(true to false, togglesFor(showVisited = true, showPlanned = true, MapVisibility.PLANNED))
        assertEquals(false to true, togglesFor(showVisited = true, showPlanned = false, MapVisibility.ALL))
        assertEquals(true to true, togglesFor(showVisited = false, showPlanned = true, MapVisibility.VISITED))
        assertEquals(false to false, togglesFor(showVisited = true, showPlanned = true, MapVisibility.ALL))
    }
}
