package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * A transport's dates, the way the web keeps them (REF `lib/dateUtils.ts`, `isAllDay`).
 *
 * The server stores one UTC instant per end and the zone it was chosen in beside it. An all-day
 * value is that day at 00:00 UTC; a timed one is the local time in its zone, converted to UTC.
 * The form works in local dates and times, so everything here turns one into the other.
 */
@OptIn(ExperimentalTime::class)
object TransportDates {

    /** A local date and, for a timed value, a local time ("HH:mm"). */
    data class Local(val date: String, val time: String?)

    fun isAllDay(stored: String): Boolean =
        stored.isBlank() || Regex("""^\d{4}-\d{2}-\d{2}(T00:00(:00(\.0+)?)?(Z|[+-]00:00)?)?$""").matches(stored)

    /** What the form shows for [stored], read in [zone]. Empty when there is nothing, or it cannot be read. */
    fun toLocal(stored: String, zone: String, allDay: Boolean): Local {
        if (stored.isBlank()) return Local("", null)
        if (allDay) return Local(stored.take(10), null)
        val local = parse(stored)?.toLocalDateTime(timeZone(zone)) ?: return Local(stored.take(10), null)
        return Local(local.date.toString(), local.time.toString().take(5))
    }

    /** What the server should store for a local [date] (and [time]) chosen in [zone]. */
    fun toStored(date: String, time: String?, zone: String, allDay: Boolean): String {
        val day = LocalDate.parse(date)
        if (allDay) return "${day}T00:00:00Z"
        val clock = time?.takeIf { it.isNotBlank() }?.let(LocalTime::parse) ?: LocalTime(0, 0)
        return LocalDateTime(day, clock).toInstant(timeZone(zone)).toString()
    }

    /** The same moment rewritten when the All day switch changes: its local day, at midnight. */
    fun switchAllDay(stored: String, zone: String, toAllDay: Boolean): String {
        if (stored.isBlank()) return stored
        val day = toLocal(stored, zone, allDay = !toAllDay).date.ifBlank { return stored }
        return toStored(day, null, zone, toAllDay)
    }

    /** Whether [arrival] is an earlier moment than [departure]. False when either is empty or unreadable. */
    fun arrivesBeforeDeparting(departure: String, arrival: String): Boolean {
        val from = parse(departure) ?: return false
        val to = parse(arrival) ?: return false
        return to < from
    }

    private fun parse(stored: String): Instant? = runCatching {
        Instant.parse(if (stored.length == 10) "${stored}T00:00:00Z" else stored)
    }.getOrNull()

    private fun timeZone(zone: String): TimeZone = runCatching { TimeZone.of(zone) }.getOrDefault(TimeZone.UTC)
}
