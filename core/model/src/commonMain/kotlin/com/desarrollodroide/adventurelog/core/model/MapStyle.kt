package com.desarrollodroide.adventurelog.core.model

/**
 * The base maps the server offers, in the order and grouping the web client shows them.
 *
 * The codes are MapLibre basemaps. On Android, a map drawn by MapLibre draws them as the web does
 * (`basemapFor` in feature/ui: CARTO's styles and `default` use OpenFreeMap, since CARTO now wants
 * an API key, and the 3D terrain styles are drawn flat). A map drawn by Google Maps cannot use
 * them, so [MapStyles.mapTypeFor] translates each one into the nearest Google map type and the
 * preference still changes what the phone shows.
 */
data class MapStyleOption(
    val code: String,
    val label: String,
    val group: String
)

object MapStyles {

    const val DEFAULT = "default"

    val options: List<MapStyleOption> = listOf(
        MapStyleOption("default", "Default", "Standard"),
        MapStyleOption("osm-standard", "OpenStreetMap", "Standard"),
        MapStyleOption("terrain-3d", "3D Terrain", "3D Terrain"),
        MapStyleOption("satellite-terrain-3d", "3D Satellite Terrain", "3D Terrain"),
        MapStyleOption("topo-terrain-3d", "3D Topographic", "3D Terrain"),
        MapStyleOption("satellite", "Satellite", "Satellite"),
        MapStyleOption("satellite-labels", "Satellite + Labels", "Satellite"),
        MapStyleOption("usgs-imagery", "USGS Imagery", "Satellite"),
        MapStyleOption("usgs-imagery-topo", "USGS Imagery + Topo", "Satellite"),
        MapStyleOption("elevation", "Elevation", "Topographic"),
        MapStyleOption("usgs-topo", "USGS Topo", "Topographic"),
        MapStyleOption("esri-topo", "Esri Topo", "Topographic"),
        MapStyleOption("opentopomap", "OpenTopoMap", "Topographic"),
        MapStyleOption("carto-light", "Light", "Clean"),
        MapStyleOption("carto-dark", "Dark", "Clean"),
        MapStyleOption("carto-positron", "Positron", "Clean"),
        MapStyleOption("carto-positron-labels", "Positron + Labels", "Clean"),
        MapStyleOption("esri-gray", "Gray Canvas", "Clean"),
        MapStyleOption("carto-voyager", "Voyager", "Specialized"),
        MapStyleOption("osm-humanitarian", "Humanitarian", "Specialized"),
        MapStyleOption("openfreemap-liberty", "OpenFreeMap Liberty", "Specialized"),
        MapStyleOption("esri-streets", "Streets", "Specialized"),
        MapStyleOption("esri-national-geographic", "National Geographic", "Specialized"),
        MapStyleOption("esri-oceans", "Oceans", "Specialized"),
        MapStyleOption("osm-france", "France Style", "Specialized")
    )

    fun labelFor(code: String?): String =
        options.firstOrNull { it.code == code }?.label ?: "Default"

    /**
     * The nearest Google Maps type for a server basemap, as a platform-neutral name that the
     * Google Maps engine turns into a `MapType`. Anything with no imagery or relief equivalent
     * stays on the plain map rather than guessing.
     */
    fun mapTypeFor(code: String?): MapRendering = when (code) {
        "satellite", "usgs-imagery" -> MapRendering.SATELLITE
        "satellite-labels", "satellite-terrain-3d", "usgs-imagery-topo" -> MapRendering.HYBRID
        "terrain-3d", "topo-terrain-3d", "elevation",
        "usgs-topo", "esri-topo", "opentopomap" -> MapRendering.TERRAIN
        else -> MapRendering.NORMAL
    }
}

enum class MapRendering { NORMAL, SATELLITE, TERRAIN, HYBRID }
