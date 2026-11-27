package com.desarrollodroide.adventurelog.feature.mapgoogle

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import com.desarrollodroide.adventurelog.core.model.MapStyles
import com.desarrollodroide.adventurelog.feature.ui.map.AppMapCamera
import com.desarrollodroide.adventurelog.feature.ui.map.AppMapSpec
import com.desarrollodroide.adventurelog.feature.ui.map.GeoBounds
import com.desarrollodroide.adventurelog.feature.ui.map.GeoPoint
import com.desarrollodroide.adventurelog.feature.ui.map.MapMarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState

/** An AppMap drawn by Google Maps, as every map in the app was before MapLibre. */
@Composable
internal fun GoogleMapContent(spec: AppMapSpec, modifier: Modifier) {
    val camera = spec.camera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(camera.initialCenter.toLatLng(), camera.initialZoom)
    }
    val density = LocalDensity.current
    DisposableEffect(camera, cameraPositionState, density) {
        val driver = GoogleCameraDriver(cameraPositionState, density)
        camera.attach(driver)
        onDispose { camera.detach(driver) }
    }

    // Whenever the camera comes to rest - the first fit included - say what is on screen. Not the
    // projection's visible region: that leaves out the padding, so the strip under the floating
    // search and filters counted as off screen. The corners of the view itself, less only the
    // start padding (where a sheet covers the map).
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val startInset = with(density) { spec.contentPadding.calculateStartPadding(LocalLayoutDirection.current).roundToPx() }
    spec.onVisibleBoundsChanged?.let { report ->
        LaunchedEffect(cameraPositionState.isMoving, mapSize, startInset) {
            val projection = cameraPositionState.projection
            if (!cameraPositionState.isMoving && projection != null && mapSize != IntSize.Zero) {
                val corners = listOf(
                    Point(startInset, 0), Point(mapSize.width, 0),
                    Point(startInset, mapSize.height), Point(mapSize.width, mapSize.height),
                )
                val builder = LatLngBounds.Builder()
                corners.forEach { builder.include(projection.fromScreenLocation(it)) }
                val bounds = builder.build()
                report(
                    GeoBounds(
                        south = bounds.southwest.latitude, west = bounds.southwest.longitude,
                        north = bounds.northeast.latitude, east = bounds.northeast.longitude,
                    ),
                )
            }
        }
    }

    val context = LocalContext.current
    val canShowLocation = spec.myLocation && listOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
    ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    val properties = rememberThemedMapProperties(MapStyles.mapTypeFor(spec.basemapCode), canShowLocation)
    val uiSettings = remember(spec.zoomControls) {
        MapUiSettings(
            zoomControlsEnabled = spec.zoomControls,
            compassEnabled = true,
            // Google's own "open in Maps" toolbar crowded small maps and repeated the page's toolbar.
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        )
    }
    // Marker images are drawn once per look, so a new theme must draw them again.
    val theme = MaterialTheme.colorScheme.primary.toArgb() to MaterialTheme.colorScheme.surface.toArgb()

    GoogleMap(
        modifier = modifier.onSizeChanged { mapSize = it },
        cameraPositionState = cameraPositionState,
        contentPadding = spec.contentPadding,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = { latLng -> spec.onMapClick?.invoke(GeoPoint(latLng.latitude, latLng.longitude)) },
    ) {
        spec.polylines.forEach { line ->
            Polyline(
                points = line.points.map { it.toLatLng() },
                color = line.color,
                width = with(density) { line.width.toPx() },
            )
        }

        spec.markers.forEach { marker ->
            key(marker.key) {
                val state = remember(marker.position) { MarkerState(position = marker.position.toLatLng()) }
                val onClick = { _: com.google.android.gms.maps.model.Marker ->
                    marker.onClick?.invoke()
                    // False keeps Google's own behaviour too: centre on the pin and show its title.
                    false
                }
                val content = marker.content
                if (content == null) {
                    Marker(state = state, title = marker.title, snippet = marker.snippet, onClick = onClick)
                } else {
                    MarkerComposable(
                        keys = arrayOf(marker.iconKey, theme),
                        state = state,
                        title = marker.title,
                        snippet = marker.snippet,
                        onClick = onClick,
                    ) { content() }
                }
            }
        }

        spec.clusters?.let { clusters ->
            val items = remember(clusters.items) { clusters.items.map(::MarkerClusterItem) }
            Clustering(
                items = items,
                onClusterItemClick = { item ->
                    item.marker.onClick?.invoke()
                    false
                },
                clusterContent = { cluster -> clusters.clusterContent(cluster.size) },
                clusterItemContent = { item -> item.marker.content?.invoke() },
            )
        }
    }
}

/**
 * A marker for the clustering library. Equal while the marker would look and sit the same, so a
 * new list with the same pins does not make the clustering draw them all again.
 */
private class MarkerClusterItem(val marker: MapMarker) : ClusterItem {
    private val latLng = marker.position.toLatLng()
    override fun getPosition(): LatLng = latLng
    override fun getTitle(): String? = marker.title
    override fun getSnippet(): String? = marker.snippet

    override fun equals(other: Any?): Boolean =
        other is MarkerClusterItem && other.marker.key == marker.key &&
            other.marker.iconKey == marker.iconKey && other.marker.position == marker.position

    override fun hashCode(): Int = marker.key.hashCode()
}

private class GoogleCameraDriver(
    private val state: CameraPositionState,
    private val density: Density,
) : AppMapCamera.Driver {

    override suspend fun fit(bounds: GeoBounds, padding: Dp, animate: Boolean) {
        val latLngBounds = LatLngBounds(LatLng(bounds.south, bounds.west), LatLng(bounds.north, bounds.east))
        val update = CameraUpdateFactory.newLatLngBounds(latLngBounds, with(density) { padding.roundToPx() })
        if (animate) state.animate(update, durationMs = 1000) else state.move(update)
    }

    override suspend fun moveTo(center: GeoPoint, zoom: Float, animate: Boolean) {
        val update = CameraUpdateFactory.newLatLngZoom(center.toLatLng(), zoom)
        if (animate) state.animate(update, durationMs = 1000) else state.move(update)
    }
}

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
