package com.desarrollodroide.adventurelog.feature.ui.map

import com.desarrollodroide.adventurelog.core.model.MapRendering
import com.google.maps.android.compose.MapType

/**
 * The Google Maps type that matches a server basemap. Styles with no imagery or relief
 * equivalent stay on the plain map rather than being approximated by something unrelated.
 */
fun MapRendering.toGoogleMapType(): MapType = when (this) {
    MapRendering.SATELLITE -> MapType.SATELLITE
    MapRendering.HYBRID -> MapType.HYBRID
    MapRendering.TERRAIN -> MapType.TERRAIN
    MapRendering.NORMAL -> MapType.NORMAL
}
