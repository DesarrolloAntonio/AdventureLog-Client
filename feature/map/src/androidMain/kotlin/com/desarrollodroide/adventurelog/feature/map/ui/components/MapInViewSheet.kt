package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.feature.ui.components.PlaceRow
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The places on screen, beside the map on a wide window, as the tablet canvas draws it: the list
 * follows the camera, so panning and zooming narrows or widens it, and a row opens its place.
 */
@Composable
fun MapInViewSheet(
    places: List<Location>,
    total: Int,
    onOpen: (Location) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 6.dp,
        modifier = modifier,
    ) {
        Column(Modifier.padding(top = 18.dp, start = 12.dp, end = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("In view", style = AdventureType.display(28.sp))
                Text(
                    "${places.size} of $total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (places.isEmpty()) {
                Text(
                    "Nothing here. Zoom out or move the map to find your places.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap),
                ) {
                    itemsIndexed(places, key = { _, it -> it.id }) { index, place ->
                        PlaceRow(
                            location = place,
                            shape = SegmentedListDefaults.shapeFor(index, places.size),
                            onClick = { onOpen(place) },
                            showActions = false,
                        )
                    }
                    item { androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 12.dp)) }
                }
            }
        }
    }
}
