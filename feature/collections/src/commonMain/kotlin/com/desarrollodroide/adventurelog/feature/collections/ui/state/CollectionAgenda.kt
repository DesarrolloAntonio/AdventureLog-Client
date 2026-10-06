package com.desarrollodroide.adventurelog.feature.collections.ui.state

import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.datetime.LocalDate

/** One dated thing in a collection, whatever kind of thing it is. */
data class AgendaEntry(
    val day: LocalDate,
    val title: String,
    val subtitle: String?,
    val kind: AgendaKind
)

enum class AgendaKind { VISIT, CHECK_IN, CHECK_OUT, TRANSPORT }

data class AgendaDay(val day: LocalDate, val entries: List<AgendaEntry>)

/**
 * The collection's dated items, by day.
 *
 * A collection's calendar is not a separate resource: the dates are already on the places' visits,
 * on lodging's check-in and check-out, and on a transportation's departure. What the view adds is
 * putting them in one order, which is the thing none of the five tabs can show.
 *
 * Undated items are left out rather than bucketed under today - a place with no visit yet is a
 * plan, and putting it on a date the user did not choose would be inventing one.
 */
fun Collection.agenda(): List<AgendaDay> {
    val entries = buildList {
        locations.forEach { place ->
            place.visits.forEach { visit ->
                val day = dayOf(visit.startDate) ?: return@forEach
                add(
                    AgendaEntry(
                        day = day,
                        title = place.name,
                        subtitle = place.location?.takeIf { it.isNotBlank() },
                        kind = AgendaKind.VISIT
                    )
                )
            }
        }
        lodging.forEach { stay ->
            dayOf(stay.checkIn)?.let {
                add(AgendaEntry(it, stay.name, "Check in", AgendaKind.CHECK_IN))
            }
            dayOf(stay.checkOut)?.let {
                add(AgendaEntry(it, stay.name, "Check out", AgendaKind.CHECK_OUT))
            }
        }
        transportations.forEach { leg ->
            val day = dayOf(leg.date) ?: return@forEach
            val route = listOfNotNull(leg.fromLocation, leg.toLocation)
                .filter { it.isNotBlank() }
                .takeIf { it.size == 2 }
                ?.joinToString(" → ")
            add(AgendaEntry(day, leg.name, route, AgendaKind.TRANSPORT))
        }
    }

    // Sorted by hand rather than with toSortedMap, which is a JVM-only extension and does not
    // exist on the iOS target.
    return entries
        .groupBy { it.day }
        .entries
        .sortedBy { it.key }
        .map { (day, ofThatDay) -> AgendaDay(day, ofThatDay) }
}
