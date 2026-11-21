package com.desarrollodroide.adventurelog.feature.home.ui.screen

import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.target
import kotlinx.datetime.LocalDate

// Split out of DashboardScreen.kt (Compose, androidMain): plain logic, tested on every platform.
// tripSubtitle was private there; internal now, because the dashboard's composables still use it.

internal fun tripSubtitle(trip: UltraSlimCollection): String = when {
    trip.status == TripStatus.IN_PROGRESS -> "Happening now"
    trip.daysUntilStart == 0 -> "Starts today"
    trip.daysUntilStart == 1 -> "Starts tomorrow"
    trip.daysUntilStart != null -> "In ${trip.daysUntilStart} days"
    else -> "Upcoming"
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
