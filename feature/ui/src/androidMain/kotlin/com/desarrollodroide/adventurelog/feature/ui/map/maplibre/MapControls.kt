package com.desarrollodroide.adventurelog.feature.ui.map.maplibre

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.maplibre.compose.map.MapState
import org.maplibre.compose.overlay.AttributionDefaults
import org.maplibre.compose.overlay.AttributionLinks
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.LocalViewportInsets
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.overlay.MaplibreLogo
import org.maplibre.compose.overlay.attributions

/**
 * What `MapOverlay.Default` draws (scale bar, compass, logo), with an attribution that folds.
 *
 * The library's attribution opens with the map and folds only when the map is dragged. A small map
 * nobody drags (a place's page, the location picker, a route) kept it open for good, over the
 * MapLibre logo and over any pin near the bottom edge. This one folds to its (i) button after five
 * seconds, on a drag or on a tap: the OSMF attribution guidelines allow each, as long as the button
 * brings the text back.
 */
@Composable
internal fun MapControls(mapState: MapState, attributionOpen: Boolean, onToggleAttribution: () -> Unit) {
    val insets = LocalViewportInsets.current
    Box(
        Modifier
            .fillMaxSize()
            .padding(insets)
            .consumeWindowInsets(insets)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(MapOverlay.Spacing),
    ) {
        DisappearingScaleBar(
            metersPerDp = { mapState.viewport?.metersPerDpAtCenter ?: 0.0 },
            zoom = { mapState.cameraPosition.zoom },
            modifier = Modifier.align(Alignment.TopStart),
        )
        DisappearingCompassButton(modifier = Modifier.align(Alignment.TopEnd))
        // One row, so the open attribution gets only the width the logo leaves and wraps in it.
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            MaplibreLogo()
            Box(
                Modifier.weight(1f).padding(start = MapOverlay.Spacing),
                contentAlignment = Alignment.BottomEnd,
            ) {
                FoldingAttribution(mapState, attributionOpen, onToggleAttribution)
            }
        }
    }
}

@Composable
private fun FoldingAttribution(mapState: MapState, open: Boolean, onToggle: () -> Unit) {
    val style = mapState.style
    val attributions by remember(style) { derivedStateOf { style.attributions() } }
    if (attributions.isEmpty()) return
    val container by animateColorAsState(if (open) AttributionDefaults.ContainerColor else Color.Transparent)
    Row(
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            // A drag across it must not pan the map underneath.
            .pointerInput(Unit) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = open,
            modifier = Modifier.weight(1f, fill = false),
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End),
        ) {
            AttributionLinks(
                attributions = attributions,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                breakWithinAttribution = true,
            )
        }
        AttributionDefaults.button(onToggle)
    }
}
