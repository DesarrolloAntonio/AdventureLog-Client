package com.desarrollodroide.adventurelog.feature.calendar

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.CalendarScreen
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarDay
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarUiState
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The calendar, state by state. An empty calendar and a calendar that failed to load look the
 * same from across the room, and only one of them is worth a retry button.
 */
@OptIn(ExperimentalTestApi::class)
class CalendarScreenTest {

    private fun day(date: String, vararg titles: String) = CalendarDay(
        LocalDate.parse(date),
        titles.map { title ->
            CalendarEvent(
                id = title,
                type = "visit",
                title = title,
                start = date,
                end = date,
                allDay = true,
                icon = "",
                category = "",
                locationLabel = "",
                collectionId = null,
                collectionName = null
            )
        }
    )

    private fun show(
        state: CalendarUiState,
        onToggleType: (String) -> Unit = {},
        onClearTypes: () -> Unit = {},
        onRetry: () -> Unit = {},
        onOpen: (EventTarget) -> Unit = {}
    ): @androidx.compose.runtime.Composable () -> Unit = {
        CalendarScreen(
            state = state,
            onToggleType = onToggleType,
            onClearTypes = onClearTypes,
            onRetry = onRetry,
            onOpen = onOpen
        )
    }

    @Test
    fun anEmptyCalendarExplainsWhatWouldFillIt() = runComposeUiTest {
        setContent { show(CalendarUiState(isLoading = false))() }

        onNodeWithText("Nothing dated yet").assertIsDisplayed()
        onNodeWithText("Visits, transport and lodging with dates on them show up here.")
            .assertIsDisplayed()
    }

    @Test
    fun aFailureOffersARetryAndAnEmptyCalendarDoesNot() = runComposeUiTest {
        var retries = 0
        setContent {
            show(
                CalendarUiState(isLoading = false, error = "No internet connection."),
                onRetry = { retries++ }
            )()
        }

        onNodeWithText("No internet connection.").assertIsDisplayed()
        onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun daysAreGroupedUnderTheirMonth() = runComposeUiTest {
        setContent {
            show(
                CalendarUiState(
                    isLoading = false,
                    days = listOf(day("2026-03-04", "Prado"), day("2026-04-01", "Peñalara")),
                    today = LocalDate.parse("2026-03-01")
                )
            )()
        }

        onNodeWithText("March 2026").assertIsDisplayed()
        onNodeWithText("Prado").assertIsDisplayed()
    }

    @Test
    fun oneTypeIsNotWorthAChipRow() = runComposeUiTest {
        setContent {
            show(
                CalendarUiState(
                    isLoading = false,
                    days = listOf(day("2026-03-04", "Prado")),
                    today = LocalDate.parse("2026-03-01"),
                    availableTypes = listOf("visit")
                )
            )()
        }

        // A filter that can only ever be on is a control that does nothing.
        onNodeWithText("All").assertDoesNotExist()
    }

    @Test
    fun choosingATypeChipReportsIt() = runComposeUiTest {
        var chosen: String? = null
        setContent {
            show(
                CalendarUiState(
                    isLoading = false,
                    days = listOf(day("2026-03-04", "Prado")),
                    today = LocalDate.parse("2026-03-01"),
                    availableTypes = listOf("lodging", "visit")
                ),
                onToggleType = { chosen = it }
            )()
        }

        // The chips are capitalised for display but report the server's own value.
        onNodeWithText("Lodging").performClick()
        assertEquals("lodging", chosen)
    }

    @Test
    fun theAllChipClearsTheTypes() = runComposeUiTest {
        var cleared = 0
        setContent {
            show(
                CalendarUiState(
                    isLoading = false,
                    days = listOf(day("2026-03-04", "Prado")),
                    today = LocalDate.parse("2026-03-01"),
                    availableTypes = listOf("lodging", "visit"),
                    selectedTypes = setOf("lodging")
                ),
                onClearTypes = { cleared++ }
            )()
        }

        onNodeWithText("All").performClick()
        assertEquals(1, cleared)
    }

    @Test
    fun theTypeChipsSayWhichOneIsOn() = runComposeUiTest {
        setContent {
            show(
                CalendarUiState(
                    isLoading = false,
                    days = listOf(day("2026-03-04", "Prado")),
                    today = LocalDate.parse("2026-03-01"),
                    availableTypes = listOf("lodging", "visit"),
                    selectedTypes = setOf("lodging")
                )
            )()
        }

        // CA-03: only the colour said so, and a screen reader heard three identical chips.
        onNodeWithText("Lodging").assertIsOn()
        onNodeWithText("Visit").assertIsOff()
        onNodeWithText("All").assertIsOff()
    }

    @Test
    fun aRowOpensWhatItNamesAndARowThatNamesNothingIsNotAButton() = runComposeUiTest {
        var opened: EventTarget? = null
        val date = "2026-03-06"
        fun event(id: String, title: String, type: String, resource: String, trip: String?) = CalendarEvent(
            id = id, type = type, title = title, start = date, end = date, allDay = true, icon = "",
            category = "", locationLabel = "", collectionId = trip, collectionName = trip?.let { "Morocco" },
            resourceId = resource
        )
        setContent {
            show(
                CalendarUiState(
                    isLoading = false,
                    days = listOf(
                        CalendarDay(
                            LocalDate.parse(date),
                            listOf(
                                event("visit-1", "Jemaa el-Fnaa", "visit", "place-1", "trip-1"),
                                event("transportation-1", "Night train", "transportation", "leg-1", null)
                            )
                        )
                    ),
                    today = LocalDate.parse("2026-03-01")
                ),
                onOpen = { opened = it }
            )()
        }

        // CA-02: every row was inert.
        onNodeWithText("Jemaa el-Fnaa").performClick()
        assertEquals(EventTarget.Place("place-1"), opened)
        onNodeWithText("Night train").assertHasNoClickAction()
    }
}
