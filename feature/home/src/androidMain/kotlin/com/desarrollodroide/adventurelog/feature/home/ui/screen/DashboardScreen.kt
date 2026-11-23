package com.desarrollodroide.adventurelog.feature.home.ui.screen

import com.desarrollodroide.adventurelog.feature.collections.ui.components.InvitesBanner
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.target
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import com.desarrollodroide.adventurelog.feature.home.ui.components.MapSoFarTiles
import com.desarrollodroide.adventurelog.feature.home.ui.components.RecentPlacesCarousel
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
import com.desarrollodroide.adventurelog.feature.ui.components.placeholderColors
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.components.TicketCard
import com.desarrollodroide.adventurelog.feature.ui.components.TicketStub
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onAdventureClick: (Location) -> Unit = { },
    onTripClick: (UltraSlimCollection) -> Unit = { },
    onSeeCalendar: () -> Unit = { },
    onSeeAllPlaces: () -> Unit = { },
    onAddPlace: () -> Unit = { },
    /** The trip card's own "Add a place": the new place goes into that trip (QA HM-09). */
    onAddPlaceToTrip: (UltraSlimCollection) -> Unit = { onAddPlace() },
    onAddCollection: () -> Unit = { },
    onRetry: () -> Unit = { },
    onSeeInvitations: () -> Unit = { },
    onOpenEvent: (EventTarget) -> Unit = { },
    /** "Hi John": the account's first name, from the shell. Null or blank reads "Hi there". */
    firstName: String? = null,
    /** "Your map so far" opens World, where the countries behind the numbers are. */
    onSeeWorld: () -> Unit = { },
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

            is HomeUiState.Success -> Column(Modifier.fillMaxSize()) {
                // The dashboard has carried the count all along and nothing showed it, so an
                // account with an invitation waiting - often a brand-new one, on the first-run
                // screen - had no idea (QA 09, MC-05).
                if (homeUiState.dashboard.inviteCount > 0) {
                    InvitesBanner(
                        count = homeUiState.dashboard.inviteCount,
                        onClick = onSeeInvitations
                    )
                }
                DashboardList(
                    dashboard = homeUiState.dashboard,
                    today = homeUiState.today,
                    onAdventureClick = onAdventureClick,
                    onTripClick = onTripClick,
                    onSeeCalendar = onSeeCalendar,
                    onSeeAllPlaces = onSeeAllPlaces,
                    onAddPlace = onAddPlace,
                    onAddPlaceToTrip = onAddPlaceToTrip,
                    onAddCollection = onAddCollection,
                    onOpenEvent = onOpenEvent,
                    firstName = firstName,
                    onSeeWorld = onSeeWorld,
                    modifier = Modifier.weight(1f)
                )
            }
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
    onAddPlaceToTrip: (UltraSlimCollection) -> Unit,
    onAddCollection: () -> Unit,
    onOpenEvent: (EventTarget) -> Unit,
    firstName: String?,
    onSeeWorld: () -> Unit,
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

    val headline = homeHeadline(firstName, featuredTrip, dashboard.stats.visitedLocationCount)

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
                        headline = headline,
                        featuredTrip = featuredTrip,
                        dashboard = dashboard,
                        onTripClick = onTripClick,
                        onAdventureClick = onAdventureClick,
                        onSeeAllPlaces = onSeeAllPlaces,
                        onAddPlaceToTrip = onAddPlaceToTrip,
                        modifier = Modifier.weight(1f)
                    )
                    DataRail(
                        dashboard = dashboard,
                        onSeeWorld = onSeeWorld,
                        featuredTripId = featuredTrip?.id,
                        otherTrips = otherTrips,
                        today = today,
                        onTripClick = onTripClick,
                        onSeeCalendar = onSeeCalendar,
                        onOpenEvent = onOpenEvent,
                        modifier = Modifier.width(DataRailWidth)
                    )
                }
            } else {
                StackedDashboard(
                    headline = headline,
                    onSeeWorld = onSeeWorld,
                    onAddPlaceToTrip = onAddPlaceToTrip,
                    featuredTrip = featuredTrip,
                    dashboard = dashboard,
                    otherTrips = otherTrips,
                    today = today,
                    onTripClick = onTripClick,
                    onAdventureClick = onAdventureClick,
                    onSeeCalendar = onSeeCalendar,
                    onSeeAllPlaces = onSeeAllPlaces,
                    onOpenEvent = onOpenEvent
                )
            }
        }
    }
}

/** The left-hand column on a wide window: the trip you are about to take, then the photographs. */
@Composable
private fun MainColumn(
    headline: HomeHeadline,
    featuredTrip: UltraSlimCollection?,
    dashboard: Dashboard,
    onTripClick: (UltraSlimCollection) -> Unit,
    onAdventureClick: (Location) -> Unit,
    onSeeAllPlaces: () -> Unit,
    onAddPlaceToTrip: (UltraSlimCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier.fillMaxHeight(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero", span = { GridItemSpan(maxLineSpan) }) { HomeHero(headline) }
        if (featuredTrip != null) {
            item(key = "trip", span = { GridItemSpan(maxLineSpan) }) {
                FeaturedTrip(
                    trip = featuredTrip,
                    onClick = { onTripClick(featuredTrip) },
                    onAddPlace = { onAddPlaceToTrip(featuredTrip) }
                )
            }
        }
        if (dashboard.recentLocations.isNotEmpty()) {
            item(key = "recent-header", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    // Not the library total: the strip shows a handful, and "(22)" over three cards read as a
                    // count of what was there (QA HM-09). "See all" is the way to the rest.
                    title = "Recently updated",
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
    onSeeWorld: () -> Unit,
    featuredTripId: String?,
    otherTrips: List<UltraSlimCollection>,
    today: LocalDate?,
    onTripClick: (UltraSlimCollection) -> Unit,
    onSeeCalendar: () -> Unit,
    onOpenEvent: (EventTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        YourMapSoFar(dashboard, onSeeWorld)

        val comingUp = comingUpEntries(otherTrips, dashboard.upcomingEvents, today, onTripClick, featuredTripId, onOpenEvent)
        if (comingUp.isNotEmpty()) {
            SectionHeader(
                title = "Coming up",
                trailing = "See all",
                onTrailingClick = onSeeCalendar
            )
            comingUp.forEach { ComingUpRow(it) }
        }

        // The next trip that still has room in it. A count of places is a fact; this is the one
        // thing on the screen asking to be done, so it is the only tinted card in the rail.
        val toPlan = otherTrips.firstOrNull { it.adventureCount in 1..4 }
        if (toPlan != null) {
            Card(
                onClick = { onTripClick(toPlan) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = toPlan.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${toPlan.adventureCount} places planned so far.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Plan the trip \u2192",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
    headline: HomeHeadline,
    onSeeWorld: () -> Unit,
    featuredTrip: UltraSlimCollection?,
    dashboard: Dashboard,
    otherTrips: List<UltraSlimCollection>,
    today: LocalDate?,
    onTripClick: (UltraSlimCollection) -> Unit,
    onAdventureClick: (Location) -> Unit,
    onSeeCalendar: () -> Unit,
    onSeeAllPlaces: () -> Unit,
    onAddPlaceToTrip: (UltraSlimCollection) -> Unit,
    onOpenEvent: (EventTarget) -> Unit
) {
    val comingUp = comingUpEntries(otherTrips, dashboard.upcomingEvents, today, onTripClick, featuredTrip?.id, onOpenEvent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHero(headline)

        if (featuredTrip != null) {
            FeaturedTrip(
                trip = featuredTrip,
                onClick = { onTripClick(featuredTrip) },
                onAddPlace = { onAddPlaceToTrip(featuredTrip) }
            )
        }

        YourMapSoFar(dashboard, onSeeWorld)

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
                    // Not the library total: the strip shows a handful, and "(22)" over three cards read as a
                    // count of what was there (QA HM-09). "See all" is the way to the rest.
                    title = "Recently updated",
                    trailing = "See all",
                    onTrailingClick = onSeeAllPlaces
                )
                RecentPlacesCarousel(dashboard.recentLocations, onClick = onAdventureClick)
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
            style = AdventureType.display(28.sp),
            modifier = Modifier.weight(1f, fill = false)
        )
        if (trailing != null && onTrailingClick != null) {
            // A TextButton, for its 48dp: the padded text it replaces measured 47dp tall.
            TextButton(onClick = onTrailingClick) {
                Text(text = trailing, style = MaterialTheme.typography.labelLarge)
            }
        } else if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
 * What Home leads with, the canvas's "Hi John, your next trip starts in 8 days": [lead], then the
 * part that changes inked in the primary colour ([emphasis]), then [tail].
 */
data class HomeHeadline(val lead: String, val emphasis: String, val tail: String = "")

/** The headline for [trip] (in progress or the next one), or for no trip at all. */
internal fun homeHeadline(firstName: String?, trip: UltraSlimCollection?, visitedPlaces: Int): HomeHeadline {
    val hi = "Hi ${firstName?.trim()?.takeIf { it.isNotEmpty() } ?: "there"}, "
    if (trip == null) {
        val places = if (visitedPlaces == 1) "1 place" else "$visitedPlaces places"
        return HomeHeadline(hi, places, " visited so far")
    }
    if (trip.status == TripStatus.IN_PROGRESS) return HomeHeadline("${hi}you are in ", trip.name)
    return when (val days = trip.daysUntilStart) {
        null -> HomeHeadline("${hi}your next trip is ", trip.name)
        0 -> HomeHeadline("${hi}your next trip starts ", "today")
        1 -> HomeHeadline("${hi}your next trip starts ", "tomorrow")
        else -> HomeHeadline("${hi}your next trip starts in ", "$days days")
    }
}

/** "5 SEP", as a ticket prints a date; null when there is no date to print. */
internal fun ticketDate(date: String?): String? {
    val parts = date?.substringBefore('T')?.split("-")?.takeIf { it.size == 3 } ?: return null
    val month = parts[1].toIntOrNull()?.let { MONTHS.getOrNull(it - 1) } ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return "$day $month"
}

/** The headline: condensed and heavy, the changing part in the primary colour. */
@Composable
private fun HomeHero(headline: HomeHeadline, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Text(
        text = buildAnnotatedString {
            append(headline.lead)
            withStyle(SpanStyle(color = accent)) { append(headline.emphasis) }
            append(headline.tail)
        },
        style = AdventureType.display(44.sp),
        modifier = modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

/**
 * The trip the user is on, or the next one they will be on, as a ticket: photo, name, then the
 * stubs it would have printed on it - from, to, how many places.
 */
@Composable
private fun FeaturedTrip(
    trip: UltraSlimCollection,
    onClick: () -> Unit,
    onAddPlace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val image = trip.featuredImage?.takeIf { it.isNotBlank() }
    val imageLoader = LocalImageLoader.current
    val stubs = listOfNotNull(
        ticketDate(trip.startDate)?.let { TicketStub("FROM", it) },
        ticketDate(trip.endDate)?.takeIf { trip.endDate != trip.startDate }?.let { TicketStub("TO", it) },
        TicketStub("PLACES", trip.adventureCount.toString()),
    )
    TicketCard(
        onClick = onClick,
        modifier = modifier,
        photo = {
            if (image != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = image, imageLoader = imageLoader),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LocationPlaceholder(icon = null, modifier = Modifier.fillMaxSize())
            }
        },
        title = trip.name,
        // The canvas prints the trip's region here; a collection has no place of its own, and
        // its description is the only line it carries.
        subtitle = trip.description.lineSequence().firstOrNull()?.trim().orEmpty(),
        stubs = stubs,
        badge = {
            // Adding to the trip from Home: the new place goes into this collection (QA HM-09).
            // A "+" alone, where the canvas has the status chip: with its words the button took
            // half the row and broke the trip's name over two lines.
            FilledTonalIconButton(onClick = onAddPlace, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add a place")
            }
        }
    )
}

/**
 * "Your map so far": the heading links to World, where the countries behind the numbers are, and
 * the four figures sit under it. Countries visited only - the dashboard counts no planned ones.
 */
@Composable
private fun YourMapSoFar(dashboard: Dashboard, onSeeWorld: () -> Unit) {
    val stats = dashboard.stats
    val places = if (stats.locationCount == 1) "1 place" else "${stats.locationCount} places"
    val countries = if (stats.visitedCountryCount == 1) "1 country" else "${stats.visitedCountryCount} countries"
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Your map so far")
        TextButton(
            onClick = onSeeWorld,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Text("$places \u00b7 $countries visited", style = MaterialTheme.typography.labelLarge)
        }
        MapSoFarTiles(dashboard.stats)
    }
}

/** "05/09 - 09/09", or nothing when the trip has no dates on it. */
private fun tripDates(trip: UltraSlimCollection): String? {
    val from = trip.startDate?.substringBefore('T')?.takeIf { it.isNotBlank() } ?: return null
    val to = trip.endDate?.substringBefore('T')?.takeIf { it.isNotBlank() }
    val short = { d: String -> d.split("-").let { if (it.size == 3) "${it[2]}/${it[1]}" else d } }
    return if (to != null && to != from) "${short(from)} \u2013 ${short(to)}" else short(from)
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
internal data class ComingUp(
    val key: String,
    val date: String,
    val title: String,
    val detail: String,
    val onClick: (() -> Unit)?
)

internal fun comingUpEntries(
    trips: List<UltraSlimCollection>,
    events: List<CalendarEvent>,
    today: LocalDate?,
    onTripClick: (UltraSlimCollection) -> Unit,
    featuredTripId: String? = null,
    onOpenEvent: (EventTarget) -> Unit = {}
): List<ComingUp> {
    // The calendar sends every dated collection as an event of its own. A trip already listed here,
    // or already on the card above, came back a second time as that event - each trip twice, and
    // the one in progress again under its past start date (measured).
    val tripsShown = trips.map { it.id }.toSet() + listOfNotNull(featuredTripId)
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
    val fromEvents = events.filterNot { it.type == "collection" && it.collectionId in tripsShown }.map { event ->
        val detail = listOfNotNull(
            event.locationLabel.takeIf { it.isNotBlank() },
            event.collectionName?.takeIf { it.isNotBlank() }
        ).firstOrNull { it != event.title }.orEmpty()
        // An event row opens what the calendar opens for it - its place, or the trip it belongs to.
        // It used to open nothing while the trip rows beside it did (QA HM-09).
        val target = event.target()
        ComingUp("event-${event.id}", event.start, event.title, detail, target?.let { { onOpenEvent(it) } })
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
