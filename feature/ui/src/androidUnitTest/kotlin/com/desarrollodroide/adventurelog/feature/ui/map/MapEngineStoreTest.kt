package com.desarrollodroide.adventurelog.feature.ui.map

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class MapEngineStoreTest {

    @Test
    fun `a new install is on Auto`() {
        assertEquals(MapEngine.AUTO, MapEngineStore(MapSettings()).engine.value)
    }

    @Test
    fun `a choice is kept across restarts`() {
        val settings = MapSettings()
        MapEngineStore(settings).set(MapEngine.MAPLIBRE)
        assertEquals(MapEngine.MAPLIBRE, MapEngineStore(settings).engine.value)
    }

    @Test
    fun `a value this version does not know reads as Auto`() {
        val settings = MapSettings().apply { putString("map_engine", "BING") }
        assertEquals(MapEngine.AUTO, MapEngineStore(settings).engine.value)
    }
}
