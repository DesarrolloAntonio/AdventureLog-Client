package com.desarrollodroide.adventurelog.feature.collections.ui.state

import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import kotlinx.datetime.LocalDate

/**
 * An itinerary entry with the thing it points at already found.
 *
 * The server's entry carries no content - only a kind and an id - so every one of them has to be
 * looked up in the collection before it can be drawn. An entry whose item is gone (deleted from
 * the collection but still placed on a day) resolves to null and is dropped rather than drawn as
 * a blank row.
 */
data class ItineraryItem(
    val entryId: String,
    val kind: ItineraryItemKind,
    val title: String,
    val subtitle: String?,
    val order: Int
)

/** One day of the trip: what it is called, and what is on it. */
data class ItineraryDay(
    val date: LocalDate,
    /** 1-based, as the web numbers them: "Day 3 of 13". */
    val dayNumber: Int,
    val totalDays: Int,
    val name: String,
    val description: String,
    val items: List<ItineraryItem>
) {
    /** What to call this day: its own name where it has one, its number where it has not. */
    val label: String get() = name.ifBlank { "Day $dayNumber" }
}

/**
 * The whole view's data: the trip's days in order, plus the things that belong to the trip without
 * belonging to any day of it.
 */
data class CollectionItinerary(
    val days: List<ItineraryDay>,
    val tripContext: List<ItineraryItem>,
    /** True when nothing has been placed anywhere. The auto-generate offer belongs here only. */
    val isEmpty: Boolean,
    /**
     * True when there are dated records to build an itinerary out of. The server refuses to
     * generate from nothing, so offering the button then would only produce an error.
     */
    val canAutoGenerate: Boolean
)

fun Collection.itinerary(): CollectionItinerary {
    val start = dayOf(startDate)
    val end = dayOf(endDate) ?: start
    val dates = if (start != null && end != null) daysFrom(start, end) else emptyList()

    val names = itineraryDays.associateBy { it.date.take(10) }
    val byDate = itinerary.filter { !it.isGlobal }.groupBy { it.date?.take(10) }

    val days = dates.mapIndexed { index, date ->
        val key = date.toString()
        val note = names[key]
        ItineraryDay(
            date = date,
            dayNumber = index + 1,
            totalDays = dates.size,
            name = note?.name.orEmpty(),
            description = note?.description.orEmpty(),
            items = (byDate[key] ?: emptyList()).toItems(this)
        )
    }

    return CollectionItinerary(
        days = days,
        tripContext = itinerary.filter { it.isGlobal }.toItems(this),
        isEmpty = itinerary.isEmpty(),
        canAutoGenerate = itinerary.isEmpty() && hasDatedRecords()
    )
}

/** Whether anything in the collection carries a date the server could build a day out of. */
private fun Collection.hasDatedRecords(): Boolean =
    locations.any { place -> place.visits.any { dayOf(it.startDate) != null } } ||
        lodging.any { dayOf(it.checkIn) != null } ||
        transportations.any { dayOf(it.date) != null } ||
        notes.any { dayOf(it.date) != null } ||
        checklists.any { dayOf(it.date) != null }

private fun List<ItineraryEntry>.toItems(collection: Collection): List<ItineraryItem> =
    sortedBy { it.order }.mapNotNull { entry -> entry.resolve(collection) }

private fun ItineraryEntry.resolve(collection: Collection): ItineraryItem? {
    fun item(title: String, subtitle: String?) =
        ItineraryItem(entryId = id, kind = kind, title = title, subtitle = subtitle, order = order)

    return when (kind) {
        // A visit is a visit *to* a place, and the place is what anybody reading the day wants to
        // see - so both kinds resolve through the collection's locations.
        ItineraryItemKind.LOCATION, ItineraryItemKind.VISIT ->
            collection.locations.firstOrNull { it.id == itemId || it.visits.any { v -> v.id == itemId } }
                ?.let { item(it.name, it.location?.takeIf(String::isNotBlank)) }

        ItineraryItemKind.TRANSPORTATION ->
            collection.transportations.firstOrNull { it.id == itemId }?.let { leg ->
                val route = listOfNotNull(leg.fromLocation, leg.toLocation)
                    .filter { it.isNotBlank() }
                    .takeIf { it.size == 2 }
                    ?.joinToString(" → ")
                item(leg.name, route)
            }

        ItineraryItemKind.LODGING ->
            collection.lodging.firstOrNull { it.id == itemId }
                ?.let { item(it.name, it.location?.takeIf(String::isNotBlank)) }

        ItineraryItemKind.NOTE ->
            collection.notes.firstOrNull { it.id == itemId }
                ?.let { item(it.name, it.content?.takeIf(String::isNotBlank)) }

        ItineraryItemKind.CHECKLIST ->
            collection.checklists.firstOrNull { it.id == itemId }?.let { list ->
                val done = list.items.count { it.isChecked }
                item(list.name, if (list.items.isEmpty()) null else "$done of ${list.items.size} done")
            }
    }
}

/** Every day from [from] to [to] inclusive; a range the wrong way round is one day. */
private fun daysFrom(from: LocalDate, to: LocalDate): List<LocalDate> {
    if (to <= from) return listOf(from)
    val days = mutableListOf<LocalDate>()
    var day = from
    while (day <= to) {
        days += day
        day = LocalDate.fromEpochDays(day.toEpochDays() + 1)
    }
    return days
}
