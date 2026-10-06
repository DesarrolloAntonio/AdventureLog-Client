package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import com.desarrollodroide.adventurelog.core.testing.testChecklist
import com.desarrollodroide.adventurelog.core.testing.testCollection
import com.desarrollodroide.adventurelog.core.testing.testItineraryDayNote
import com.desarrollodroide.adventurelog.core.testing.testItineraryEntry
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.core.testing.testLodging
import com.desarrollodroide.adventurelog.core.testing.testTransportation
import com.desarrollodroide.adventurelog.core.testing.testVisit
import com.desarrollodroide.adventurelog.feature.collections.ui.state.itinerary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The Itinerary view's data.
 *
 * An itinerary entry carries no content of its own - only a kind and an id - so almost everything
 * here is about the join back into the collection, which is the part that can silently produce a
 * blank row or the wrong name.
 */
class CollectionItineraryTest {

    @Test
    fun aTripWithNoDatesHasNoDays() {
        val plan = testCollection().itinerary()
        assertTrue(plan.days.isEmpty())
    }

    @Test
    fun everyDayOfTheWindowIsThereIncludingTheEmptyOnes() {
        // The empty Thursday in the middle of a fortnight is the thing you open an itinerary to
        // find, so unlike the Calendar view this does not skip it.
        val plan = testCollection(startDate = "2025-09-12", endDate = "2025-09-16").itinerary()
        assertEquals(5, plan.days.size)
        assertEquals(listOf(1, 2, 3, 4, 5), plan.days.map { it.dayNumber })
        assertTrue(plan.days.all { it.totalDays == 5 })
        assertTrue(plan.days.all { it.items.isEmpty() })
    }

    @Test
    fun anEntryIsResolvedToThePlaceItPointsAt() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-13",
            locations = listOf(testLocation("Seljalandsfoss", id = "loc-1")),
            itinerary = listOf(testItineraryEntry("loc-1", "2025-09-13"))
        ).itinerary()

        val item = plan.days.last().items.single()
        assertEquals("Seljalandsfoss", item.title)
        assertEquals(ItineraryItemKind.LOCATION, item.kind)
    }

    @Test
    fun anEntryPointingAtSomethingNoLongerInTheCollectionIsDroppedNotDrawnBlank() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-12",
            itinerary = listOf(testItineraryEntry("gone", "2025-09-12"))
        ).itinerary()
        assertTrue(plan.days.single().items.isEmpty())
    }

    @Test
    fun aVisitEntryResolvesThroughToItsPlace() {
        // The server can point at a visit rather than at the place. What anyone reading the day
        // wants to see is the place.
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-12",
            locations = listOf(
                testLocation("Reynisfjara", id = "loc-1", visits = listOf(testVisit("v-1", "2025-09-12")))
            ),
            itinerary = listOf(testItineraryEntry("v-1", "2025-09-12", ItineraryItemKind.VISIT))
        ).itinerary()
        assertEquals("Reynisfjara", plan.days.single().items.single().title)
    }

    @Test
    fun theOtherKindsResolveToTheirOwnLists() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-12",
            lodging = listOf(testLodging("Hostel", id = "lo-1")),
            transportations = listOf(
                testTransportation("Flight", id = "tr-1", fromLocation = "Madrid", toLocation = "Lima")
            ),
            checklists = listOf(testChecklist("Packing", id = "ch-1")),
            itinerary = listOf(
                testItineraryEntry("lo-1", "2025-09-12", ItineraryItemKind.LODGING, id = "e1"),
                testItineraryEntry("tr-1", "2025-09-12", ItineraryItemKind.TRANSPORTATION, id = "e2"),
                testItineraryEntry("ch-1", "2025-09-12", ItineraryItemKind.CHECKLIST, id = "e3")
            )
        ).itinerary()

        val titles = plan.days.single().items.map { it.title }
        assertEquals(listOf("Hostel", "Flight", "Packing"), titles)
        assertEquals("Madrid → Lima", plan.days.single().items[1].subtitle)
    }

    @Test
    fun itemsComeOutInTheOrderTheServerKeeps() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-12",
            locations = listOf(
                testLocation("Second", id = "a"),
                testLocation("First", id = "b")
            ),
            itinerary = listOf(
                testItineraryEntry("a", "2025-09-12", id = "e1", order = 5),
                testItineraryEntry("b", "2025-09-12", id = "e2", order = 1)
            )
        ).itinerary()
        assertEquals(listOf("First", "Second"), plan.days.single().items.map { it.title })
    }

    @Test
    fun aGlobalEntryGoesToTripContextAndOntoNoDay() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-13",
            locations = listOf(testLocation("Somewhere", id = "loc-1")),
            itinerary = listOf(testItineraryEntry("loc-1", null))
        ).itinerary()

        assertEquals("Somewhere", plan.tripContext.single().title)
        assertTrue(plan.days.all { it.items.isEmpty() })
    }

    @Test
    fun aDayTakesItsNameAndDescriptionFromItsOwnRecord() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-13",
            itineraryDays = listOf(
                testItineraryDayNote("2025-09-13", name = "Sacred Valley", description = "Slow one")
            )
        ).itinerary()

        assertEquals("", plan.days.first().name)
        assertEquals("Sacred Valley", plan.days.last().name)
        assertEquals("Slow one", plan.days.last().description)
    }

    @Test
    fun theAutoGenerateOfferIsOnlyForAnEmptyItineraryWithSomethingToGenerateFrom() {
        val nothingDated = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-13",
            locations = listOf(testLocation("Somewhere"))
        ).itinerary()
        assertTrue(nothingDated.isEmpty)
        assertFalse(nothingDated.canAutoGenerate)

        val dated = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-13",
            locations = listOf(
                testLocation("Somewhere", id = "l", visits = listOf(testVisit("v", "2025-09-12")))
            )
        ).itinerary()
        assertTrue(dated.canAutoGenerate)

        val alreadyBuilt = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-13",
            locations = listOf(
                testLocation("Somewhere", id = "l", visits = listOf(testVisit("v", "2025-09-12")))
            ),
            itinerary = listOf(testItineraryEntry("l", "2025-09-12"))
        ).itinerary()
        assertFalse(alreadyBuilt.isEmpty)
        assertFalse(alreadyBuilt.canAutoGenerate)
    }

    @Test
    fun aTimestampOnAnEntryStillLandsOnItsDay() {
        val plan = testCollection(
            startDate = "2025-09-12",
            endDate = "2025-09-12",
            locations = listOf(testLocation("Somewhere", id = "l")),
            itinerary = listOf(testItineraryEntry("l", "2025-09-12T18:00:00Z"))
        ).itinerary()
        assertEquals("Somewhere", plan.days.single().items.single().title)
    }
}
