package com.desarrollodroide.adventurelog.feature.collections.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.collections.ui.state.CollectionStats
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The collection as figures.
 *
 * Four groups, in the order the web puts them: how far the trip got, where it went, how long it
 * lasted, and what it holds. Each figure is a count over the collection itself - see
 * [com.desarrollodroide.adventurelog.feature.collections.ui.state.stats].
 */
@Composable
fun CollectionStatsView(
    stats: CollectionStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressCard(stats)
        StatGrid(
            title = "Trip",
            entries = listOf(
                StatEntry("Footprints", stats.placesVisited.toString(), "Places visited"),
                StatEntry("Photos", stats.photos.toString(), "Captured"),
                StatEntry(
                    label = "Countries",
                    value = stats.countries.size.toString(),
                    caption = "${stats.regions.size} regions, ${stats.cities.size} cities"
                ),
                StatEntry("Travellers", stats.travellers.toString(), "On this trip")
            )
        )

        if (stats.countries.isNotEmpty() || stats.regions.isNotEmpty() || stats.cities.isNotEmpty()) {
            GeographyCard(stats)
        }

        StatGrid(
            title = "Timeline",
            entries = listOf(
                StatEntry("Total days", stats.totalDays.toString(), "Trip window"),
                StatEntry("Active days", stats.activeDays.toString(), "Busy"),
                StatEntry("Visits", stats.visits.toString(), "Recorded"),
                StatEntry(
                    label = "Nights",
                    value = stats.nights.toString(),
                    caption = if (stats.stays == 1) "1 stay" else "${stats.stays} stays"
                )
            )
        )

        StatGrid(
            title = "Contents",
            entries = listOf(
                StatEntry("Photos", stats.photos.toString(), "Captured"),
                StatEntry("Notes", stats.notes.toString(), "Written"),
                StatEntry("Checklists", stats.checklists.toString(), "Lists"),
                StatEntry("Transport", stats.transportations.toString(), "Segments"),
                StatEntry("Lodging", stats.stays.toString(), "Stays"),
                StatEntry("Places", stats.placesTotal.toString(), "Total")
            )
        )
    }
}

private data class StatEntry(val label: String, val value: String, val caption: String)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressCard(stats: CollectionStats) {
    StatsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Visited", style = AdventureType.display(24.sp))
            Text(
                text = "${stats.placesVisited} / ${stats.placesTotal}",
                style = AdventureType.display(24.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // The wave only when there is something to visit: at 0/0 an indicator still paints a
        // track that reads as "nothing done yet" for a collection where there is nothing to do.
        if (stats.placesTotal > 0) {
            LinearWavyProgressIndicator(
                progress = { stats.visitedFraction },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }

        Text(
            text = when {
                stats.placesTotal == 0 -> "No places in this collection yet"
                stats.isComplete -> "Every place visited"
                else -> "${stats.placesTotal - stats.placesVisited} still planned"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeographyCard(stats: CollectionStats) {
    StatsCard {
        Text(text = "Where it went", style = AdventureType.display(24.sp))
        GeographyGroup("Countries", stats.countries)
        GeographyGroup("Regions", stats.regions)
        GeographyGroup("Cities", stats.cities)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeographyGroup(title: String, names: List<String>) {
    if (names.isEmpty()) return
    Text(
        text = "$title (${names.size})",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        names.forEach { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatGrid(title: String, entries: List<StatEntry>) {
    StatsCard {
        Text(text = title, style = AdventureType.display(24.sp))
        // Whatever divides the group evenly, so no tile is left alone on a line the width of the
        // card - four go two by two, six go three by three.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = if (entries.size % 3 == 0) 3 else 2
        ) {
            entries.forEach { entry ->
                // Every tile on a line is as tall as the tallest, so a caption that wraps to two
                // lines does not leave its neighbours floating above a gap.
                StatTile(entry, Modifier.weight(1f).fillMaxRowHeight())
            }
        }
    }
}

@Composable
private fun StatTile(entry: StatEntry, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = entry.value,
            style = AdventureType.display(32.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = entry.caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Lowest, as the collection page's own cards are.
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}
