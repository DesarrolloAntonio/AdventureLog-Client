package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.mappers.toVisitRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class VisitRequestMapperTest {

    @Test
    fun `an all day visit is sent as midnight on both bounds`() {
        // The server only collapses an all-day visit to midnight when the end already looks like
        // an all-day bound, and its end-of-day test compares against 23:59:59.999999. Sending
        // 23:59:59 slipped past it and was stored verbatim, so the visit came back with times on
        // it and the All day switch read as off.
        val request = VisitFormData(
            startDate = "2026-08-23",
            endDate = "2026-08-25",
            isAllDay = true
        ).toVisitRequest()

        assertEquals("2026-08-23T00:00:00Z", request.startDate)
        assertEquals("2026-08-25T00:00:00Z", request.endDate)
    }

    @Test
    fun `a single day all day visit ends on the day it starts`() {
        val request = VisitFormData(startDate = "2026-08-23", isAllDay = true).toVisitRequest()

        assertEquals("2026-08-23T00:00:00Z", request.startDate)
        assertEquals("2026-08-23T00:00:00Z", request.endDate)
    }

    @Test
    fun `a timed visit keeps the times it was given`() {
        val request = VisitFormData(
            startDate = "2026-08-23",
            endDate = "2026-08-23",
            startTime = "09:30",
            endTime = "18:45",
            isAllDay = false
        ).toVisitRequest()

        assertEquals("2026-08-23T09:30:00Z", request.startDate)
        assertEquals("2026-08-23T18:45:00Z", request.endDate)
    }

    @Test
    fun `the timezone and notes travel with the visit`() {
        val request = VisitFormData(
            startDate = "2026-08-23",
            timezone = "Europe/Madrid",
            notes = "Con linterna"
        ).toVisitRequest()

        assertEquals("Europe/Madrid", request.timezone)
        assertEquals("Con linterna", request.notes)
    }
}
