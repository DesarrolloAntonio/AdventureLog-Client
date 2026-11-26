package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A timed visit is stored as the web stores it (REF `lib/dateUtils.ts`, `toUTCDatetime`): the
 * local time chosen in the visit's zone, converted to UTC. The apps used to send the wall-clock
 * time with a "Z" on it, so the web showed a Madrid visit an hour or two late.
 */
class VisitTimesTest {

    @Test
    fun aTimedVisitInMadridIsStoredInUtc() {
        assertEquals("2026-11-26T09:00:00Z", visitStored("2026-11-26", "10:00", "Europe/Madrid"))
        // Summer time: two hours ahead.
        assertEquals("2026-07-01T08:30:00Z", visitStored("2026-07-01", "10:30", "Europe/Madrid"))
    }

    @Test
    fun aStoredTimeReadsBackInItsZone() {
        assertEquals("2026-11-26T10:00", visitLocalDateTime("2026-11-26T09:00:00Z", "Europe/Madrid"))
    }

    @Test
    fun aLateEveningInTokyoIsTheNextDayThere() {
        assertEquals("2026-11-27T01:00", visitLocalDateTime("2026-11-26T16:00:00Z", "Asia/Tokyo"))
    }

    @Test
    fun withoutAZoneOrATimeTheValueIsLeftAsItIs() {
        assertEquals("2026-11-26T09:00:00Z", visitLocalDateTime("2026-11-26T09:00:00Z", null))
        assertEquals("2026-11-26", visitLocalDateTime("2026-11-26", "Europe/Madrid"))
        assertEquals("nonsense", visitLocalDateTime("nonsense", "Europe/Madrid"))
    }

    @Test
    fun anUnknownZoneIsTreatedAsUtc() {
        assertEquals("2026-11-26T10:00:00Z", visitStored("2026-11-26", "10:00", "Mars/Olympus"))
    }
}
