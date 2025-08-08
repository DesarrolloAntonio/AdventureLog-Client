package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

/**
 * A common interface for the adventure map view.
 * Platform-specific implementations will be provided in corresponding source sets.
 */
@Composable
expect fun AdventureMapView(
    adventures: List<Adventure>,
    visitedRegions: List<VisitedRegion>,
    showRegions: Boolean,
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier = Modifier
)
