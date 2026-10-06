package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.testing.testCollection
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.core.testing.testLodging
import com.desarrollodroide.adventurelog.core.testing.testTransportation
import com.desarrollodroide.adventurelog.core.testing.testVisit
import com.desarrollodroide.adventurelog.feature.collections.ui.state.AgendaKind
import com.desarrollodroide.adventurelog.feature.collections.ui.state.agenda
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectionAgendaTest {

    @Test
    fun aCollectionWithNothingDatedHasNoDays() {
        val days = testCollection(locations = listOf(testLocation("Somewhere"))).agenda()
        assertTrue(days.isEmpty())
    }

    @Test
    fun daysComeOutInOrderWhateverOrderTheItemsWereIn() {
        val days = testCollection(
            locations = listOf(
                testLocation("Late", visits = listOf(testVisit("a", "2025-09-14"))),
                testLocation("Early", visits = listOf(testVisit("b", "2025-09-12")))
            )
        ).agenda()
        assertEquals(listOf("2025-09-12", "2025-09-14"), days.map { it.day.toString() })
    }

    @Test
    fun everythingOnOneDayIsOneBlock() {
        val days = testCollection(
            locations = listOf(testLocation("Museum", visits = listOf(testVisit("a", "2025-09-12")))),
            lodging = listOf(testLodging("Hostel", checkIn = "2025-09-12"))
        ).agenda()
        assertEquals(1, days.size)
        assertEquals(2, days.single().entries.size)
    }

    @Test
    fun aStayIsTwoEntriesOnTwoDaysNotOneSpan() {
        val days = testCollection(
            lodging = listOf(testLodging("Hostel", checkIn = "2025-09-12", checkOut = "2025-09-14"))
        ).agenda()
        assertEquals(2, days.size)
        assertEquals(AgendaKind.CHECK_IN, days.first().entries.single().kind)
        assertEquals(AgendaKind.CHECK_OUT, days.last().entries.single().kind)
    }

    @Test
    fun aJourneyWithBothEndsReadsAsARoute() {
        val days = testCollection(
            transportations = listOf(
                testTransportation(
                    "Flight",
                    date = "2025-09-12",
                    fromLocation = "Madrid",
                    toLocation = "Lima"
                )
            )
        ).agenda()
        assertEquals("Madrid → Lima", days.single().entries.single().subtitle)
    }

    @Test
    fun aJourneyWithOnlyOneEndSaysNothingRatherThanHalfARoute() {
        val days = testCollection(
            transportations = listOf(
                testTransportation("Flight", date = "2025-09-12", fromLocation = "Madrid")
            )
        ).agenda()
        assertEquals(null, days.single().entries.single().subtitle)
    }

    @Test
    fun anUndatedVisitIsLeftOutRatherThanFiledUnderToday() {
        val days = testCollection(
            locations = listOf(testLocation("Museum", visits = listOf(testVisit("a", null))))
        ).agenda()
        assertTrue(days.isEmpty())
    }

    @Test
    fun aTimestampIsFiledUnderItsDay() {
        val days = testCollection(
            locations = listOf(
                testLocation("Museum", visits = listOf(testVisit("a", "2025-09-12T22:30:00Z")))
            )
        ).agenda()
        assertEquals("2025-09-12", days.single().day.toString())
    }
}
