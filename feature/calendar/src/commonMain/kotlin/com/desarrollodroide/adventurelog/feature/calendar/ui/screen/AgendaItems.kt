package com.desarrollodroide.adventurelog.feature.calendar.ui.screen

import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarDay
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarUiState
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.Earlier
import kotlinx.datetime.LocalDate

// Split out of CalendarScreen.kt (Compose, androidMain): plain logic, tested on every platform.

/**
 * One row of the agenda. The list and the index it scrolls to are both read from [agendaItems], so
 * the two cannot disagree about where today is.
 */
internal sealed interface AgendaItem {
    val key: String

    data object Chips : AgendaItem {
        override val key = "chips"
    }

    data object Earlier : AgendaItem {
        override val key = "earlier"
    }

    data class Month(val date: LocalDate) : AgendaItem {
        override val key get() = "month-${monthKey(date)}"
    }

    data class Today(val date: LocalDate) : AgendaItem {
        override val key get() = "today"
    }

    data class Day(val day: CalendarDay) : AgendaItem {
        override val key get() = "day-${day.date}"
    }
}

/**
 * The agenda, top to bottom: the type chips when there is a choice, what is known before the
 * window, then each month's heading and days - with today marked where it falls, even on a date
 * with nothing on it. Nothing marked it (QA 07, CA-01): today was only used to pick where to
 * scroll, so on opening you could not tell what had happened from what was coming.
 */
internal fun agendaItems(state: CalendarUiState): List<AgendaItem> = buildList {
    if (state.availableTypes.size > 1) add(AgendaItem.Chips)
    val earlier = state.earlier
    if (state.windowStart != null && !(earlier is Earlier.Loaded && earlier.found > 0)) {
        add(AgendaItem.Earlier)
    }

    val today = state.today
    var todayPlaced = today == null
    var lastMonth: String? = null
    state.days.forEach { day ->
        val month = monthKey(day.date)
        val todayGoesHere = !todayPlaced && today != null && day.date >= today
        // Today's month has nothing in it: the marker goes between the months, dated.
        if (todayGoesHere && monthKey(today!!) != month) {
            add(AgendaItem.Today(today))
            todayPlaced = true
        }
        if (month != lastMonth) {
            lastMonth = month
            add(AgendaItem.Month(day.date))
        }
        // Today's month has something: the marker goes under its heading.
        if (todayGoesHere && !todayPlaced) {
            add(AgendaItem.Today(today!!))
            todayPlaced = true
        }
        add(AgendaItem.Day(day))
    }
    // Everything is in the past: today goes after it.
    if (!todayPlaced && today != null) add(AgendaItem.Today(today))
}

private fun monthKey(date: LocalDate) = "${date.year}-${date.monthNumber}"

/** Where the list opens: today's marker. 0 (the top) when there is no today to mark. */
internal fun todayScrollIndex(state: CalendarUiState): Int =
    agendaItems(state).indexOfFirst { it is AgendaItem.Today }.coerceAtLeast(0)
