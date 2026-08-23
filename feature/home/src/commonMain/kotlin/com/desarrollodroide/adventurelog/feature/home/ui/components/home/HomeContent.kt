package com.desarrollodroide.adventurelog.feature.home.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onAdventureClick: (Location) -> Unit = { },
    onTripClick: (UltraSlimCollection) -> Unit = { },
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (homeUiState) {
            is HomeUiState.Loading -> LoadingDialog(isLoading = true, showMessage = false)

            is HomeUiState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(homeUiState.message, style = MaterialTheme.typography.bodyLarge)
            }

            is HomeUiState.Success -> DashboardList(
                dashboard = homeUiState.dashboard,
                onAdventureClick = onAdventureClick,
                onTripClick = onTripClick
            )
        }
    }
}

/**
 * Sections appear only when they have something to say. The web dashboard fills its grid with
 * "nothing here yet" cards because it has columns to keep square; a single mobile column has no
 * such obligation, so an empty block simply takes no room.
 */
@Composable
private fun DashboardList(
    dashboard: Dashboard,
    onAdventureClick: (Location) -> Unit,
    onTripClick: (UltraSlimCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    // An in-progress trip outranks a future one: if the user is travelling right now, that is the
    // single most useful thing the screen can lead with.
    val featuredTrip = dashboard.activeTrip ?: dashboard.upcomingTrips.firstOrNull()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (featuredTrip != null) {
            item(key = "trip") {
                TripCard(trip = featuredTrip, onClick = { onTripClick(featuredTrip) })
            }
        }

        item(key = "stats") {
            StatsCard(dashboard)
        }

        if (dashboard.upcomingEvents.isNotEmpty()) {
            item(key = "events-header") {
                SectionHeader("Coming up")
            }
            items(dashboard.upcomingEvents, key = { "event-${it.id}" }) { event ->
                EventRow(event)
            }
        }

        if (dashboard.recentLocations.isNotEmpty()) {
            item(key = "recent-header") {
                SectionHeader(
                    title = "Recently updated",
                    trailing = "${dashboard.stats.locationCount}"
                )
            }
            items(dashboard.recentLocations, key = { "loc-${it.id}" }) { location ->
                AdventureItem(
                    location = location,
                    onClick = { onAdventureClick(location) },
                    showMenu = false
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * The trip the user is on, or the next one they will be on.
 */
@Composable
private fun TripCard(
    trip: UltraSlimCollection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inProgress = trip.status == TripStatus.IN_PROGRESS
    val subtitle = when {
        inProgress -> "Happening now"
        trip.daysUntilStart == 0 -> "Starts today"
        trip.daysUntilStart == 1 -> "Starts tomorrow"
        trip.daysUntilStart != null -> "In ${trip.daysUntilStart} days"
        else -> "Upcoming"
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inProgress) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = trip.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (trip.adventureCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (trip.adventureCount == 1) "1 place" else "${trip.adventureCount} places",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Four figures, each against the total it is a fraction of. A bare "8" says nothing; "8 of 5,322"
 * is the whole point of the number.
 */
@Composable
private fun StatsCard(
    dashboard: Dashboard,
    modifier: Modifier = Modifier
) {
    val stats = dashboard.stats

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatRow(
                icon = Icons.Default.Public,
                label = "Countries",
                visited = stats.visitedCountryCount,
                total = stats.totalCountries
            )
            StatRow(
                icon = Icons.Default.Terrain,
                label = "Regions",
                visited = stats.visitedRegionCount,
                total = stats.totalRegions
            )
            StatRow(
                icon = Icons.Default.LocationCity,
                label = "Cities",
                visited = stats.visitedCityCount,
                total = stats.totalCities
            )
            StatRow(
                icon = Icons.Default.Place,
                label = "Places visited",
                visited = stats.visitedLocationCount,
                total = stats.locationCount
            )
        }
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    visited: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (total > 0) (visited.toFloat() / total).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = visited.grouped(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " / ${total.grouped()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = event.icon, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val detail = listOfNotNull(
                event.locationLabel.takeIf { it.isNotBlank() },
                event.collectionName?.takeIf { it.isNotBlank() }
            ).firstOrNull()
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = event.start.toShortDate(),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 153728 -> "153,728". The totals run into six figures, and unbroken digits are hard to read. */
private fun Int.grouped(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()

/**
 * The server sends ISO 8601, either a plain date or a full timestamp. Only the day is worth the
 * width here, so this takes the date half rather than pulling in a full datetime parser.
 */
private fun String.toShortDate(): String {
    val date = substringBefore('T')
    val parts = date.split('-')
    return if (parts.size == 3) "${parts[2]}/${parts[1]}" else date
}
