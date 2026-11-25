package com.desarrollodroide.adventurelog.feature.ui.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import com.desarrollodroide.adventurelog.core.model.MapRendering
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties

/**
 * The map's properties for the account's style and the app's theme: a dark app gets dark street
 * tiles, not a white map glaring out of a dark screen. Satellite and hybrid are photographs and
 * stay as they are. Follows the app's own theme (its Light/Dark setting), not the system's.
 */
@Composable
fun rememberThemedMapProperties(rendering: MapRendering, isMyLocationEnabled: Boolean = false): MapProperties {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return remember(rendering, dark, isMyLocationEnabled) {
        val drawn = rendering == MapRendering.NORMAL || rendering == MapRendering.TERRAIN
        MapProperties(
            isMyLocationEnabled = isMyLocationEnabled,
            mapType = rendering.toGoogleMapType(),
            mapStyleOptions = if (dark && drawn) MapStyleOptions(NightStyle) else null,
        )
    }
}

/** Google's "Night" style, cooled towards the app's teal so it sits under the dark scheme. */
private const val NightStyle = """
[
  {"elementType":"geometry","stylers":[{"color":"#1d2326"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#8a9a9c"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#1d2326"}]},
  {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#3c4a4d"}]},
  {"featureType":"administrative.country","elementType":"labels.text.fill","stylers":[{"color":"#b8c7c9"}]},
  {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#c3d1d3"}]},
  {"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#7f9093"}]},
  {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#1f3330"}]},
  {"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#6b9a8f"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#2f393c"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#1b2124"}]},
  {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#8a9a9c"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#3d4a4e"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2629"}]},
  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2a3336"}]},
  {"featureType":"transit.station","elementType":"labels.text.fill","stylers":[{"color":"#8a9a9c"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#0e2a33"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#4f7680"}]},
  {"featureType":"landscape.natural","elementType":"geometry","stylers":[{"color":"#20292b"}]}
]
"""
