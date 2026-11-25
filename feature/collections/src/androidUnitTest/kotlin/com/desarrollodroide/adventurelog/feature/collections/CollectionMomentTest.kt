package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.feature.collections.ui.components.ticketStub
import com.desarrollodroide.adventurelog.feature.collections.ui.components.TicketStub
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionMoment
import com.desarrollodroide.adventurelog.feature.collections.ui.components.arrowDates
import com.desarrollodroide.adventurelog.feature.collections.ui.components.collectionMoment
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The collection header's "PLANNED · 8 DAYS TO GO", worked out on the phone from the dates. */
class CollectionMomentTest {

    private val today = LocalDate(2026, 8, 28)

    @Test
    fun `before the trip it is planned and counts down`() {
        assertEquals(CollectionMoment("PLANNED", 8), collectionMoment("2026-09-05", "2026-09-09", today))
        assertEquals(CollectionMoment("PLANNED", 1), collectionMoment("2026-08-29", null, today))
    }

    @Test
    fun `during the trip it is happening`() {
        assertEquals(CollectionMoment("HAPPENING NOW", null), collectionMoment("2026-08-25", "2026-08-30", today))
        assertEquals(CollectionMoment("HAPPENING NOW", null), collectionMoment("2026-08-28", null, today))
    }

    @Test
    fun `after it, it is done, and without dates it is nothing`() {
        assertEquals(CollectionMoment("COMPLETED", null), collectionMoment("2026-07-03", "2026-07-11", today))
        assertEquals(CollectionMoment(null, null), collectionMoment(null, null, today))
    }

    @Test
    fun `dates read from and to`() {
        assertEquals("5 SEP → 9 SEP", arrowDates("2026-09-05", "2026-09-09"))
        assertEquals("5 SEP", arrowDates("2026-09-05T00:00:00Z", "2026-09-05"))
        assertNull(arrowDates(null, "2026-09-05"))
    }

    private val nov23 = kotlinx.datetime.LocalDate.parse("2026-11-23")

    @Test
    fun theStubCountsDownToATripAhead() {
        assertEquals(TicketStub("8", "DAYS TO GO"), ticketStub("2026-12-01", "2026-12-05", nov23, 2))
        assertEquals(TicketStub("1", "DAY TO GO"), ticketStub("2026-11-24", null, nov23, 2))
    }

    @Test
    fun theStubSaysWhichDayOfATripUnderWay() {
        assertEquals(TicketStub("3", "DAY OF 5"), ticketStub("2026-11-21", "2026-11-25", nov23, 2))
    }

    @Test
    fun theStubSaysHowLongAFinishedTripLasted() {
        assertEquals(TicketStub("13", "DAYS"), ticketStub("2026-10-10", "2026-10-22", nov23, 3))
        assertEquals(TicketStub("1", "DAY"), ticketStub("2026-10-10T00:00:00Z", "2026-10-10", nov23, 3))
    }

    @Test
    fun aFolderStubCountsItsPlaces() {
        assertEquals(TicketStub("3", "PLACES"), ticketStub(null, null, nov23, 3))
        assertEquals(TicketStub("1", "PLACE"), ticketStub("", null, nov23, 1))
    }
}
