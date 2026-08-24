package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapStylesTest {

    @Test
    fun `every server basemap is offered`() {
        // The server rejects a map_style outside its own choices, so the list has to stay in step
        // with it. 25 is what BASEMAP_CHOICES holds.
        assertEquals(25, MapStyles.options.size)
        assertEquals(MapStyles.options.size, MapStyles.options.map { it.code }.toSet().size)
    }

    @Test
    fun `imagery styles render as satellite`() {
        assertEquals(MapRendering.SATELLITE, MapStyles.mapTypeFor("satellite"))
        assertEquals(MapRendering.SATELLITE, MapStyles.mapTypeFor("usgs-imagery"))
    }

    @Test
    fun `labelled imagery renders as hybrid`() {
        assertEquals(MapRendering.HYBRID, MapStyles.mapTypeFor("satellite-labels"))
        assertEquals(MapRendering.HYBRID, MapStyles.mapTypeFor("usgs-imagery-topo"))
    }

    @Test
    fun `relief styles render as terrain`() {
        assertEquals(MapRendering.TERRAIN, MapStyles.mapTypeFor("opentopomap"))
        assertEquals(MapRendering.TERRAIN, MapStyles.mapTypeFor("terrain-3d"))
    }

    @Test
    fun `styles with no mobile equivalent stay on the plain map`() {
        assertEquals(MapRendering.NORMAL, MapStyles.mapTypeFor("carto-dark"))
        assertEquals(MapRendering.NORMAL, MapStyles.mapTypeFor("default"))
    }

    @Test
    fun `an unknown or missing style falls back rather than crashing`() {
        // An older client must survive a style added on the server after it shipped.
        assertEquals(MapRendering.NORMAL, MapStyles.mapTypeFor("style-from-the-future"))
        assertEquals(MapRendering.NORMAL, MapStyles.mapTypeFor(null))
        assertEquals("Default", MapStyles.labelFor(null))
        assertEquals("Default", MapStyles.labelFor("style-from-the-future"))
    }

    @Test
    fun `groups are contiguous so the menu headings are not repeated`() {
        val groupsInOrder = MapStyles.options.map { it.group }
        val firstAppearances = groupsInOrder.distinct()
        var index = 0
        firstAppearances.forEach { group ->
            while (index < groupsInOrder.size && groupsInOrder[index] == group) index++
        }
        assertTrue(index == groupsInOrder.size, "groups are interleaved")
    }
}
