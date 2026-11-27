package com.desarrollodroide.adventurelog.feature.ui.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeoBoundsTest {

    private val spain = GeoBounds(south = 36.0, west = -9.5, north = 43.8, east = 3.3)

    @Test
    fun `contains a point inside and not one outside`() {
        assertTrue(spain.contains(GeoPoint(40.4168, -3.7038)))
        assertFalse(spain.contains(GeoPoint(48.8566, 2.3522)))
        assertFalse(spain.contains(GeoPoint(40.0, 10.0)))
    }

    // A box over the Pacific: its west edge is east of its east edge.
    @Test
    fun `contains points on both sides of the antimeridian`() {
        val pacific = GeoBounds(south = -20.0, west = 170.0, north = 0.0, east = -170.0)
        assertTrue(pacific.contains(GeoPoint(-10.0, 175.0)))
        assertTrue(pacific.contains(GeoPoint(-10.0, -175.0)))
        assertFalse(pacific.contains(GeoPoint(-10.0, 0.0)))
    }

    @Test
    fun `of no points is null`() {
        assertNull(GeoBounds.of(emptyList()))
    }

    @Test
    fun `of points is the smallest box around them`() {
        val bounds = GeoBounds.of(listOf(GeoPoint(40.0, -3.0), GeoPoint(41.0, 2.0), GeoPoint(39.0, 0.0)))
        assertEquals(GeoBounds(south = 39.0, west = -3.0, north = 41.0, east = 2.0), bounds)
    }
}
