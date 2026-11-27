package com.desarrollodroide.adventurelog.feature.ui.map

import kotlin.test.Test
import kotlin.test.assertEquals

class MapEngineTest {

    @Test
    fun `auto uses Google only when it is available`() {
        assertEquals(MapRenderer.GOOGLE, resolveMapRenderer(MapEngine.AUTO, GoogleMapsStatus.AVAILABLE))
        assertEquals(MapRenderer.MAPLIBRE, resolveMapRenderer(MapEngine.AUTO, GoogleMapsStatus.UNAVAILABLE))
        assertEquals(MapRenderer.MAPLIBRE, resolveMapRenderer(MapEngine.AUTO, GoogleMapsStatus.NOT_INSTALLED))
    }

    // The GrapheneOS crash: a phone without Play Services must never compose a Google map.
    @Test
    fun `stored Google falls back to MapLibre without Play Services`() {
        assertEquals(MapRenderer.MAPLIBRE, resolveMapRenderer(MapEngine.GOOGLE, GoogleMapsStatus.UNAVAILABLE))
    }

    // A preference saved by the play build survives a switch to the fdroid build, which has no
    // Google backend at all.
    @Test
    fun `stored Google falls back to MapLibre on the fdroid build`() {
        assertEquals(MapRenderer.MAPLIBRE, resolveMapRenderer(MapEngine.GOOGLE, GoogleMapsStatus.NOT_INSTALLED))
    }

    @Test
    fun `stored Google uses Google when it is available`() {
        assertEquals(MapRenderer.GOOGLE, resolveMapRenderer(MapEngine.GOOGLE, GoogleMapsStatus.AVAILABLE))
    }

    @Test
    fun `MapLibre is MapLibre whatever Google can do`() {
        GoogleMapsStatus.entries.forEach { status ->
            assertEquals(MapRenderer.MAPLIBRE, resolveMapRenderer(MapEngine.MAPLIBRE, status))
        }
    }
}
