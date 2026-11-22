package com.desarrollodroide.adventurelog.feature.collections.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.feature.map.ui.components.AdventureMapView

/**
 * The collection's places on one map.
 *
 * It reuses the app's own map rather than growing a second one, so clustering, the marker popup
 * and the platform implementations are the same here as on the Map tab. The regions and cities
 * layers are off: those belong to the whole account, and drawing them over four places would say
 * something about the collection that is not true.
 *
 * Places with no coordinates cannot be drawn, and the screen says how many rather than quietly
 * showing fewer pins than the collection has places - the same footnote the Map tab carries.
 */
@Composable
fun CollectionMapView(
    locations: List<Location>,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val placed = locations.filter { !it.latitude.isNullOrBlank() && !it.longitude.isNullOrBlank() }
    val missing = locations.size - placed.size

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (placed.isEmpty()) {
            MapCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Nothing to put on a map",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "None of this collection's places has coordinates yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Column
        }

        AdventureMapView(
            locations = placed,
            visitedRegions = emptyList(),
            visitedCities = emptyList(),
            showCities = false,
            showRegions = false,
            onAdventureClick = onLocationClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(18.dp))
        )

        if (missing > 0) {
            Text(
                text = if (missing == 1) "1 place has no coordinates and is not shown"
                else "$missing places have no coordinates and are not shown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MapCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}
