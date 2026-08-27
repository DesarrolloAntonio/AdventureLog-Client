package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.network.model.mappers.toUserStats
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/stats/dashboard/`.
 *
 * The server already trims each list to what a dashboard can show - three recent locations,
 * three upcoming trips, ten events - so there is nothing left for the client to slice.
 */
@Serializable
data class DashboardDTO(
    @SerialName("stats")
    val stats: UserStatsDTO? = null,

    @SerialName("recent_locations")
    val recentLocations: List<LocationDTO> = emptyList(),

    @SerialName("upcoming_trips")
    val upcomingTrips: List<UltraSlimCollectionDTO> = emptyList(),

    @SerialName("active_trip")
    val activeTrip: UltraSlimCollectionDTO? = null,

    @SerialName("upcoming_events")
    val upcomingEvents: List<CalendarEventDTO> = emptyList(),

    @SerialName("invite_count")
    val inviteCount: Int = 0
)

@Serializable
data class CalendarEventDTO(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String = "",

    @SerialName("title")
    val title: String = "",

    @SerialName("start")
    val start: String? = null,

    @SerialName("end")
    val end: String? = null,

    @SerialName("all_day")
    val allDay: Boolean = false,

    @SerialName("icon")
    val icon: String = "",

    @SerialName("category")
    val category: String = "",

    @SerialName("location_label")
    val locationLabel: String = "",

    @SerialName("collection_id")
    val collectionId: String? = null,

    @SerialName("collection_name")
    val collectionName: String? = null
)

fun CalendarEventDTO.toDomainModel(): CalendarEvent = CalendarEvent(
    id = id,
    type = type,
    title = title,
    start = start.orEmpty(),
    end = end ?: start.orEmpty(),
    allDay = allDay,
    icon = icon,
    category = category,
    locationLabel = locationLabel,
    collectionId = collectionId,
    collectionName = collectionName
)

fun DashboardDTO.toDomainModel(): Dashboard = Dashboard(
    stats = stats?.toUserStats() ?: UserStats(),
    recentLocations = recentLocations.map { it.toDomainModel() },
    upcomingTrips = upcomingTrips.map { it.toDomainModel() },
    activeTrip = activeTrip?.toDomainModel(),
    upcomingEvents = upcomingEvents.map { it.toDomainModel() },
    inviteCount = inviteCount
)

/**
 * Response of `GET /api/calendar/events/`.
 *
 * The same event shape the dashboard returns - the server builds both from one place - so the
 * calendar screen reads the model that already exists rather than a parallel one.
 */
@Serializable
data class CalendarEventsDTO(
    @SerialName("events")
    val events: List<CalendarEventDTO> = emptyList(),

    @SerialName("count")
    val count: Int = 0
)
