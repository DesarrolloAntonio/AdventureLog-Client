package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MapView(
    originLat: Double?,
    originLng: Double?,
    destinationLat: Double?,
    destinationLng: Double?,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement actual map functionality with platform-specific implementations
    // For now, show a placeholder
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (originLat != null && originLng != null && destinationLat != null && destinationLng != null) {
                "Route Map\nFrom: ($originLat, $originLng)\nTo: ($destinationLat, $destinationLng)"
            } else if (originLat != null && originLng != null) {
                "Map\nOrigin: ($originLat, $originLng)"
            } else if (destinationLat != null && destinationLng != null) {
                "Map\nDestination: ($destinationLat, $destinationLng)"
            } else {
                "Map View\n(No coordinates set)"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
