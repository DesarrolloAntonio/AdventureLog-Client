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
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.desarrollodroide.adventurelog.feature.ui.components.MaxContentWidth
import com.desarrollodroide.adventurelog.feature.ui.components.MaxDashboardWidth
import com.desarrollodroide.adventurelog.feature.ui.components.DataRailWidth
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import androidx.compose.foundation.layout.IntrinsicSize
import com.desarrollodroide.adventurelog.feature.ui.components.LocationPlaceholder
import androidx.compose.foundation.horizontalScroll

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

    // Wide enough and the leftover space becomes a second column instead of margin: the main
    // column keeps the trip and the photographs, and a 348dp rail carries the numbers. That is
    // the whole point of the layout - a tablet was showing one phone-shaped column with a third
    // of the screen empty beside it.
    //
    // The threshold is the redesign's own width (1fr + 348 + gap). Below it there is no room for
    // two columns that both read well, so everything stacks, which is what a phone had anyway.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val twoColumns = maxWidth >= 1000.dp

        ContentColumn(maxWidth = if (twoColumns) MaxDashboardWidth else MaxContentWidth) {
            if (twoColumns) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MainColumn(
                        featuredTrip = featuredTrip,
                        dashboard = dashboard,
                        onTripClick = onTripClick,
                        onAdventureClick = onAdventureClick,
                        onSeeAllPlaces = onSeeAllPlaces,
                        modifier = Modifier.weight(1f)
                    )
                    DataRail(
                        dashboard = dashboard,
                        otherTrips = otherTrips,
                        today = today,
                        onTripClick = onTripClick,
                        onSeeCalendar = onSeeCalendar,
                        modifier = Modifier.width(DataRailWidth)
                    )
                }
            } else {
                StackedDashboard(
                    onAddPlace = onAddPlace,
                    featuredTrip = featuredTrip,
                    dashboard = dashboard,
                    otherTrips = otherTrips,
                    today = today,
                    onTripClick = onTripClick,
                    onAdventureClick = onAdventureClick,
                    onSeeCalendar = onSeeCalendar,
                    onSeeAllPlaces = onSeeAllPlaces
                )
            }
        }
    }
}

/** The left-hand column on a wide window: the trip you are about to take, then the photographs. */
@Composable
private fun MainColumn(
    featuredTrip: UltraSlimCollection?,
    dashboard: Dashboard,
    onTripClick: (UltraSlimCollection) -> Unit,
    onAdventureClick: (Location) -> Unit,
    onSeeAllPlaces: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier.fillMaxHeight(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (featuredTrip != null) {
            item(key = "trip", span = { GridItemSpan(maxLineSpan) }) {
                TripCard(trip = featuredTrip, onClick = { onTripClick(featuredTrip) })
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
 * The right-hand rail: everything that is a number or a date.
 *
 * It scrolls on its own so a long list of trips cannot drag the photographs off the screen with
 * it, and each block is a card because side by side they would otherwise run together.
 */
@Composable
private fun DataRail(
    dashboard: Dashboard,
    otherTrips: List<UltraSlimCollection>,
    today: LocalDate?,
    onTripClick: (UltraSlimCollection) -> Unit,
    onSeeCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(dashboard, title = "YOUR MAP SO FAR")

        val comingUp = comingUpEntries(otherTrips, dashboard.upcomingEvents, today, onTripClick)
        if (comingUp.isNotEmpty()) {
            SectionHeader(
                title = "Coming up",
                trailing = "See all",
                onTrailingClick = onSeeCalendar
            )
            comingUp.forEach { ComingUpRow(it) }
        }
    }
}

/** A titled block in the rail, on the same white as the stats card beside it. */
@Composable
private fun RailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

/** One column, for phones and foldables: the same blocks, stacked in reading order. */
@Composable
private fun StackedDashboard(
    featuredTrip: UltraSlimCollection?,
    dashboard: Dashboard,
    otherTrips: List<UltraSlimCollection>,
    today: LocalDate?,
    onTripClick: (UltraSlimCollection) -> Unit,
    onAdventureClick: (Location) -> Unit,
    onSeeCalendar: () -> Unit,
    onSeeAllPlaces: () -> Unit,
    onAddPlace: () -> Unit
) {
    val comingUp = comingUpEntries(otherTrips, dashboard.upcomingEvents, today, onTripClick)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (featuredTrip != null) {
            TripCard(
                trip = featuredTrip,
                onClick = { onTripClick(featuredTrip) },
                onAddPlace = onAddPlace
            )
        }

        StatsCard(dashboard)

        if (comingUp.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "Coming up",
                    trailing = "See all",
                    onTrailingClick = onSeeCalendar
                )
                comingUp.forEach { ComingUpRow(it) }
            }
        }

        if (dashboard.recentLocations.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "Recently updated (${dashboard.stats.locationCount})",
                    trailing = "See all",
                    onTrailingClick = onSeeAllPlaces
                )
                // A strip that runs off the edge, so the photographs stay photographs instead of
                // becoming a column of thumbnails a phone screen deep.
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    dashboard.recentLocations.forEach { location ->
                        PlaceMiniCard(location, onClick = { onAdventureClick(location) })
                    }
                }
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
    onAddPlace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inProgress = trip.status == TripStatus.IN_PROGRESS
    val subtitle = tripSubtitle(trip)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // Always the primary container. The featured trip used the secondary one unless it
            // had already started, which is why the hero read as washed-out grey-green instead of
            // the cyan the redesign leads with.
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val image = trip.featuredImage?.takeIf { it.isNotBlank() }
        val imageLoader = LocalImageLoader.current

        // The photograph sits beside the text at every size - a narrow strip on a phone, a third
        // of the card on a tablet. Stacking it above pushed the trip's name and its two actions
        // below the fold, which is the opposite of what a hero card is for.
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            TripCardText(trip, subtitle, onClick, onAddPlace, Modifier.weight(1f))
            if (image != null) {
                // The Box is what keeps the card the height of its text. An Image inside a
                // height(IntrinsicSize.Min) row reports its own natural height, and the card grows
                // to the full size of the photograph - nine hundred dp of Cinque Terre.
                Box(modifier = Modifier.fillMaxHeight().weight(0.37f)) {
                    Image(
                        painter = rememberAsyncImagePainter(model = image, imageLoader = imageLoader),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun TripCardText(
    trip: UltraSlimCollection,
    subtitle: String,
    onOpen: () -> Unit,
    onAddPlace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = trip.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Places and dates on one line, the way the trip would be described out loud.
        val places = when {
            trip.adventureCount == 1 -> "1 place"
            trip.adventureCount > 1 -> "${trip.adventureCount} places"
            else -> null
        }
        val dates = tripDates(trip)
        val line = listOfNotNull(places, dates).joinToString(" · ")
        if (line.isNotEmpty()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text("Open trip", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onAddPlace,
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text("Add a place", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** "05/09 - 09/09", or nothing when the trip has no dates on it. */
private fun tripDates(trip: UltraSlimCollection): String? {
    val from = trip.startDate?.substringBefore('T')?.takeIf { it.isNotBlank() } ?: return null
    val to = trip.endDate?.substringBefore('T')?.takeIf { it.isNotBlank() }
    val short = { d: String -> d.split("-").let { if (it.size == 3) "${it[2]}/${it[1]}" else d } }
    return if (to != null && to != from) "${short(from)} \u2013 ${short(to)}" else short(from)
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
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val stats = dashboard.stats
    val rows = listOf(
        Triple("Countries", stats.visitedCountryCount, stats.totalCountries),
        Triple("Regions", stats.visitedRegionCount, stats.totalRegions),
        Triple("Cities", stats.visitedCityCount, stats.totalCities),
        Triple("Places visited", stats.visitedLocationCount, stats.locationCount)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // No icons. Four of them down the left edge added a column of decoration and pushed the
        // numbers - the only part anyone reads - into the middle of the card.
        BoxWithConstraints {
            val perRow = when {
                maxWidth >= 900.dp -> 4
                maxWidth >= 560.dp -> 2
                else -> 1
            }
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                rows.chunked(perRow).forEach { chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        chunk.forEach { (label, visited, total) ->
                            StatRow(label, visited, total, Modifier.weight(1f))
                        }
                        repeat(perRow - chunk.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    visited: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (total > 0) (visited.toFloat() / total).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            // Monospaced, so four rows of counts line up as a column of figures rather than
            // drifting with the width of each number.
            Text(
                text = visited.grouped(),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " / ${total.grouped()}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
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


/**
 * One dated thing, whether it came from a trip or from the calendar.
 *
 * The redesign folds "Upcoming trips" and "Coming up" into a single list, and it is right to: a
 * trip that starts on the 5th and an event on the 5th are the same kind of fact to whoever is
 * looking, and two separate lists made the reader merge them by eye.
 */
private data class ComingUp(
    val key: String,
    val date: String,
    val title: String,
    val detail: String,
    val onClick: (() -> Unit)?
)

private fun comingUpEntries(
    trips: List<UltraSlimCollection>,
    events: List<CalendarEvent>,
    today: LocalDate?,
    onTripClick: (UltraSlimCollection) -> Unit
): List<ComingUp> {
    val fromTrips = trips.map { trip ->
        ComingUp(
            key = "trip-${trip.id}",
            date = trip.startDate.orEmpty(),
            title = trip.name,
            detail = listOfNotNull(
                tripSubtitle(trip),
                trip.adventureCount.takeIf { it > 0 }
                    ?.let { if (it == 1) "1 place" else "$it places" }
            ).joinToString(" \u00b7 "),
            onClick = { onTripClick(trip) }
        )
    }
    val fromEvents = events.map { event ->
        val detail = listOfNotNull(
            event.locationLabel.takeIf { it.isNotBlank() },
            event.collectionName?.takeIf { it.isNotBlank() }
        ).firstOrNull { it != event.title }.orEmpty()
        ComingUp("event-${event.id}", event.start, event.title, detail, null)
    }
    return (fromTrips + fromEvents).sortedBy { it.date.substringBefore('T') }
}

private val MONTHS = listOf(
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
)

/** A dated row: the day and month in a narrow column, then what happens on it. */
@Composable
private fun ComingUpRow(entry: ComingUp, modifier: Modifier = Modifier) {
    val parts = entry.date.substringBefore('T').split("-")
    val day = parts.getOrNull(2).orEmpty()
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { MONTHS.getOrNull(it - 1) }.orEmpty()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Clickable only when there is somewhere to go. A disabled Card is not "not clickable",
        // it is greyed out and faded, and a calendar entry that leads nowhere was reading as
        // switched off next to a trip that does.
        val clickable = entry.onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
        Row(
            modifier = clickable.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(38.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = day, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = month,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.detail.isNotBlank()) {
                    Text(
                        text = entry.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * A place in the home strip: photograph on top, its four facts underneath on white.
 *
 * Not the card the places list uses. Text laid over a photograph disappears whenever the
 * photograph is pale - the name of a white village against a bright sky - and at this size there
 * is no room to darken it enough to fix that without losing the photograph too.
 */
@Composable
private fun PlaceMiniCard(
    location: Location,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current
    val photo = location.images.firstOrNull()?.image?.takeIf { it.isNotBlank() }

    Card(
        onClick = onClick,
        modifier = modifier.width(150.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (photo != null) {
            Image(
                painter = rememberAsyncImagePainter(model = photo, imageLoader = imageLoader),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(72.dp)
            )
        } else {
            LocationPlaceholder(
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                modifier = Modifier.fillMaxWidth().height(72.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val where = listOfNotNull(
                location.location?.takeIf { it.isNotBlank() }
            ).firstOrNull()
            if (where != null) {
                Text(
                    text = where,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val rating = location.rating?.toInt() ?: 0
            val category = location.category?.displayName?.uppercase().orEmpty()
            if (rating > 0 || category.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (rating > 0) {
                        Text(
                            text = "\u2605".repeat(rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    if (category.isNotBlank()) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
