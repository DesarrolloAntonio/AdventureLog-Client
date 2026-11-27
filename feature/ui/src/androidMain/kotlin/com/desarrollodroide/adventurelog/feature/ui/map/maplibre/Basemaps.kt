package com.desarrollodroide.adventurelog.feature.ui.map.maplibre

import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** What MapLibre draws under the app's own markers for one of the server's basemap codes. */
sealed interface Basemap {
    /** A vector style published by its provider. */
    data class StyleUrl(val url: String) : Basemap

    /** Raster tiles, drawn over an optional [underlay], as the web client builds them. */
    data class Raster(
        val tiles: List<String>,
        val attribution: String,
        /** The deepest zoom the provider serves; MapLibre stretches those tiles past it. */
        val maxZoom: Int = 19,
        val underlay: List<String>? = null,
    ) : Basemap
}

/**
 * The account's basemap as the web client draws it (`getBasemapUrl` in the web's
 * `frontend/src/lib/index.ts`), with three differences:
 *
 * - **CARTO** codes, and `default`, which the web draws with CARTO, use OpenFreeMap instead.
 *   CARTO's 2026 terms require an API key and its keyless tiles come back as a watermark that
 *   says so. OpenFreeMap needs no key, sets no limits and allows commercial use.
 * - **`default` follows the app's theme** (Liberty or Dark), as the web's does. A style the user
 *   picked by name is drawn as picked.
 * - **3D terrain** codes draw their base flat: MapLibre Native has no terrain.
 */
fun basemapFor(code: String?, dark: Boolean): Basemap = when (code) {
    "openfreemap-liberty", "carto-voyager", "terrain-3d" -> OpenFreeMap.Liberty
    "carto-light", "carto-positron", "carto-positron-labels" -> OpenFreeMap.Positron
    "carto-dark" -> OpenFreeMap.Dark
    "osm-standard" -> xyz(
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        "© OpenStreetMap contributors",
    )
    "osm-humanitarian" -> xyz(
        "https://tile-{s}.openstreetmap.fr/hot/{z}/{x}/{y}.png",
        "© OpenStreetMap contributors, Humanitarian OSM Team",
    )
    "osm-france" -> xyz(
        "https://tile-{s}.openstreetmap.fr/osmfr/{z}/{x}/{y}.png",
        "© OpenStreetMap France",
        maxZoom = 20,
    )
    "opentopomap" -> Basemap.Raster(
        tiles = listOf("a", "b", "c").map { "https://$it.tile.opentopomap.org/{z}/{x}/{y}.png" },
        attribution = "© OpenTopoMap (CC-BY-SA), © OpenStreetMap contributors",
        maxZoom = 17,
    )
    "satellite", "satellite-terrain-3d" -> esri("World_Imagery", "© Esri, Maxar, Earthstar Geographics, USGS")
    "satellite-labels" -> esri("Reference/World_Boundaries_and_Places", "© Esri")
        .copy(underlay = listOf(esriUrl("World_Imagery")))
    "elevation" -> esri("Elevation/World_Hillshade", "© Esri, USGS, NOAA", maxZoom = 16)
    "esri-topo", "topo-terrain-3d" -> esri("World_Topo_Map", "© Esri, HERE, Garmin, USGS, FAO, NOAA")
    "esri-streets" -> esri("World_Street_Map", "© Esri, HERE, Garmin, USGS, EPA")
    "esri-national-geographic" -> esri(
        "NatGeo_World_Map",
        "© Esri, National Geographic, Garmin, HERE, UNEP-WCMC",
        maxZoom = 16,
    )
    "esri-oceans" -> esri(
        "Ocean/World_Ocean_Base",
        "© Esri, GEBCO, NOAA, National Geographic, Garmin, HERE",
        maxZoom = 13,
    )
    "esri-gray" -> esri("Canvas/World_Light_Gray_Base", "© Esri, HERE, Garmin, USGS, EPA", maxZoom = 16)
    "usgs-topo" -> usgs("USGSTopo")
    "usgs-imagery" -> usgs("USGSImageryOnly")
    "usgs-imagery-topo" -> usgs("USGSImageryTopo")
    else -> if (dark) OpenFreeMap.Dark else OpenFreeMap.Liberty
}

/** The raster style as a MapLibre style document: the underlay first, so it draws below. */
fun Basemap.Raster.toStyleJson(): String = buildJsonObject {
    put("version", 8)
    putJsonObject("sources") {
        putJsonObject(RasterSource) {
            put("type", "raster")
            putJsonArray("tiles") { tiles.forEach { add(it) } }
            put("tileSize", 256)
            put("maxzoom", maxZoom)
            put("attribution", attribution)
        }
        underlay?.let { base ->
            putJsonObject(BaseSource) {
                put("type", "raster")
                putJsonArray("tiles") { base.forEach { add(it) } }
                put("tileSize", 256)
                put("maxzoom", maxZoom)
            }
        }
    }
    putJsonArray("layers") {
        if (underlay != null) addJsonObject {
            put("id", "base-layer")
            put("type", "raster")
            put("source", BaseSource)
        }
        addJsonObject {
            put("id", "raster-layer")
            put("type", "raster")
            put("source", RasterSource)
        }
    }
}.toString()

private const val RasterSource = "raster-tiles"
private const val BaseSource = "base-tiles"

private object OpenFreeMap {
    val Liberty = Basemap.StyleUrl("https://tiles.openfreemap.org/styles/liberty")
    val Positron = Basemap.StyleUrl("https://tiles.openfreemap.org/styles/positron")
    val Dark = Basemap.StyleUrl("https://tiles.openfreemap.org/styles/dark")
}

/** One tile URL, with a Leaflet-style `{s}` expanded to the a, b and c servers as the web does. */
private fun xyz(template: String, attribution: String, maxZoom: Int = 19) = Basemap.Raster(
    tiles = if ("{s}" in template) listOf("a", "b", "c").map { template.replace("{s}", it) } else listOf(template),
    attribution = attribution,
    maxZoom = maxZoom,
)

private fun esriUrl(service: String) =
    "https://server.arcgisonline.com/ArcGIS/rest/services/$service/MapServer/tile/{z}/{y}/{x}"

private fun esri(service: String, attribution: String, maxZoom: Int = 19) =
    Basemap.Raster(listOf(esriUrl(service)), attribution, maxZoom)

private fun usgs(service: String) = Basemap.Raster(
    tiles = listOf("https://basemap.nationalmap.gov/arcgis/rest/services/$service/MapServer/tile/{z}/{y}/{x}"),
    attribution = "© USGS",
    maxZoom = 16,
)
