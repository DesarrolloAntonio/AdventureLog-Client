package com.desarrollodroide.adventurelog.feature.ui.map.maplibre

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.desarrollodroide.adventurelog.feature.ui.map.AppMapCamera
import com.desarrollodroide.adventurelog.feature.ui.map.AppMapSpec
import com.desarrollodroide.adventurelog.feature.ui.map.GeoBounds
import com.desarrollodroide.adventurelog.feature.ui.map.GeoPoint
import com.desarrollodroide.adventurelog.feature.ui.map.MapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraFit
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.plus
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.LocationIndicatorLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.rememberLocationState
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MissingImageResolver
import org.maplibre.compose.map.ResolvedStyleImage
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.DpPadding
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

/** How long the attribution stays open before folding: the OSMF guidelines' five seconds. */
private val AttributionOpenFor = 5.seconds

/**
 * The MapLibre engine behind [com.desarrollodroide.adventurelog.feature.ui.map.AppMap]: the
 * account's basemap, the app's markers and lines on top.
 *
 * Markers are composables, and MapLibre draws images, so each distinct look is drawn once into a
 * bitmap when the map first asks for it (the same thing Google's `MarkerComposable` does). Image
 * ids carry the theme, so switching to dark draws them again in the new colours.
 */
@Composable
internal fun MapLibreMap(spec: AppMapSpec, modifier: Modifier) {
    val context = LocalContext.current
    remember { MapLibreSetup.ensure(context.applicationContext) }

    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.5f
    val baseStyle = remember(spec.basemapCode, dark) {
        when (val basemap = basemapFor(spec.basemapCode, dark)) {
            is Basemap.StyleUrl -> BaseStyle.Uri(basemap.url)
            is Basemap.Raster -> BaseStyle.Json(basemap.toStyleJson())
        }
    }
    val theme = remember(colors) { "%08x%08x".format(colors.primary.toArgb(), colors.surface.toArgb()) }
    val images = remember(spec.markers, spec.clusters, theme) { MarkerImages(spec, theme) }
    var label by remember { mutableStateOf<MapMarker?>(null) }
    // A label whose marker has left the map (a layer switched off, a filter) goes with it.
    LaunchedEffect(images) {
        if (label?.key?.let(images.byKey::containsKey) == false) label = null
    }
    val scope = rememberCoroutineScope()
    // Open as the map appears, folded after five seconds or the first touch (see MapControls).
    var attributionOpen by remember { mutableStateOf(true) }
    LaunchedEffect(attributionOpen) {
        if (attributionOpen) {
            delay(AttributionOpenFor)
            attributionOpen = false
        }
    }

    val onMarkerTap: (String) -> Unit = { key ->
        attributionOpen = false
        images.byKey[key]?.let { marker ->
            val onClick = marker.onClick
            if (onClick != null) {
                label = null
                onClick()
            } else if (marker.title != null) {
                label = marker
            }
        }
    }

    val host = LocalView.current as ViewGroup
    val compositionContext = rememberCompositionContext()
    val rasterizer = remember(host, compositionContext) { ComposableRasterizer(host, compositionContext) }
    // Pins whose image the loaded style already holds. A point only joins the map once its image is
    // there: an image that arrives after its tile was laid out stays blank until the camera moves,
    // which on a still map (a place's page) is never.
    var readyPins by remember { mutableStateOf(emptySet<String>()) }

    val mapState = rememberMapState(
        baseStyle = baseStyle,
        initialCameraPosition = CameraPosition(
            center = spec.camera.initialCenter.toPosition(),
            zoom = spec.camera.initialZoom.toMapLibreZoom(),
        ),
    ) {
        spec.polylines.forEachIndexed { index, line ->
            val source = rememberGeoJsonSource(
                GeoJsonData.Features(LineString(line.points.map { it.toPosition() })),
            )
            LineLayer(
                id = "app-line-$index",
                source = source,
                color = const(line.color),
                width = const(line.width),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round),
            )
        }

        if (spec.markers.isNotEmpty()) {
            val source = rememberGeoJsonSource(GeoJsonData.Features(images.features(spec.markers, readyPins)))
            SymbolLayer(
                id = "app-markers",
                source = source,
                iconImage = image(feature["icon"].asString()),
                iconAnchor = const(SymbolAnchor.Bottom),
                // Every marker is drawn, as Google draws them, rather than hidden where two touch.
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                onClick = { features ->
                    features.firstOrNull()?.properties?.get("key")?.jsonPrimitive?.content?.let(onMarkerTap)
                    ClickResult.Consume
                },
            )
        }

        spec.clusters?.let { clusters ->
            val map = LocalMapState.current
            val source = rememberGeoJsonSource(GeoJsonData.Features(images.features(clusters.items, readyPins))) {
                cluster = true
                // maps-utils' default renderer leaves groups of fewer than four as single pins.
                clusterMinPoints = 4
            }
            SymbolLayer(
                id = "app-clusters",
                source = source,
                filter = feature.has("point_count"),
                // The count is a number: converted, not asserted, or the id comes out empty.
                iconImage = image(const("cluster:") + feature["point_count"].convertToString() + const(":$theme")),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                onClick = { features ->
                    // Zoom in on the bubble until it splits, as the web does.
                    val point = features.firstOrNull()?.geometry as? Point
                    if (point != null && map != null) scope.launch {
                        map.animateCamera(CameraUpdate(center = point.coordinates, zoom = map.cameraPosition.zoom + 2.0))
                    }
                    ClickResult.Consume
                },
            )
            SymbolLayer(
                id = "app-cluster-items",
                source = source,
                filter = !feature.has("point_count"),
                iconImage = image(feature["icon"].asString()),
                iconAnchor = const(SymbolAnchor.Bottom),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                onClick = { features ->
                    features.firstOrNull()?.properties?.get("key")?.jsonPrimitive?.content?.let(onMarkerTap)
                    ClickResult.Consume
                },
            )
        }

        if (spec.myLocation && hasLocationPermission(context)) {
            LocationIndicatorLayer(id = "app-location", locationState = rememberLocationState())
        }
    }

    // Every pin look goes into the style before its points do; a new style (theme, basemap) starts
    // empty, so it gets them again.
    val loadState = mapState.style.loadState
    LaunchedEffect(mapState, images, loadState) {
        if (loadState != StyleLoadState.Ready) {
            readyPins = emptySet()
            return@LaunchedEffect
        }
        val prepared = images.pinContent.filterKeys { it !in readyPins }.mapNotNull { (id, content) ->
            rasterizer.render(content)?.let { id to ResolvedStyleImage.fromBitmap(it.asImageBitmap()) }
        }.toMap()
        if (prepared.isEmpty()) return@LaunchedEffect
        mapState.style.images.setAll(prepared)
        readyPins = readyPins + prepared.keys
    }

    // Cluster bubbles carry a count nobody knows in advance: drawn when the map asks for one.
    // Clusters only change as the camera moves, so a late image is drawn on the next frame.
    val currentImages by rememberUpdatedState(images)
    DisposableEffect(mapState, rasterizer) {
        mapState.missingImageResolver = MissingImageResolver { request ->
            val content = currentImages.contentFor(request.id) ?: return@MissingImageResolver null
            val bitmap = withContext(Dispatchers.Main) { rasterizer.render(content) } ?: return@MissingImageResolver null
            ResolvedStyleImage.fromBitmap(bitmap.asImageBitmap())
        }
        onDispose { mapState.missingImageResolver = null }
    }

    LaunchedEffect(mapState) {
        snapshotFlow { mapState.isCameraMoving && mapState.cameraMoveReason == CameraMoveReason.Gesture }
            .filter { it }
            .collect { attributionOpen = false }
    }

    DisposableEffect(spec.camera, mapState) {
        val driver = MapLibreCameraDriver(mapState)
        spec.camera.attach(driver)
        onDispose { spec.camera.detach(driver) }
    }

    spec.onVisibleBoundsChanged?.let { report -> VisibleBoundsReporter(mapState, spec, report) }

    MaplibreMap(
        modifier = modifier,
        state = mapState,
        viewportInsets = spec.contentPadding,
        interactions = MapInteractions {
            callbacks {
                click {
                    // Only taps that no marker took: those pick a point, and close a label.
                    onUnhandled { event ->
                        label = null
                        attributionOpen = false
                        event.position?.let { position ->
                            spec.onMapClick?.invoke(GeoPoint(position.latitude, normalizeLongitude(position.longitude)))
                        }
                        ClickResult.Consume
                    }
                }
            }
        },
        overlay = {
            MapControls(mapState, attributionOpen, onToggleAttribution = { attributionOpen = !attributionOpen })
            label?.let { marker ->
                MarkerLabel(
                    marker = marker,
                    modifier = Modifier
                        .placedAt(marker.position.toPosition(), alignment = Alignment.BottomCenter)
                        .padding(bottom = 52.dp),
                )
            }
            if (spec.zoomControls) {
                ZoomButtons(
                    onZoomIn = { scope.launch { mapState.animateCamera(CameraUpdate(zoom = mapState.cameraPosition.zoom + 1.0)) } },
                    onZoomOut = { scope.launch { mapState.animateCamera(CameraUpdate(zoom = mapState.cameraPosition.zoom - 1.0)) } },
                    // Mid-height: the bottom corners hold the logo and the attribution.
                    modifier = Modifier.align(Alignment.CenterEnd).padding(12.dp),
                )
            }
        },
    )
}

/**
 * Whenever the camera comes to rest, the first fit included, says what is on screen: the corners
 * of the view itself, less only the start padding (where a side sheet covers the map). A composable
 * of its own because it reads the camera, which changes every frame of a pan; only this recomposes.
 */
@Composable
private fun VisibleBoundsReporter(mapState: MapState, spec: AppMapSpec, report: (GeoBounds) -> Unit) {
    val startInset = spec.contentPadding.calculateStartPadding(LocalLayoutDirection.current)
    val settled by remember(mapState) { derivedStateOf { !mapState.isCameraMoving } }
    val size by remember(mapState) { derivedStateOf { mapState.viewport?.size } }
    val currentReport by rememberUpdatedState(report)
    LaunchedEffect(settled, size, startInset) {
        val viewSize = size
        if (!settled || viewSize == null) return@LaunchedEffect
        val corners = listOf(
            DpOffset(startInset, 0.dp), DpOffset(viewSize.width, 0.dp),
            DpOffset(startInset, viewSize.height), DpOffset(viewSize.width, viewSize.height),
        ).mapNotNull { mapState.positionFromScreenLocation(it) }
        if (corners.size == 4) currentReport(viewBounds(corners))
    }
}

/** The images a map's markers need, and the GeoJSON that points at them. */
private class MarkerImages(spec: AppMapSpec, private val theme: String) {
    private val allMarkers = spec.markers + spec.clusters?.items.orEmpty()
    private val clusterContent = spec.clusters?.clusterContent
    val byKey: Map<String, MapMarker> = allMarkers.associateBy { it.key }
    /** Each pin look, by image id. */
    val pinContent: Map<String, @Composable () -> Unit> =
        allMarkers.associate { idOf(it) to (it.content ?: { StandardPin() }) }

    private fun idOf(marker: MapMarker) =
        if (marker.content == null) "pin:standard:$theme" else "pin:${marker.iconKey}:$theme"

    /** The points of [markers] whose image is in the style already ([ready]). */
    fun features(markers: List<MapMarker>, ready: Set<String>) = FeatureCollection(
        markers.filter { idOf(it) in ready }.map { marker ->
            Feature(
                geometry = Point(marker.position.toPosition()),
                properties = buildJsonObject {
                    put("key", JsonPrimitive(marker.key))
                    put("icon", JsonPrimitive(idOf(marker)))
                },
            )
        },
    )

    fun contentFor(id: String): (@Composable () -> Unit)? {
        pinContent[id]?.let { return it }
        val count = id.removePrefix("cluster:").substringBefore(':').toIntOrNull()
        val content = clusterContent
        return if (id.startsWith("cluster:") && count != null && content != null) ({ content(count) }) else null
    }
}

private class MapLibreCameraDriver(private val state: MapState) : AppMapCamera.Driver {

    override suspend fun fit(bounds: GeoBounds, padding: androidx.compose.ui.unit.Dp, animate: Boolean) {
        val box = BoundingBox(west = bounds.west, south = bounds.south, east = bounds.east, north = bounds.north)
        // Pins stand on their point, so the top edge keeps room for one, or the northernmost is cut.
        val fitPadding = DpPadding(left = padding, top = padding + PinHeight, right = padding, bottom = padding)
        val camera = state.cameraForBounds(box, CameraFit(fitPadding = fitPadding))
            ?: return
        // A single place would otherwise fill the screen at the deepest zoom there is.
        val update = camera.copy(zoom = min(camera.zoom, MaxFitZoom)).toCameraUpdate()
        if (animate) state.animateCamera(update, CameraAnimation.Ease { duration = 1.seconds })
        else state.setCameraPosition(camera.copy(zoom = min(camera.zoom, MaxFitZoom)))
    }

    override suspend fun moveTo(center: GeoPoint, zoom: Float, animate: Boolean) {
        val update = CameraUpdate(center = center.toPosition(), zoom = zoom.toMapLibreZoom())
        if (animate) state.animateCamera(update, CameraAnimation.Ease { duration = 1.seconds })
        else state.setCameraPosition(state.cameraPosition.copy(center = center.toPosition(), zoom = zoom.toMapLibreZoom()))
    }

    private companion object {
        /** Google's 16, a few streets. */
        const val MaxFitZoom = 15.0
        val PinHeight = 48.dp
    }
}

/** The map's standard pin, for markers that bring no look of their own. */
@Composable
private fun StandardPin() {
    Icon(
        imageVector = Icons.Filled.Place,
        contentDescription = null,
        tint = Color(0xFFEA4335),
        modifier = Modifier.size(44.dp),
    )
}

/** What Google shows as an info window: the tapped marker's title, and its snippet. */
@Composable
private fun MarkerLabel(marker: MapMarker, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.inverseSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = marker.title.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            color = colors.inverseOnSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        marker.snippet?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inverseOnSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ZoomButtons(onZoomIn: () -> Unit, onZoomOut: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton(onClick = onZoomIn) { Icon(Icons.Filled.Add, contentDescription = "Zoom in") }
        FilledTonalIconButton(onClick = onZoomOut) { Icon(Icons.Filled.Remove, contentDescription = "Zoom out") }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

private fun GeoPoint.toPosition() = Position(longitude = longitude, latitude = latitude)

/** MapLibre's tiles are 512 wide where Google's are 256, so its zoom levels sit one lower. */
private fun Float.toMapLibreZoom(): Double = (this - 1f).toDouble().coerceAtLeast(0.0)

private fun normalizeLongitude(longitude: Double): Double = ((longitude + 540.0) % 360.0) - 180.0

/**
 * The box around the view's corners. MapLibre repeats the world sideways, so a view over the
 * Pacific reports longitudes past 180; folded back, its west edge ends up east of its east edge,
 * which is how [GeoBounds] says it crosses the antimeridian.
 */
private fun viewBounds(corners: List<Position>): GeoBounds {
    val west = corners.minOf { it.longitude }
    val east = corners.maxOf { it.longitude }
    val south = corners.minOf { it.latitude }
    val north = corners.maxOf { it.latitude }
    return if (east - west >= 360.0) GeoBounds(south, -180.0, north, 180.0)
    else GeoBounds(south, normalizeLongitude(west), north, normalizeLongitude(east))
}
