package com.desarrollodroide.adventurelog.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** The time of day in an ISO date-time, as "HH:mm", or null when it carries none. */
fun timeOfDay(isoDateTime: String?): String? =
    isoDateTime?.substringAfter('T', "")?.take(5)?.takeIf { it.isNotEmpty() }

/**
 * True when this bound carries no meaningful time of day.
 *
 * Midnight is how an all-day visit is stored now; end-of-day is the older form the server still
 * accepts and normalises. Both have to read as all-day, or a visit saved before the app started
 * sending midnight comes back looking like it runs from 00:00 to 23:59.
 */
fun isAllDayBound(isoDateTime: String?): Boolean =
    timeOfDay(isoDateTime).let { it == null || it == "00:00" || it == "23:59" }

/**
 * A visit is all day only when neither end has a time. Starting and ending on the same date is not
 * enough: the place page showed a visit from 10:30 to 18:45 as "All day" and hid its times (measured).
 */
fun isAllDayVisit(startDate: String?, endDate: String?): Boolean =
    isAllDayBound(startDate) && isAllDayBound(endDate ?: startDate)

/** A visit bound as shown to a person: "2025-07-01 10:30", or the date alone when it has no time. */
fun visitBoundText(isoDateTime: String?): String {
    val date = isoDateTime?.substringBefore('T').orEmpty()
    return timeOfDay(isoDateTime)?.let { "$date $it" } ?: date
}

/**
 * What the server should store for a timed visit chosen at [time] ("HH:mm") on [date] in [zone]:
 * that moment in UTC, as the web sends it (REF `lib/dateUtils.ts`, `toUTCDatetime`). The apps
 * used to send the wall-clock time with a "Z" on it, and the web showed it an hour or two late.
 */
@OptIn(ExperimentalTime::class)
fun visitStored(date: String, time: String, zone: String): String =
    LocalDateTime(LocalDate.parse(date), LocalTime.parse(time)).toInstant(timeZoneOrUtc(zone)).toString()

/**
 * A stored timed visit bound as it was chosen: [stored] (UTC) read in [zone], "yyyy-MM-ddTHH:mm",
 * as the web shows it. A value with no time, no zone, or that cannot be read comes back as it was.
 */
@OptIn(ExperimentalTime::class)
fun visitLocalDateTime(stored: String?, zone: String?): String? {
    if (stored == null || zone.isNullOrBlank() || 'T' !in stored) return stored
    val instant = runCatching { Instant.parse(stored) }.getOrNull() ?: return stored
    val local = instant.toLocalDateTime(timeZoneOrUtc(zone))
    return "${local.date}T${local.time.toString().take(5)}"
}

private fun timeZoneOrUtc(zone: String): TimeZone = runCatching { TimeZone.of(zone) }.getOrDefault(TimeZone.UTC)
