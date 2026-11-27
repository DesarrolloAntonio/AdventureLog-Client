package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.feature.ui.map.AppMap
import com.desarrollodroide.adventurelog.feature.ui.map.GeoPoint
import com.desarrollodroide.adventurelog.feature.ui.map.MapMarker
import com.desarrollodroide.adventurelog.feature.ui.map.rememberAppMapCamera

/** The place being edited on a map: tap anywhere to put it there. */
@Composable
fun LocationMapSection(
    latitude: String?,
    longitude: String?,
    onMapClick: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val picked = remember(latitude, longitude) {
        val lat = latitude?.toDoubleOrNull()
        val lng = longitude?.toDoubleOrNull()
        if (lat != null && lng != null) GeoPoint(lat, lng) else null
    }
    val target = picked ?: Madrid

    val camera = rememberAppMapCamera(center = target, zoom = if (latitude != null) 12f else 2f)

    // Update camera when coordinates change
    LaunchedEffect(target) {
        camera.moveTo(target, zoom = 12f)
    }

    AppMap(
        camera = camera,
        modifier = modifier.fillMaxSize(),
        markers = listOfNotNull(picked?.let { MapMarker(key = "picked", position = it, title = "Selected location") }),
        onMapClick = { onMapClick(it.latitude, it.longitude) },
        zoomControls = true,
    )
}

private val Madrid = GeoPoint(40.4168, -3.7038)
