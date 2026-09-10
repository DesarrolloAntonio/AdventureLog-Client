package com.desarrollodroide.adventurelog.feature.collections.ui.state

import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.datetime.LocalDate

/**
 * Everything the Statistics view shows, worked out from the collection alone.
 *
 * The server sends no roll-up for a collection, and it does not need to: a collection arrives with
 * its places, transportations, lodging, notes and checklists already inside it, so every figure
 * here is a count over data the screen is holding anyway. Keeping the arithmetic in one pure
 * function rather than inside the composable is what makes it checkable - the figures are the
 * whole point of the view, and a wrong one is invisible until someone counts by hand.
 */
data class CollectionStats(
    val placesVisited: Int,
    val placesTotal: Int,
    val photos: Int,
    val countries: List<String>,
    val regions: List<String>,
    val cities: List<String>,
    val travellers: Int,
    val totalDays: Int,
    val activeDays: Int,
    val visits: Int,
    val nights: Int,
    val stays: Int,
    val notes: Int,
    val checklists: Int,
    val transportations: Int
) {
    /** 0f..1f, and 0f rather than a division by zero for a collection with no places. */
    val visitedFraction: Float
        get() = if (placesTotal == 0) 0f else placesVisited.toFloat() / placesTotal

    val isComplete: Boolean get() = placesTotal > 0 && placesVisited == placesTotal
}

/**
 * A date the server sent, reduced to a day.
 *
 * Dates arrive in two shapes - a bare `2025-09-12` and a full `2025-09-12T08:30:00Z` - so the day
 * is the first ten characters of either. Anything else is not a date this can count, and returns
 * null rather than guessing.
 */
internal fun dayOf(raw: String?): LocalDate? {
    val day = raw?.take(10) ?: return null
    return runCatching { LocalDate.parse(day) }.getOrNull()
}

fun Collection.stats(): CollectionStats {
    val visits = locations.flatMap { it.visits }

    /**
     * Footprints, photos and geography are a record of the trip as *travelled*, so they count
     * only places that have been visited - which is why the web reads 0 countries for a trip to
     * Peru that has not happened yet, while the same collection's three photographs sit in the
     * hero carousel above. Counting every place instead would make the Statistics view answer a
     * different question from the one its labels ask, and disagree with the web on the same
     * account.
     */
    val visited = locations.filter { it.isVisited }

    /**
     * A day counts as active when something is on it: a visit, a night's stay, a journey. Lodging
     * spans a range and marks every day it covers, which is why this is a set of days rather than
     * a count of items - two visits on one afternoon are one active day.
     */
    val activeDays = buildSet {
        visits.forEach { visit ->
            val from = dayOf(visit.startDate)
            val to = dayOf(visit.endDate) ?: from
            if (from != null && to != null) addAll(daysBetween(from, to))
        }
        lodging.forEach { stay ->
            val from = dayOf(stay.checkIn)
            val to = dayOf(stay.checkOut) ?: from
            if (from != null && to != null) addAll(daysBetween(from, to))
        }
        transportations.forEach { leg ->
            val from = dayOf(leg.date)
            val to = dayOf(leg.endDate) ?: from
            if (from != null && to != null) addAll(daysBetween(from, to))
        }
    }

    val start = dayOf(startDate)
    val end = dayOf(endDate) ?: start

    return CollectionStats(
        placesVisited = locations.count { it.isVisited },
        placesTotal = locations.size,
        photos = visited.sumOf { it.images.size },
        countries = visited.mapNotNull { it.country?.name }.distinct().sorted(),
        regions = visited.mapNotNull { it.region?.name }.distinct().sorted(),
        cities = visited.mapNotNull { it.city?.name }.distinct().sorted(),
        // The owner is a traveller too, and is never in sharedWith.
        travellers = sharedWith.size + 1,
        totalDays = if (start != null && end != null) daysBetween(start, end).size else 0,
        activeDays = activeDays.size,
        visits = visits.size,
        nights = lodging.sumOf { stay ->
            val from = dayOf(stay.checkIn)
            val to = dayOf(stay.checkOut)
            if (from != null && to != null && to > from) daysBetween(from, to).size - 1 else 0
        },
        stays = lodging.size,
        notes = notes.size,
        checklists = checklists.size,
        transportations = transportations.size
    )
}

/**
 * Every day from [from] to [to] inclusive, so a trip of the 12th to the 20th is nine days and not
 * eight. A range the wrong way round is one day rather than an empty list or a hang.
 */
private fun daysBetween(from: LocalDate, to: LocalDate): List<LocalDate> {
    if (to <= from) return listOf(from)
    val days = mutableListOf<LocalDate>()
    var day = from
    while (day <= to) {
        days += day
        day = day.plusDays(1)
    }
    return days
}

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days)
