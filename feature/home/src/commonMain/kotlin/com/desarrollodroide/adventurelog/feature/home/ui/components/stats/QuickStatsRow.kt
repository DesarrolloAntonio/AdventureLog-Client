package com.desarrollodroide.adventurelog.feature.home.ui.components.stats

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Row of statistics cards for quick overview
 */
@Composable
fun QuickStatsRow(adventureCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        StatsCard(
            icon = Icons.Filled.Explore,
            title = "Adventures",
            value = adventureCount.toString(),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        StatsCard(
            icon = Icons.Filled.Place,
            title = "Countries",
            value = "12",
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        StatsCard(
            icon = Icons.Filled.Photo,
            title = "Photos",
            value = "347",
            modifier = Modifier.weight(1f)
        )
    }
}