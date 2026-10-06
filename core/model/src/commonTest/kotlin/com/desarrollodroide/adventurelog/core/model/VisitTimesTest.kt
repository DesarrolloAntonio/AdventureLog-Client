package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisitTimesTest {

    @Test
    fun `a visit with times on a single day is not all day`() {
        // The place page showed this one as "All Day 2025-07-01" and hid 10:30 - 18:45.
        assertFalse(isAllDayVisit("2025-07-01T10:30:15Z", "2025-07-01T18:45:30Z"))
        assertFalse(isAllDayVisit("2025-08-02T09:00:00Z", "2025-08-02T23:59:00Z"))
    }

    @Test
    fun `a visit with no time of day is all day`() {
        assertTrue(isAllDayVisit("2025-06-10T00:00:00Z", "2025-06-10T00:00:00Z"))
        assertTrue(isAllDayVisit("2025-05-01T00:00:00Z", "2025-05-03T00:00:00Z"))
        assertTrue(isAllDayVisit("2025-05-01", null))
        // The older way an all-day visit ended.
        assertTrue(isAllDayVisit("2025-05-01T00:00:00Z", "2025-05-01T23:59:00Z"))
    }

    @Test
    fun `a timed visit shows its times`() {
        assertEquals("2025-07-01 10:30", visitBoundText("2025-07-01T10:30:15Z"))
        assertEquals("2025-07-01", visitBoundText("2025-07-01"))
    }
}
