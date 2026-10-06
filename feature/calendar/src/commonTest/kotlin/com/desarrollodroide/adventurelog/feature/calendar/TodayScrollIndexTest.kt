package com.desarrollodroide.adventurelog.feature.calendar

import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.AgendaItem
import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.agendaItems
import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.todayScrollIndex
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.Earlier
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarDay
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarUiState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Where the calendar lands when it opens.
 *
 * The index has to count the chip row and each month heading exactly as the list builds them,
 * and the two loops are written twice in two places - so they drift, and the screen opens a
 * fortnight off with nothing to indicate it went wrong.
 */
class TodayScrollIndexTest {

    private fun day(date: String) = CalendarDay(LocalDate.parse(date), emptyList())

    private fun state(
        days: List<String>,
        today: String?,
        types: List<String> = emptyList(),
        windowStart: String? = null,
        earlier: Earlier = Earlier.NotAsked
    ) = CalendarUiState(
        days = days.map(::day),
        today = today?.let(LocalDate::parse),
        isLoading = false,
        availableTypes = types,
        windowStart = windowStart?.let(LocalDate::parse),
        earlier = earlier
    )

    @Test
    fun withOneMonthAndNoChipsTodayIsAfterItsMonthHeading() {
        // item 0 is the March heading, 1 is the 1st, 2 is the 4th.
        val s = state(listOf("2026-03-01", "2026-03-04"), today = "2026-03-04")
        assertEquals(2, todayScrollIndex(s))
    }

    @Test
    fun theChipRowPushesEverythingDownByOne() {
        val s = state(
            listOf("2026-03-01", "2026-03-04"),
            today = "2026-03-04",
            types = listOf("lodging", "visit")
        )
        assertEquals(3, todayScrollIndex(s))
    }

    @Test
    fun asingleTypeShowsNoChipRowAtAll() {
        // The screen only draws the chips when there is more than one type to choose between.
        val s = state(listOf("2026-03-01"), today = "2026-03-01", types = listOf("visit"))
        assertEquals(1, todayScrollIndex(s))
    }

    @Test
    fun eachNewMonthCostsOneMoreItem() {
        val s = state(
            listOf("2026-01-05", "2026-02-05", "2026-03-05"),
            today = "2026-03-05"
        )
        // Jan heading, Jan 5, Feb heading, Feb 5, Mar heading, Mar 5.
        assertEquals(5, todayScrollIndex(s))
    }

    @Test
    fun landingOnTheFirstDayAfterTodayWhenTodayItselfHasNothing() {
        val s = state(listOf("2026-03-01", "2026-03-10"), today = "2026-03-04")
        assertEquals(2, todayScrollIndex(s))
    }

    @Test
    fun aJournalEntirelyInThePastOpensOnTodayBelowIt() {
        // Jan heading, Jan 1, Feb heading, Feb 1, then today. It used to open at the top, in 2020.
        val s = state(listOf("2020-01-01", "2020-02-01"), today = "2026-03-04")
        assertEquals(4, todayScrollIndex(s))
    }

    @Test
    fun todayIsMarkedOnADayWithNothingOnIt() {
        // CA-01: today fell between two trips and nothing on the screen said where.
        val items = agendaItems(state(listOf("2026-09-05", "2026-10-10"), today = "2026-09-16"))

        assertEquals(
            listOf("month-2026-9", "day-2026-09-05", "today", "month-2026-10", "day-2026-10-10"),
            items.map { it.key }
        )
    }

    @Test
    fun todayOnADayWithSomethingSitsAboveThatDayUnderItsMonth() {
        val items = agendaItems(state(listOf("2026-03-01", "2026-03-04"), today = "2026-03-04"))

        assertEquals(
            listOf("month-2026-3", "day-2026-03-01", "today", "day-2026-03-04"),
            items.map { it.key }
        )
    }

    @Test
    fun theWayToEarlierEventsSitsAboveTheFirstMonth() {
        // CA-04: before the window there was nothing on screen, not even a way to ask.
        val s = state(listOf("2026-03-01"), today = "2026-03-01", windowStart = "2025-03-01")

        assertEquals(AgendaItem.Earlier, agendaItems(s).first())
        assertEquals(2, todayScrollIndex(s))
    }

    @Test
    fun earlierEventsFoundTakeThePlaceOfTheWayToThem() {
        val s = state(
            listOf("2024-05-18", "2026-03-01"),
            today = "2026-03-01",
            windowStart = "2025-03-01",
            earlier = Earlier.Loaded(found = 1)
        )

        assertTrue(AgendaItem.Earlier !in agendaItems(s))
    }

    @Test
    fun nothingEarlierIsSaidRatherThanLeftBlank() {
        val s = state(
            listOf("2026-03-01"),
            today = "2026-03-01",
            windowStart = "2025-03-01",
            earlier = Earlier.Loaded(found = 0)
        )

        assertEquals(AgendaItem.Earlier, agendaItems(s).first())
    }

    @Test
    fun noTodayMeansTheTop() {
        assertEquals(0, todayScrollIndex(state(listOf("2026-03-01"), today = null)))
    }
}
