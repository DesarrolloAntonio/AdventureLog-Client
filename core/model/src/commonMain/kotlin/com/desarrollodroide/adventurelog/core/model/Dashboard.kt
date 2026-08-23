package com.desarrollodroide.adventurelog.core.model

/**
 * Everything the home screen needs, in the shape the server's `/api/stats/dashboard/`
 * endpoint returns it. Fetching this as one document replaces the two round trips home
 * used to make (stats plus a page of locations) and adds the trip and event blocks the
 * screen previously had no data for.
 */
data class Dashboard(
    val stats: UserStats = UserStats(),
    val recentLocations: List<Location> = emptyList(),
    val upcomingTrips: List<UltraSlimCollection> = emptyList(),
    val activeTrip: UltraSlimCollection? = null,
    val upcomingEvents: List<CalendarEvent> = emptyList(),
    val inviteCount: Int = 0
)

/**
 * A dated entry from the calendar - a visit, a transport leg, a night's lodging, and so on.
 */
data class CalendarEvent(
    val id: String,
    val type: String,
    val title: String,
    val start: String,
    val end: String,
    val allDay: Boolean,
    val icon: String,
    val category: String,
    val locationLabel: String,
    val collectionId: String?,
    val collectionName: String?
)
