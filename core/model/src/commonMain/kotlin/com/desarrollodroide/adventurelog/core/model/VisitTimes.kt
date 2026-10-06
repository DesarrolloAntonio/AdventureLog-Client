package com.desarrollodroide.adventurelog.core.model

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
 * enough: the place page showed a visit from 10:30 to 18:45 as "All Day" and hid its times (measured).
 */
fun isAllDayVisit(startDate: String?, endDate: String?): Boolean =
    isAllDayBound(startDate) && isAllDayBound(endDate ?: startDate)

/** A visit bound as shown to a person: "2025-07-01 10:30", or the date alone when it has no time. */
fun visitBoundText(isoDateTime: String?): String {
    val date = isoDateTime?.substringBefore('T').orEmpty()
    return timeOfDay(isoDateTime)?.let { "$date $it" } ?: date
}
