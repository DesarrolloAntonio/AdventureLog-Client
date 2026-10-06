package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.testing.testChecklist
import com.desarrollodroide.adventurelog.core.testing.testCollection
import com.desarrollodroide.adventurelog.core.testing.testImage
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.core.testing.testLodging
import com.desarrollodroide.adventurelog.core.testing.testNote
import com.desarrollodroide.adventurelog.core.testing.testTransportation
import com.desarrollodroide.adventurelog.core.testing.testVisit
import com.desarrollodroide.adventurelog.feature.collections.ui.state.stats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The figures on the Statistics view.
 *
 * These are the whole point of that view, and a wrong one is invisible: nothing on the screen
 * contradicts it. The numbers here are the ones the web shows for the same account, so a
 * disagreement between the two clients shows up as a failure rather than as a screenshot.
 */
class CollectionStatsTest {

    @Test
    fun aTripOfTheTwelfthToTheTwentiethIsNineDaysNotEight() {
        val stats = testCollection(startDate = "2025-09-12", endDate = "2025-09-20").stats()
        assertEquals(9, stats.totalDays)
    }

    @Test
    fun aTripWithNoDatesHasNoLength() {
        assertEquals(0, testCollection().stats().totalDays)
    }

    @Test
    fun aOneDayTripIsOneDay() {
        val stats = testCollection(startDate = "2025-09-12", endDate = "2025-09-12").stats()
        assertEquals(1, stats.totalDays)
    }

    @Test
    fun anEndDateTheServerNeverSetLeavesTheTripAsLongAsItsStart() {
        val stats = testCollection(startDate = "2025-09-12").stats()
        assertEquals(1, stats.totalDays)
    }

    @Test
    fun aFullTimestampCountsAsItsDay() {
        val stats = testCollection(
            startDate = "2025-09-12T08:30:00Z",
            endDate = "2025-09-14T22:00:00Z"
        ).stats()
        assertEquals(3, stats.totalDays)
    }

    @Test
    fun twoVisitsOnOneAfternoonAreOneActiveDay() {
        val stats = testCollection(
            locations = listOf(
                testLocation("Museum", visits = listOf(testVisit("a", "2025-09-12"))),
                testLocation("Market", visits = listOf(testVisit("b", "2025-09-12")))
            )
        ).stats()
        assertEquals(1, stats.activeDays)
        assertEquals(2, stats.visits)
    }

    @Test
    fun aStaySpanningThreeDaysMarksAllThreeActive() {
        val stats = testCollection(
            lodging = listOf(testLodging("Hostel", checkIn = "2025-09-12", checkOut = "2025-09-14"))
        ).stats()
        assertEquals(3, stats.activeDays)
    }

    @Test
    fun nightsAreTheGapsBetweenDaysNotTheDaysThemselves() {
        // In on the 12th, out on the 14th: two nights, three days touched.
        val stats = testCollection(
            lodging = listOf(testLodging("Hostel", checkIn = "2025-09-12", checkOut = "2025-09-14"))
        ).stats()
        assertEquals(2, stats.nights)
        assertEquals(1, stats.stays)
    }

    @Test
    fun aStayWithNoCheckOutIsNoNights() {
        val stats = testCollection(
            lodging = listOf(testLodging("Hostel", checkIn = "2025-09-12"))
        ).stats()
        assertEquals(0, stats.nights)
        assertEquals(1, stats.stays)
    }

    @Test
    fun theOwnerIsATravellerToo() {
        assertEquals(1, testCollection().stats().travellers)
        assertEquals(3, testCollection(sharedWith = listOf("a", "b")).stats().travellers)
    }

    @Test
    fun placesAreCountedVisitedAndTotal() {
        val stats = testCollection(
            locations = listOf(
                testLocation("Seen", isVisited = true),
                testLocation("Planned")
            )
        ).stats()
        assertEquals(1, stats.placesVisited)
        assertEquals(2, stats.placesTotal)
        assertEquals(0.5f, stats.visitedFraction)
        assertFalse(stats.isComplete)
    }

    @Test
    fun anEmptyCollectionIsNotComplete() {
        val stats = testCollection().stats()
        assertEquals(0f, stats.visitedFraction)
        assertFalse(stats.isComplete)
    }

    @Test
    fun everyPlaceVisitedIsComplete() {
        val stats = testCollection(
            locations = listOf(testLocation("A", isVisited = true))
        ).stats()
        assertTrue(stats.isComplete)
    }

    @Test
    fun photosAreTheOnesOnPlacesActuallyVisited() {
        val stats = testCollection(
            locations = listOf(
                testLocation("Been", isVisited = true, images = listOf(testImage("1"))),
                testLocation("Planned", images = listOf(testImage("2"), testImage("3")))
            )
        ).stats()
        assertEquals(1, stats.photos)
    }

    @Test
    fun aTripNotTakenYetHasBeenNowhere() {
        // The web reads 0 countries for a planned trip to Peru whose places carry a country and
        // three photographs. These figures are the trip as travelled, not as planned.
        val stats = testCollection(
            locations = listOf(
                testLocation("Machu Picchu", country = "Peru", images = listOf(testImage("1")))
            )
        ).stats()
        assertEquals(0, stats.photos)
        assertTrue(stats.countries.isEmpty())
    }

    @Test
    fun geographyIsDistinctAndSorted() {
        val stats = testCollection(
            locations = listOf(
                testLocation("A", isVisited = true, country = "Iceland", region = "Suðurland", city = "Vík"),
                testLocation("B", isVisited = true, country = "Iceland", region = "Norðurland", city = "Vík"),
                testLocation("C", isVisited = true)
            )
        ).stats()
        assertEquals(listOf("Iceland"), stats.countries)
        assertEquals(listOf("Norðurland", "Suðurland"), stats.regions)
        assertEquals(listOf("Vík"), stats.cities)
    }

    @Test
    fun theContentsAreCountedAsTheyAre() {
        val stats = testCollection(
            notes = listOf(testNote("n")),
            checklists = listOf(testChecklist("c1"), testChecklist("c2")),
            transportations = listOf(testTransportation("flight", date = "2025-09-12"))
        ).stats()
        assertEquals(1, stats.notes)
        assertEquals(2, stats.checklists)
        assertEquals(1, stats.transportations)
    }

    @Test
    fun aDateTheServerCouldNotParseCountsAsNoDateRatherThanCrashing() {
        val stats = testCollection(
            startDate = "not a date",
            locations = listOf(testLocation("A", visits = listOf(testVisit("v", "also not"))))
        ).stats()
        assertEquals(0, stats.totalDays)
        assertEquals(0, stats.activeDays)
        // The visit still happened; only its day is unknown.
        assertEquals(1, stats.visits)
    }
}
