package com.desarrollodroide.adventurelog.feature.ui.map.maplibre

import com.desarrollodroide.adventurelog.core.model.MapStyles
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BasemapsTest {

    @Test
    fun `default follows the theme with OpenFreeMap`() {
        assertEquals(Basemap.StyleUrl("https://tiles.openfreemap.org/styles/liberty"), basemapFor("default", dark = false))
        assertEquals(Basemap.StyleUrl("https://tiles.openfreemap.org/styles/dark"), basemapFor("default", dark = true))
    }

    @Test
    fun `no code or one this version does not know is the default`() {
        assertEquals(basemapFor("default", dark = true), basemapFor(null, dark = true))
        assertEquals(basemapFor("default", dark = false), basemapFor("bing-aerial", dark = false))
    }

    // Keyless CARTO tiles come back as an "API KEY REQUIRED" watermark since its 2026 terms.
    @Test
    fun `no style asks CARTO for tiles`() {
        MapStyles.options.forEach { option ->
            listOf(false, true).forEach { dark ->
                val basemap = basemapFor(option.code, dark)
                val urls = when (basemap) {
                    is Basemap.StyleUrl -> listOf(basemap.url)
                    is Basemap.Raster -> basemap.tiles + basemap.underlay.orEmpty()
                }
                assertTrue(urls.none { "carto" in it }, "${option.code} -> $urls")
            }
        }
    }

    @Test
    fun `CARTO codes become their OpenFreeMap equivalent`() {
        assertEquals(Basemap.StyleUrl("https://tiles.openfreemap.org/styles/positron"), basemapFor("carto-positron", dark = true))
        assertEquals(Basemap.StyleUrl("https://tiles.openfreemap.org/styles/positron"), basemapFor("carto-light", dark = false))
        assertEquals(Basemap.StyleUrl("https://tiles.openfreemap.org/styles/dark"), basemapFor("carto-dark", dark = false))
        assertEquals(Basemap.StyleUrl("https://tiles.openfreemap.org/styles/liberty"), basemapFor("carto-voyager", dark = true))
    }

    @Test
    fun `a chosen raster style does not follow the theme`() {
        assertEquals(basemapFor("osm-standard", dark = false), basemapFor("osm-standard", dark = true))
        val osm = assertIs<Basemap.Raster>(basemapFor("osm-standard", dark = false))
        assertEquals(listOf("https://tile.openstreetmap.org/{z}/{x}/{y}.png"), osm.tiles)
    }

    @Test
    fun `subdomain templates are expanded`() {
        val france = assertIs<Basemap.Raster>(basemapFor("osm-france", dark = false))
        assertEquals(3, france.tiles.size)
        assertFalse(france.tiles.any { "{s}" in it })
    }

    @Test
    fun `satellite with labels lays the labels over the imagery`() {
        val labels = assertIs<Basemap.Raster>(basemapFor("satellite-labels", dark = false))
        assertTrue("World_Boundaries_and_Places" in labels.tiles.single())
        assertTrue("World_Imagery" in assertNotNull(labels.underlay).single())
    }

    @Test
    fun `3D terrain styles draw their base flat`() {
        assertEquals(basemapFor("satellite", dark = false), basemapFor("satellite-terrain-3d", dark = false))
        assertEquals(basemapFor("esri-topo", dark = false), basemapFor("topo-terrain-3d", dark = false))
        assertEquals(basemapFor("openfreemap-liberty", dark = false), basemapFor("terrain-3d", dark = false))
    }

    @Test
    fun `a raster style is a style document MapLibre can load`() {
        val labels = assertIs<Basemap.Raster>(basemapFor("satellite-labels", dark = false))
        val style = Json.parseToJsonElement(labels.toStyleJson()).jsonObject
        assertEquals("8", style.getValue("version").jsonPrimitive.content)
        val layers = style.getValue("layers").jsonArray.map { it.jsonObject.getValue("source").jsonPrimitive.content }
        // Underlay first, so the labels draw over the imagery.
        assertEquals(listOf("base-tiles", "raster-tiles"), layers)
        val source = style.getValue("sources").jsonObject.getValue("raster-tiles").jsonObject
        assertEquals(labels.attribution, source.getValue("attribution").jsonPrimitive.content)
    }
}
