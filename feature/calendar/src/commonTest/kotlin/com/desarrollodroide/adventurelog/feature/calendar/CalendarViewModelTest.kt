package com.desarrollodroide.adventurelog.feature.calendar

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCalendarEventsUseCase
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.testing.CalendarRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testCalendarEvent
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The calendar is a grouping, and the grouping is the whole feature: the server sends a flat
 * list of dated things and everything the screen shows comes out of how they are bucketed.
 */
class CalendarViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun before() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun after() = Dispatchers.resetMain()

    /** Answers with whatever the test hands it, and records the window it was asked for. */
    private class Events(
        private val answer: Either<ApiResponse, List<CalendarEvent>>
    ) : CalendarRepositoryStub() {
        var askedStart: String? = null
        var askedEnd: String? = null

        override suspend fun getEvents(
            start: String?,
            end: String?
        ): Either<ApiResponse, List<CalendarEvent>> {
            askedStart = start
            askedEnd = end
            return answer
        }
    }

    private fun viewModel(events: List<CalendarEvent>) = CalendarViewModel(
        GetCalendarEventsUseCase(Events(Either.Right(events)))
    )

    @Test
    fun eventsAreGroupedByTheDayTheyStart() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(
                testCalendarEvent("a", "2026-03-04T09:00:00Z"),
                testCalendarEvent("b", "2026-03-04T18:00:00Z"),
                testCalendarEvent("c", "2026-03-06T10:00:00Z")
            )
        )
        testScheduler.advanceUntilIdle()

        val days = vm.uiState.value.days
        assertEquals(2, days.size)
        assertEquals(listOf("a", "b"), days[0].events.map { it.id })
        assertEquals(listOf("c"), days[1].events.map { it.id })
    }

    @Test
    fun daysComeOutInDateOrder() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(
                testCalendarEvent("later", "2026-05-01T00:00:00Z"),
                testCalendarEvent("earlier", "2026-01-01T00:00:00Z")
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf("2026-01-01", "2026-05-01"),
            vm.uiState.value.days.map { it.date.toString() }
        )
    }

    @Test
    fun aFortnightLongTripIsOneEntryNotFourteen() = runTest(dispatcher) {
        val trip = testCalendarEvent("trip", "2026-07-01T00:00:00Z").copy(end = "2026-07-14T00:00:00Z")
        val vm = viewModel(listOf(trip))
        testScheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.days.size)
        assertEquals("2026-07-01", vm.uiState.value.days.single().date.toString())
    }

    @Test
    fun anAllDayEventWithNoTimePartStillLands() = runTest(dispatcher) {
        // The server sends bare dates for all-day entries and full timestamps otherwise.
        val vm = viewModel(listOf(testCalendarEvent("allday", "2026-02-02")))
        testScheduler.advanceUntilIdle()

        assertEquals("2026-02-02", vm.uiState.value.days.single().date.toString())
    }

    @Test
    fun somethingUndatedIsDroppedRatherThanCrashingTheScreen() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(testCalendarEvent("good", "2026-02-02"), testCalendarEvent("bad", ""), testCalendarEvent("junk", "not-a-date"))
        )
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("good"), vm.uiState.value.days.flatMap { it.events }.map { it.id })
    }

    @Test
    fun theTypeChipsAreTheTypesActuallyPresentSortedAndDeduplicated() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(
                testCalendarEvent("a", "2026-02-02", type = "visit"),
                testCalendarEvent("b", "2026-02-03", type = "lodging"),
                testCalendarEvent("c", "2026-02-04", type = "visit"),
                testCalendarEvent("d", "2026-02-05", type = "")
            )
        )
        testScheduler.advanceUntilIdle()

        // No blank, no duplicate "visit", and no type the account has nothing of.
        assertEquals(listOf("lodging", "visit"), vm.uiState.value.availableTypes)
    }

    @Test
    fun choosingATypeKeepsOnlyThatType() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(
                testCalendarEvent("stay", "2026-02-02", type = "lodging"),
                testCalendarEvent("seen", "2026-02-03", type = "visit")
            )
        )
        testScheduler.advanceUntilIdle()

        vm.toggleType("lodging")

        assertEquals(listOf("stay"), vm.uiState.value.days.flatMap { it.events }.map { it.id })
        assertEquals(setOf("lodging"), vm.uiState.value.selectedTypes)
    }

    @Test
    fun choosingTheSameTypeAgainTurnsItOff() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(
                testCalendarEvent("stay", "2026-02-02", type = "lodging"),
                testCalendarEvent("seen", "2026-02-03", type = "visit")
            )
        )
        testScheduler.advanceUntilIdle()

        vm.toggleType("lodging")
        vm.toggleType("lodging")

        // Back to everything, not to nothing.
        assertEquals(2, vm.uiState.value.days.size)
        assertTrue(vm.uiState.value.selectedTypes.isEmpty())
    }

    @Test
    fun clearingTheTypesBringsEverythingBack() = runTest(dispatcher) {
        val vm = viewModel(
            listOf(
                testCalendarEvent("stay", "2026-02-02", type = "lodging"),
                testCalendarEvent("seen", "2026-02-03", type = "visit")
            )
        )
        testScheduler.advanceUntilIdle()

        vm.toggleType("lodging")
        vm.clearTypes()

        assertEquals(2, vm.uiState.value.days.size)
    }

    @Test
    fun aWindowIsAskedForRatherThanTheWholeJournal() = runTest(dispatcher) {
        val stub = Events(Either.Right(emptyList()))
        CalendarViewModel(GetCalendarEventsUseCase(stub))
        testScheduler.advanceUntilIdle()

        // Passing neither bound asks the server for everything the account has ever logged.
        assertTrue(stub.askedStart != null && stub.askedEnd != null)
        assertTrue(stub.askedStart!! < stub.askedEnd!!)
    }

    @Test
    fun aRefusalBecomesAMessageAndStopsTheSpinner() = runTest(dispatcher) {
        val vm = CalendarViewModel(
            GetCalendarEventsUseCase(
                Events(Either.Left(ApiResponse.IOException))
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals("No internet connection.", vm.uiState.value.error)
        assertEquals(false, vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.today)
    }

    @Test
    fun anAccountWithNothingDatedReadsAsEmptyNotAsAnError() = runTest(dispatcher) {
        val vm = viewModel(emptyList())
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isEmpty)
        assertNull(vm.uiState.value.error)
    }
}
