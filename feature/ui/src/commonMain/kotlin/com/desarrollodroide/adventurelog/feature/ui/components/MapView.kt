package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The two ends of a journey on a map: either, both, or neither may be known yet.
 *
 * A transportation is the one thing in the app with two places rather than one, which is why it
 * does not reuse the single-point map the rest of the app draws.
 */
@Composable
expect fun MapView(
    originLat: Double?,
    originLng: Double?,
    destinationLat: Double?,
    destinationLng: Double?,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
)
