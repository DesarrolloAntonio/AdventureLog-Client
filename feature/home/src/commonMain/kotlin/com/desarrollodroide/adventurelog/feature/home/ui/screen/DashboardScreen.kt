package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.clickable

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onAdventureClick: (Location) -> Unit = { },
    onTripClick: (UltraSlimCollection) -> Unit = { },
    onSeeCalendar: () -> Unit = { },
    onSeeAllPlaces: () -> Unit = { },
    onAddPlace: () -> Unit = { },
    onAddCollection: () -> Unit = { },
    onRetry: () -> Unit = { },
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (homeUiState) {
            is HomeUiState.Loading -> LoadingDialog(isLoading = true, showMessage = false)

            // The commonest reason to land here is no signal, which is over the moment it is
            // over - so offer the retry rather than making someone restart the app to get it.
            // The calendar has said "Try again" all along; this screen only stated the problem.
            is HomeUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = homeUiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry) { Text("Try again") }
            }

            is HomeUiState.Success -> DashboardList(
                dashboard = homeUiState.dashboard,
                today = homeUiState.today,
                onAdventureClick = onAdventureClick,
                onTripClick = onTripClick,
                onSeeCalendar = onSeeCalendar,
                onSeeAllPlaces = onSeeAllPlaces,
                onAddPlace = onAddPlace,
                onAddCollection = onAddCollection
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
    today: LocalDate?,
    onAdventureClick: (Location) -> Unit,
    onTripClick: (UltraSlimCollection) -> Unit,
    onSeeCalendar: () -> Unit,
    onSeeAllPlaces: () -> Unit,
    onAddPlace: () -> Unit,
    onAddCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    // An in-progress trip outranks a future one: if the user is travelling right now, that is the
    // single most useful thing the screen can lead with.
    val featuredTrip = dashboard.activeTrip ?: dashboard.upcomingTrips.firstOrNull()

    // The featured card above already covers the soonest trip (or the one in progress); the rest
    // of upcomingTrips would otherwise never be reachable from Home.
    val otherTrips = if (dashboard.activeTrip != null) {
        dashboard.upcomingTrips
    } else {
        dashboard.upcomingTrips.drop(1)
    }

    // Hiding empty sections is right until every section is empty, and then the first screen of a
    // new account is a card of zeros above a blank half-page. A counter reading 0 / 250 is not an
    // invitation; it is a scoreboard for a game nobody has started.
    val nothingYet = dashboard.stats.locationCount == 0 &&
        dashboard.stats.tripsCount == 0 &&
        featuredTrip == null &&
        dashboard.upcomingEvents.isEmpty() &&
        dashboard.recentLocations.isEmpty()

    if (nothingYet) {
        FirstRun(
            onAddPlace = onAddPlace,
            onAddCollection = onAddCollection,
            modifier = modifier
        )
        return
    }

    // Only the place cards at the bottom gain from a second column; everything above them is a
    // full-width row that would read worse cut in half, so those span the whole line.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (featuredTrip != null) {
            item(key = "trip", span = { GridItemSpan(maxLineSpan) }) {
                // Spanned so it leads the screen, but capped: stretched over a tablet it stops
                // being a card and becomes a letterbox band with a title floating in it.
                TripCard(
                    trip = featuredTrip,
                    onClick = { onTripClick(featuredTrip) },
                    modifier = Modifier.widthIn(max = 720.dp)
                )
            }
        }

        item(key = "stats", span = { GridItemSpan(maxLineSpan) }) {
            StatsCard(dashboard)
        }

        if (otherTrips.isNotEmpty()) {
            item(key = "trips-header", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = "Upcoming trips")
            }
            items(otherTrips, key = { "trip-${it.id}" }) { trip ->
                TripRow(trip = trip, onClick = { onTripClick(trip) })
            }
        }

        if (dashboard.upcomingEvents.isNotEmpty()) {
            item(key = "events-header", span = { GridItemSpan(maxLineSpan) }) {
                // Home shows the next few; the calendar has the rest, and this is where anyone
                // looking at what is coming would think to ask for more of it.
                SectionHeader(
                    title = "Coming up",
                    trailing = "See all",
                    onTrailingClick = onSeeCalendar
                )
            }
            items(dashboard.upcomingEvents, key = { "event-${it.id}" }) { event ->
                EventRow(event, today)
            }
        }

        if (dashboard.recentLocations.isNotEmpty()) {
            item(key = "recent-header", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Recently updated (${dashboard.stats.locationCount})",
                    trailing = "See all",
                    onTrailingClick = onSeeAllPlaces
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

/**
 * What a brand new account sees instead of its own emptiness.
 *
 * Both routes out are offered because both are real starting points: some people log the place
 * they just came back from, others open a collection for a trip they are about to take.
 */
@Composable
private fun FirstRun(
    onAddPlace: () -> Unit,
    onAddCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Your journal starts here",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Add somewhere you have been, or somewhere you are going. Once a trip takes " +
                "shape, group its places into a collection.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onAddPlace,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add a place")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onAddCollection,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create a collection")
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String? = null,
    onTrailingClick: (() -> Unit)? = null,
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
                color = if (onTrailingClick != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = if (onTrailingClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onTrailingClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                } else {
                    Modifier
                }
            )
        }
    }
}

private fun tripSubtitle(trip: UltraSlimCollection): String = when {
    trip.status == TripStatus.IN_PROGRESS -> "Happening now"
    trip.daysUntilStart == 0 -> "Starts today"
    trip.daysUntilStart == 1 -> "Starts tomorrow"
    trip.daysUntilStart != null -> "In ${trip.daysUntilStart} days"
    else -> "Upcoming"
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
    val subtitle = tripSubtitle(trip)

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
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
 * A trip further out than the featured one - same information as TripCard, in the compact row
 * shape the events list already uses, since a full-size card per trip would push everything else
 * off the first screen.
 */
@Composable
private fun TripRow(
    trip: UltraSlimCollection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (trip.adventureCount > 0) {
                    Text(
                        text = if (trip.adventureCount == 1) "1 place" else "${trip.adventureCount} places",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = tripSubtitle(trip),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        // The screen sits on a full-bleed photo, so the cards take the brightest fill on the
        // tonal scale. That alone separates them - no shadow needed, and a shadow on a flat
        // white card over a photograph only ever looks like a sticker.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val rows = listOf(
            Triple(Icons.Default.Public, "Countries", stats.visitedCountryCount to stats.totalCountries),
            Triple(Icons.Default.Terrain, "Regions", stats.visitedRegionCount to stats.totalRegions),
            Triple(Icons.Default.LocationCity, "Cities", stats.visitedCityCount to stats.totalCities),
            Triple(Icons.Default.Place, "Places visited", stats.visitedLocationCount to stats.locationCount)
        )

        // A progress bar says "how far along" by how much of it is filled, and past about 400dp
        // the eye stops reading a proportion and starts reading a very long line. On a tablet the
        // card is wider than that on its own, so the four stats pair off into columns instead of
        // each one being stretched the full width of the window.
        BoxWithConstraints {
            val perRow = when {
                maxWidth >= 1100.dp -> 4
                maxWidth >= 560.dp -> 2
                else -> 1
            }
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rows.chunked(perRow).forEach { chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        chunk.forEach { (icon, label, counts) ->
                            StatRow(
                                icon = icon,
                                label = label,
                                visited = counts.first,
                                total = counts.second,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Keep the last row's columns aligned with the one above when the count
                        // is odd, rather than letting a lone stat stretch across both.
                        repeat(perRow - chunk.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
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
                style = MaterialTheme.typography.bodyMedium,
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
    today: LocalDate?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                // A collection whose only dated thing carries its own name printed it twice,
                // one line under the other. The calendar already drops a label that repeats the
                // title; home was the copy that did not.
                val detail = listOfNotNull(
                    event.locationLabel.takeIf { it.isNotBlank() },
                    event.collectionName?.takeIf { it.isNotBlank() }
                ).firstOrNull { it != event.title }
                if (detail != null) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            EventWhen(event, today)
        }
    }
}

/**
 * A multi-day event that has already begun is still "coming up" - it has not finished - but
 * printing its start date reads as a date in the past. Say it is running, and when it ends.
 */
@Composable
private fun EventWhen(
    event: CalendarEvent,
    today: LocalDate?,
    modifier: Modifier = Modifier
) {
    val started = today != null && event.start.isNotBlank() &&
        event.start.substringBefore('T') <= today.toString()
    val ends = event.end.substringBefore('T')
    val running = started && ends >= (today?.toString() ?: "")

    if (running) {
        Column(modifier = modifier, horizontalAlignment = Alignment.End) {
            Text(
                text = "NOW",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (ends != today?.toString()) {
                Text(
                    text = "to ${ends.toShortDate()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Text(
            text = event.start.toShortDate(),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
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
