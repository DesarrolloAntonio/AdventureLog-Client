package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LocationMapSection(
    latitude: String?,
    longitude: String?,
    onMapClick: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
)
