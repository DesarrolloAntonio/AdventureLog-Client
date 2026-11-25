package com.desarrollodroide.adventurelog.feature.home

import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.home.ui.screen.HomeHeadline
import com.desarrollodroide.adventurelog.feature.home.ui.screen.homeHeadline
import com.desarrollodroide.adventurelog.feature.home.ui.screen.ticketDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The canvas's "Hi John, your next trip starts in 8 days", for every state a trip can be in. */
class HomeHeadlineTest {

    private fun trip(status: TripStatus, days: Int?) = UltraSlimCollection(
        id = "1", name = "Cinque Terre", description = "", isPublic = false, isArchived = false,
        createdAt = "", updatedAt = "", startDate = "2026-09-05", endDate = "2026-09-09",
        adventureCount = 2, featuredImage = null, link = null, status = status, daysUntilStart = days,
    )

    @Test
    fun `counts down to the next trip`() {
        assertEquals(
            HomeHeadline("Hi John, your next trip starts in ", "8 days"),
            homeHeadline("John", trip(TripStatus.UPCOMING, 8), visitedPlaces = 10),
        )
    }

    @Test
    fun `one day is tomorrow, none is today`() {
        assertEquals("tomorrow", homeHeadline("John", trip(TripStatus.UPCOMING, 1), 10).emphasis)
        assertEquals("today", homeHeadline("John", trip(TripStatus.UPCOMING, 0), 10).emphasis)
    }

    @Test
    fun `a trip under way is now`() {
        assertEquals(
            HomeHeadline("Hi John, you are in ", "Cinque Terre"),
            homeHeadline("John", trip(TripStatus.IN_PROGRESS, 0), 10),
        )
    }

    @Test
    fun `an upcoming trip without a count of days still reads`() {
        assertEquals(
            HomeHeadline("Hi John, your next trip is ", "Cinque Terre"),
            homeHeadline("John", trip(TripStatus.UPCOMING, null), 10),
        )
    }

    @Test
    fun `no trip falls back to what has been done`() {
        assertEquals(
            HomeHeadline("Hi John, ", "10 places", " visited so far"),
            homeHeadline("John", null, visitedPlaces = 10),
        )
        assertEquals("1 place", homeHeadline("John", null, visitedPlaces = 1).emphasis)
    }

    @Test
    fun `no first name is still a greeting`() {
        assertEquals("Hi there, your next trip starts in ", homeHeadline("", trip(TripStatus.UPCOMING, 8), 10).lead)
        assertEquals("Hi there, your next trip starts in ", homeHeadline(null, trip(TripStatus.UPCOMING, 8), 10).lead)
    }

    @Test
    fun `ticket dates read like a ticket`() {
        assertEquals("5 SEP", ticketDate("2026-09-05"))
        assertEquals("22 OCT", ticketDate("2026-10-22T00:00:00Z"))
        assertNull(ticketDate(null))
        assertNull(ticketDate(""))
    }
}
