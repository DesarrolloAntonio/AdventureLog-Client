package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.visitFormOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** A stored visit opens in the form at the times it was chosen at, in its own zone (as the web shows it). */
class VisitFormTimesTest {

    private fun visit(start: String, end: String, zone: String) =
        Visit(id = "v", location = "p", startDate = start, endDate = end, timezone = zone, createdAt = "", updatedAt = "")

    @Test
    fun aTimedVisitOpensAtItsLocalHours() {
        val form = visitFormOf(visit("2026-11-26T09:00:00Z", "2026-11-26T11:00:00Z", "Europe/Madrid"))

        assertEquals("2026-11-26", form.startDate)
        assertEquals("10:00", form.startTime)
        assertEquals("12:00", form.endTime)
    }

    @Test
    fun aTimedVisitCanFallOnTheNextDayLocally() {
        val form = visitFormOf(visit("2026-11-26T16:00:00Z", "2026-11-26T18:00:00Z", "Asia/Tokyo"))

        assertEquals("2026-11-27", form.startDate)
        assertEquals("01:00", form.startTime)
    }

    @Test
    fun anAllDayVisitKeepsItsDayAndHasNoHours() {
        val form = visitFormOf(visit("2026-11-26T00:00:00Z", "2026-11-28T00:00:00Z", "Asia/Tokyo"))

        assertEquals("2026-11-26", form.startDate)
        assertEquals("2026-11-28", form.endDate)
        assertNull(form.startTime)
    }
}
