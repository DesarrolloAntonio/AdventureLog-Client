package com.desarrollodroide.adventurelog.feature.ui.map

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.feature.ui.map.maplibre.MapLibreMap
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatformTools

/**
 * A pin on the map.
 *
 * @param key identifies the marker, and is what a tap reports.
 * @param iconKey identifies how it looks: markers with the same [iconKey] share one drawn image,
 *   so pass something that changes whenever [content] would draw differently.
 * @param title shown when the marker is tapped and [onClick] is null (Google shows it as an info
 *   window whenever it is set).
 * @param content the pin, as a composable; null for the engine's standard pin.
 */
data class MapMarker(
    val key: String,
    val position: GeoPoint,
    val title: String? = null,
    val snippet: String? = null,
    val iconKey: Any = key,
    val onClick: (() -> Unit)? = null,
    val content: (@Composable () -> Unit)? = null,
)

/**
 * Markers that merge into one bubble where they crowd each other, and split as the map zooms in.
 * Two hundred pins over one country pile into an unreadable blob otherwise.
 */
data class MarkerClusters(
    val items: List<MapMarker>,
    val clusterContent: @Composable (count: Int) -> Unit,
)

data class MapPolyline(val points: List<GeoPoint>, val color: Color, val width: Dp)

/** Everything an [AppMap] was asked to draw, handed to whichever engine draws it. */
class AppMapSpec(
    val camera: AppMapCamera,
    val markers: List<MapMarker>,
    val clusters: MarkerClusters?,
    val polylines: List<MapPolyline>,
    /** Room kept clear for controls floating over the map: framing and logos stay outside it. */
    val contentPadding: PaddingValues,
    val onMapClick: ((GeoPoint) -> Unit)?,
    /** The area on screen whenever the camera settles: the view's corners, less the start padding. */
    val onVisibleBoundsChanged: ((GeoBounds) -> Unit)?,
    /** Draws the user's position; needs the location permission, which the app does not ask for yet. */
    val myLocation: Boolean,
    val zoomControls: Boolean,
    /** The account's basemap code (`MapStyles`). */
    val basemapCode: String?,
)

/** The Google Maps engine. Only the play flavor has one; it registers it in Koin. */
interface GoogleMapBackend {
    /** Whether the Maps SDK can start on this phone. Asked once per process and cached. */
    fun isAvailable(context: Context): Boolean

    @Composable
    fun Map(spec: AppMapSpec, modifier: Modifier)
}

/**
 * The app's map. Google Maps or MapLibre draws it, depending on the "Map engine" setting and on
 * whether Google can run here at all; the screen never knows which.
 */
@Composable
fun AppMap(
    camera: AppMapCamera,
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    clusters: MarkerClusters? = null,
    polylines: List<MapPolyline> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(),
    onMapClick: ((GeoPoint) -> Unit)? = null,
    onVisibleBoundsChanged: ((GeoBounds) -> Unit)? = null,
    myLocation: Boolean = false,
    zoomControls: Boolean = false,
) {
    val spec = AppMapSpec(
        camera = camera,
        markers = markers,
        clusters = clusters,
        polylines = polylines,
        contentPadding = contentPadding,
        onMapClick = onMapClick,
        onVisibleBoundsChanged = onVisibleBoundsChanged,
        myLocation = myLocation,
        zoomControls = zoomControls,
        basemapCode = rememberMapStyleCode(),
    )
    val choice = rememberMapEngineChoice()
    val google = choice.googleBackend
    if (choice.renderer == MapRenderer.GOOGLE && google != null) {
        google.Map(spec, modifier)
    } else {
        MapLibreMap(spec, modifier)
    }
}

/** The engine setting, what Google can do here, and the engine that results. */
class MapEngineChoice(
    val preference: MapEngine,
    val googleStatus: GoogleMapsStatus,
    internal val googleBackend: GoogleMapBackend?,
    private val store: MapEngineStore?,
) {
    val renderer: MapRenderer get() = resolveMapRenderer(preference, googleStatus)

    fun set(engine: MapEngine) {
        store?.set(engine)
    }
}

@Composable
fun rememberMapEngineChoice(): MapEngineChoice {
    // No Koin in a @Preview or a screen test: MapLibre on Auto, which draws a grey box in previews.
    val koin = KoinPlatformTools.defaultContext().getOrNull()
    val store = remember(koin) { koin?.getOrNull<MapEngineStore>() }
    val backend = remember(koin) { koin?.getOrNull<GoogleMapBackend>() }
    val context = LocalContext.current
    val status = remember(backend) {
        when {
            backend == null -> GoogleMapsStatus.NOT_INSTALLED
            backend.isAvailable(context) -> GoogleMapsStatus.AVAILABLE
            else -> GoogleMapsStatus.UNAVAILABLE
        }
    }
    val preference by (store?.engine ?: remember { MutableStateFlow(MapEngine.AUTO) }).collectAsState()
    return MapEngineChoice(preference, status, backend, store)
}

/**
 * The account's "Default map style" (a `MapStyles` code).
 *
 * The web says this style is used "as the default base map on every map", so every map in the app
 * reads it here rather than having each screen thread it down: it is ambient account state, not a
 * property of any one caller. It comes from the session, so changing it on the web changes it here
 * on the next refresh.
 */
@Composable
fun rememberMapStyleCode(): String? {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) return null
    val userRepository = koinInject<UserRepository>()
    val user by userRepository.getUserSession().collectAsState(initial = userRepository.activeSession)
    return user?.mapStyle
}
