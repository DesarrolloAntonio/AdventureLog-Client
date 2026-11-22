package com.desarrollodroide.adventurelog.feature.calendar

import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.monthCells
import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.spannedDays
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarDay
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The month view's grid: weeks starting on Monday, and the days a multi-day trip covers. */
class MonthGridTest {

    @Test
    fun `a month starts in its weekday's column`() {
        val september = monthCells(2026, 9)
        // 1 September 2026 is a Tuesday: one empty cell for Monday first.
        assertNull(september[0])
        assertEquals(LocalDate(2026, 9, 1), september[1])
        assertEquals(LocalDate(2026, 9, 30), september.filterNotNull().last())
        assertEquals(0, september.size % 7)
    }

    @Test
    fun `a trip covers every day from its start to its end`() {
        fun event(start: String, end: String) = CalendarEvent(
            id = start, type = "collection", title = "Trip", start = start, end = end, allDay = true,
            icon = "", category = "", locationLabel = "", collectionId = "c", collectionName = "Trip",
        )
        val days = listOf(
            CalendarDay(LocalDate(2026, 9, 5), listOf(event("2026-09-05", "2026-09-09"))),
            CalendarDay(LocalDate(2026, 9, 20), listOf(event("2026-09-20T10:00:00Z", "2026-09-20T12:00:00Z"))),
        )

        assertEquals(
            (5..9).map { LocalDate(2026, 9, it) }.toSet(),
            spannedDays(days),
        )
    }
}
