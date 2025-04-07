package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A common interface for the map view.
 * Platform-specific implementations will be provided in corresponding source sets.
 */
@Composable
expect fun MapView(
    latitude: String,
    longitude: String,
    modifier: Modifier = Modifier
)
