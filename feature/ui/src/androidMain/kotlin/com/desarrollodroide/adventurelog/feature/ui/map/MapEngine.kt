package com.desarrollodroide.adventurelog.feature.ui.map

/** The map engine the user asked for in Settings. */
enum class MapEngine { AUTO, GOOGLE, MAPLIBRE }

/** The engine that actually draws a map. */
enum class MapRenderer { GOOGLE, MAPLIBRE }

/** What Google Maps can do on this install. */
enum class GoogleMapsStatus {
    /** The fdroid flavor: the Google backend is not in the app at all. */
    NOT_INSTALLED,

    /** The backend is there, but Play Services are missing or too old to start the Maps SDK. */
    UNAVAILABLE,
    AVAILABLE,
}

/**
 * Which engine draws the maps. Google only when it can actually start: without Play Services the
 * Maps SDK never initialises and the first camera move crashes the app, so a stored "Google"
 * falls back to MapLibre the same way Auto does.
 */
fun resolveMapRenderer(preference: MapEngine, google: GoogleMapsStatus): MapRenderer = when {
    preference == MapEngine.MAPLIBRE -> MapRenderer.MAPLIBRE
    google == GoogleMapsStatus.AVAILABLE -> MapRenderer.GOOGLE
    else -> MapRenderer.MAPLIBRE
}
