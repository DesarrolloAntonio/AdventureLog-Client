package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportDates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A transport's dates as the web stores them (QA 04, CO-06: the app could not set them at all).
 * Checked against the server: whatever wall time arrives is kept as UTC, so the conversion is the
 * client's job.
 */
class TransportDatesTest {

    @Test
    fun aTimeChosenInMadridIsStoredAsTheSameMomentInUtc() {
        assertEquals("2026-10-05T12:30:00Z", TransportDates.toStored("2026-10-05", "14:30", "Europe/Madrid", allDay = false))
    }

    @Test
    fun whatTheServerStoresIsShownInTheTransportsOwnZone() {
        assertEquals(
            TransportDates.Local("2026-10-06", "01:30"),
            TransportDates.toLocal("2026-10-05T16:30:00Z", "Asia/Tokyo", allDay = false)
        )
    }

    @Test
    fun anAllDayDateIsThatDayAtUtcMidnightWhateverTheZone() {
        assertEquals("2026-10-05T00:00:00Z", TransportDates.toStored("2026-10-05", null, "Asia/Tokyo", allDay = true))
        assertEquals(TransportDates.Local("2026-10-05", null), TransportDates.toLocal("2026-10-05T00:00:00Z", "Asia/Tokyo", allDay = true))
    }

    @Test
    fun midnightUtcReadsAsAllDayAndAnyOtherTimeDoesNot() {
        assertTrue(TransportDates.isAllDay("2026-10-05T00:00:00Z"))
        assertFalse(TransportDates.isAllDay("2026-10-05T12:30:00Z"))
    }

    @Test
    fun anArrivalBeforeTheDepartureIsCaughtAndTheSameMomentIsNot() {
        // QA 04: the form saved a departure on the 12th with its arrival on the 5th.
        assertTrue(TransportDates.arrivesBeforeDeparting("2026-10-05T08:00:00Z", "2026-10-05T07:59:00Z"))
        assertFalse(TransportDates.arrivesBeforeDeparting("2026-10-05T08:00:00Z", "2026-10-05T08:00:00Z"))
        assertFalse(TransportDates.arrivesBeforeDeparting("", "2026-10-05T08:00:00Z"))
    }

}
