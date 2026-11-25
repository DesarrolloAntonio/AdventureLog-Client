package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.collectionBadge
import com.desarrollodroide.adventurelog.feature.collections.ui.components.ticketDateRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** What a collection's ticket prints: "IN 8 DAYS" and "5–9 SEP". */
class CollectionTicketTextTest {

    private fun trip(status: TripStatus, days: Int? = null, archived: Boolean = false) = UltraSlimCollection(
        id = "1", name = "Cinque Terre", description = "", isPublic = false, isArchived = archived,
        createdAt = "", updatedAt = "", startDate = "2026-09-05", endDate = "2026-09-09",
        adventureCount = 2, featuredImage = null, link = null, status = status, daysUntilStart = days,
    )

    @Test
    fun `the badge says when the trip is`() {
        assertEquals("IN 8 DAYS", collectionBadge(trip(TripStatus.UPCOMING, 8)))
        assertEquals("TOMORROW", collectionBadge(trip(TripStatus.UPCOMING, 1)))
        assertEquals("TODAY", collectionBadge(trip(TripStatus.UPCOMING, 0)))
        assertEquals("PLANNED", collectionBadge(trip(TripStatus.UPCOMING, null)))
        assertEquals("NOW", collectionBadge(trip(TripStatus.IN_PROGRESS, 0)))
    }

    @Test
    fun `a past trip or a folder needs no badge, an archived one says so`() {
        assertNull(collectionBadge(trip(TripStatus.COMPLETED)))
        assertNull(collectionBadge(trip(TripStatus.FOLDER)))
        assertEquals("ARCHIVED", collectionBadge(trip(TripStatus.COMPLETED, archived = true)))
    }

    @Test
    fun `dates read like a ticket`() {
        assertEquals("5–9 SEP", ticketDateRange("2026-09-05", "2026-09-09"))
        assertEquals("28 SEP – 3 OCT", ticketDateRange("2026-09-28", "2026-10-03"))
        assertEquals("5 SEP", ticketDateRange("2026-09-05", "2026-09-05"))
        assertEquals("5 SEP", ticketDateRange("2026-09-05T00:00:00Z", null))
        assertNull(ticketDateRange(null, null))
    }
}
