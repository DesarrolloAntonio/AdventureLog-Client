package com.desarrollodroide.adventurelog.feature.home

import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen
import com.desarrollodroide.adventurelog.feature.home.ui.screen.comingUpEntries
import com.desarrollodroide.adventurelog.feature.home.ui.screen.dashboardSubtitle
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertEquals

/** What Home says is ahead: each trip once, and only trips that have not started. */
class HomeComingUpTest {

    private fun trip(id: String, start: String, status: TripStatus = TripStatus.UPCOMING) = UltraSlimCollection(
        id = id, name = id, description = "", isPublic = false, isArchived = false,
        createdAt = "", updatedAt = "", startDate = start, endDate = start, adventureCount = 0,
        featuredImage = null, link = null, status = status
    )

    // The calendar's own event for a dated collection, as the server sends it.
    private fun tripEvent(trip: UltraSlimCollection) = CalendarEvent(
        id = "collection-${trip.id}", type = "collection", title = trip.name, start = trip.startDate!!,
        end = trip.endDate!!, allDay = true, icon = "", category = "", locationLabel = "",
        collectionId = trip.id, collectionName = trip.name
    )

    private fun placeEvent(id: String, start: String, collectionId: String?) = CalendarEvent(
        id = id, type = "location", title = id, start = start, end = start, allDay = true, icon = "",
        category = "", locationLabel = "Lima", collectionId = collectionId, collectionName = collectionId
    )

    private fun keys(trips: List<UltraSlimCollection>, events: List<CalendarEvent>, featured: String? = null) =
        comingUpEntries(trips, events, today = null, onTripClick = {}, featuredTripId = featured).map { it.key }

    @Test
    fun `a trip listed as a row does not come back as its calendar event`() {
        val tomorrow = trip("QA_Trip_Tomorrow", "2026-09-16")

        assertEquals(listOf("trip-QA_Trip_Tomorrow"), keys(listOf(tomorrow), listOf(tripEvent(tomorrow))))
    }

    @Test
    fun `the trip on the card above does not come back as its calendar event`() {
        val now = trip("QA_Trip_Now", "2026-09-14", TripStatus.IN_PROGRESS)

        assertEquals(emptyList(), keys(emptyList(), listOf(tripEvent(now)), featured = now.id))
    }

    @Test
    fun `a trip Home does not show still comes up as its calendar event`() {
        val shown = trip("Peru", "2026-10-01")
        val notShown = trip("Japan", "2026-11-01")

        assertEquals(
            listOf("trip-Peru", "event-collection-Japan"),
            keys(listOf(shown), listOf(tripEvent(shown), tripEvent(notShown)))
        )
    }

    @Test
    fun `a place inside a trip on Home still comes up`() {
        val peru = trip("Peru", "2026-10-01")

        assertEquals(
            listOf("trip-Peru", "event-Machu Picchu"),
            keys(listOf(peru), listOf(placeEvent("Machu Picchu", "2026-10-03", collectionId = peru.id)), featured = peru.id)
        )
    }

    private fun subtitle(active: UltraSlimCollection?, upcoming: List<UltraSlimCollection>) = dashboardSubtitle(
        CurrentScreen.HOME,
        HomeUiState.Success(
            dashboard = Dashboard(stats = UserStats(visitedLocationCount = 5), activeTrip = active, upcomingTrips = upcoming)
        )
    )

    @Test
    fun `the trip under way is not counted as a trip ahead`() {
        val now = trip("QA_Trip_Now", "2026-09-14", TripStatus.IN_PROGRESS)

        assertEquals("5 places visited · 2 trips ahead", subtitle(now, listOf(trip("a", "2026-09-16"), trip("b", "2026-10-01"))))
        assertEquals("5 places visited", subtitle(now, emptyList()))
    }

    @Test
    fun `trips that have not started are counted`() {
        assertEquals("5 places visited · 1 trip ahead", subtitle(null, listOf(trip("a", "2026-09-16"))))
    }

    // QA HM-09: event rows in Coming up opened nothing while the trip rows beside them did.

    private fun opened(event: CalendarEvent): EventTarget? {
        var target: EventTarget? = null
        val entry = comingUpEntries(emptyList(), listOf(event), today = null, onTripClick = {}, onOpenEvent = { target = it }).single()
        entry.onClick?.invoke()
        return target
    }

    private fun event(type: String, collectionId: String? = null, resourceId: String = "") = CalendarEvent(
        id = "$type-1", type = type, title = "QA_$type", start = "2026-10-12", end = "2026-10-12", allDay = true,
        icon = "", category = "", locationLabel = "", collectionId = collectionId, collectionName = collectionId,
        resourceId = resourceId
    )

    @Test
    fun `a visit in Coming up opens its place`() {
        assertEquals(EventTarget.Place("p9"), opened(event("visit", collectionId = "Peru", resourceId = "p9")))
    }

    @Test
    fun `a stay inside a trip opens the trip`() {
        assertEquals(EventTarget.Collection("Peru", "Peru"), opened(event("lodging", collectionId = "Peru")))
    }

    @Test
    fun `an event that belongs to nothing opens nothing`() {
        val entry = comingUpEntries(emptyList(), listOf(event("note")), today = null, onTripClick = {}).single()
        assertNull(entry.onClick)
    }

}
