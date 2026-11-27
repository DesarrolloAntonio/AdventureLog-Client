package com.desarrollodroide.adventurelog.feature.ui.map

/** A place on the map, in degrees. The app's own type, so no screen depends on an engine's. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * A box on the map, in degrees. When [west] is greater than [east] the box crosses the
 * antimeridian, as a view over the Pacific does.
 */
data class GeoBounds(val south: Double, val west: Double, val north: Double, val east: Double) {

    fun contains(point: GeoPoint): Boolean {
        if (point.latitude < south || point.latitude > north) return false
        return if (west <= east) point.longitude in west..east
        else point.longitude >= west || point.longitude <= east
    }

    companion object {
        /** The smallest box around [points], or null when there are none. */
        fun of(points: Collection<GeoPoint>): GeoBounds? {
            if (points.isEmpty()) return null
            return GeoBounds(
                south = points.minOf { it.latitude },
                west = points.minOf { it.longitude },
                north = points.maxOf { it.latitude },
                east = points.maxOf { it.longitude },
            )
        }
    }
}
