package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

/**
 * A common interface for the adventure map view.
 * Platform-specific implementations will be provided in corresponding source sets.
 */
@Composable
expect fun AdventureMapView(
    locations: List<Location>,
    visitedRegions: List<VisitedRegion>,
    showRegions: Boolean,
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier = Modifier
)
