package com.desarrollodroide.adventurelog.feature.calendar

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCalendarEventsUseCase
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.testing.CalendarRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testCalendarEvent
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarViewModel
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.Earlier
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.target
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

    /** The window's answer, then whatever the request with no lower bound gets. */
    private class Journal(
        private val window: List<CalendarEvent>,
        var before: Either<ApiResponse, List<CalendarEvent>>
    ) : CalendarRepositoryStub() {
        val asked = mutableListOf<Pair<String?, String?>>()

        override suspend fun getEvents(
            start: String?,
            end: String?
        ): Either<ApiResponse, List<CalendarEvent>> {
            asked += start to end
            return if (start == null) before else Either.Right(window)
        }
    }

    @Test
    fun showingEarlierAsksForEverythingBeforeTheWindowAndAddsItOnce() = runTest(dispatcher) {
        val trip = testCalendarEvent("spans", "2025-01-01T00:00:00Z")
        val journal = Journal(
            window = listOf(trip, testCalendarEvent("recent", "2026-03-01")),
            // The server counts anything overlapping the bound, so the trip comes back again.
            before = Either.Right(listOf(testCalendarEvent("petra", "2024-05-18", type = "lodging"), trip))
        )
        val vm = CalendarViewModel(GetCalendarEventsUseCase(journal))
        testScheduler.advanceUntilIdle()
        val windowStart = vm.uiState.value.windowStart.toString()

        vm.loadEarlier()
        testScheduler.advanceUntilIdle()

        assertEquals(null to windowStart, journal.asked.last())
        assertEquals(
            listOf("petra", "spans", "recent"),
            vm.uiState.value.days.flatMap { it.events }.map { it.id }
        )
        assertEquals(Earlier.Loaded(found = 1), vm.uiState.value.earlier)
        // A type only the older events had is offered too.
        assertEquals(listOf("lodging", "visit"), vm.uiState.value.availableTypes)
    }

    @Test
    fun earlierThatFailsSaysSoKeepsTheWindowAndCanBeTriedAgain() = runTest(dispatcher) {
        val journal = Journal(
            window = listOf(testCalendarEvent("recent", "2026-03-01")),
            before = Either.Left(ApiResponse.IOException)
        )
        val vm = CalendarViewModel(GetCalendarEventsUseCase(journal))
        testScheduler.advanceUntilIdle()

        vm.loadEarlier()
        testScheduler.advanceUntilIdle()

        assertEquals(Earlier.Failed("No internet connection."), vm.uiState.value.earlier)
        assertEquals(listOf("recent"), vm.uiState.value.days.flatMap { it.events }.map { it.id })

        journal.before = Either.Right(listOf(testCalendarEvent("petra", "2024-05-18")))
        vm.loadEarlier()
        testScheduler.advanceUntilIdle()

        assertEquals(Earlier.Loaded(found = 1), vm.uiState.value.earlier)
        assertEquals(listOf("petra", "recent"), vm.uiState.value.days.flatMap { it.events }.map { it.id })
    }

    @Test
    fun aVisitOpensItsPlaceAndAnythingInATripOpensTheTrip() {
        val visit = testCalendarEvent("visit-1", "2026-03-06").copy(
            resourceId = "place-1", collectionId = "trip-1", collectionName = "Morocco"
        )
        val trip = testCalendarEvent("collection-1", "2026-03-05", type = "collection").copy(
            resourceId = "trip-1", collectionId = "trip-1", collectionName = "Morocco"
        )
        val stay = testCalendarEvent("lodging-1", "2026-03-06", type = "lodging").copy(
            resourceId = "stay-1", collectionId = "trip-1", collectionName = "Morocco"
        )

        assertEquals(EventTarget.Place("place-1"), visit.target())
        assertEquals(EventTarget.Collection("trip-1", "Morocco"), trip.target())
        assertEquals(EventTarget.Collection("trip-1", "Morocco"), stay.target())
    }

    @Test
    fun somethingThatBelongsToNothingTheAppShowsOpensNothing() {
        val flight = testCalendarEvent("transportation-1", "2026-03-06", type = "transportation")
            .copy(resourceId = "leg-1")

        assertNull(flight.target())
    }

    @Test
    fun anAccountWithNothingDatedReadsAsEmptyNotAsAnError() = runTest(dispatcher) {
        val vm = viewModel(emptyList())
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isEmpty)
        assertNull(vm.uiState.value.error)
    }
}
