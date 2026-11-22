package com.desarrollodroide.adventurelog.feature.detail

import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.core.testing.testVisit
import com.desarrollodroide.adventurelog.feature.detail.ui.components.PlaceFact
import com.desarrollodroide.adventurelog.feature.detail.ui.components.placeFacts
import com.desarrollodroide.adventurelog.feature.detail.ui.components.placeWhere
import com.desarrollodroide.adventurelog.feature.detail.ui.components.visitStamp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The canvas's VISITED / VISITS / TRIP strip under a place's name, and the line above it. */
class PlaceFactsTest {

    private val fjords = UltraSlimCollection(
        id = "c1", name = "Fjords", description = "", isPublic = false, isArchived = false,
        createdAt = "", updatedAt = "", startDate = "2026-07-03", endDate = "2026-07-11",
        adventureCount = 3, featuredImage = null, link = null, status = TripStatus.COMPLETED,
    )

    @Test
    fun `a visit prints like a stamp`() {
        assertEquals("6 JUL 26", visitStamp("2026-07-06"))
        assertEquals("6 JUL 26", visitStamp("2026-07-06T10:00:00Z"))
        assertNull(visitStamp(null))
        assertNull(visitStamp("someday"))
    }

    @Test
    fun `the last visit, how many, and the trip`() {
        val place = testLocation(
            "Trolltunga",
            isVisited = true,
            visits = listOf(testVisit("v1", "2025-06-01"), testVisit("v2", "2026-07-06")),
        )

        assertEquals(
            listOf(
                PlaceFact("VISITED", "6 JUL 26"),
                PlaceFact("VISITS", "2"),
                PlaceFact("TRIP", "Fjords", fjords),
            ),
            placeFacts(place, listOf(fjords)),
        )
    }

    @Test
    fun `a planned place has no visit to stamp`() {
        val place = testLocation("Trolltunga")

        assertEquals(listOf(PlaceFact("TRIP", "Fjords", fjords)), placeFacts(place, listOf(fjords)))
        assertEquals(emptyList(), placeFacts(place, emptyList()))
    }

    @Test
    fun `several trips say how many`() {
        val other = fjords.copy(id = "c2", name = "Norway again")
        val place = testLocation("Trolltunga")

        assertEquals(listOf(PlaceFact("TRIPS", "2")), placeFacts(place, listOf(fjords, other)))
    }

    @Test
    fun `where is region and country, or what the server wrote`() {
        assertEquals(
            "Vestland, Norway",
            placeWhere(testLocation("Trolltunga", region = "Vestland", country = "Norway")),
        )
        assertEquals("Odda, Norway", placeWhere(testLocation("Trolltunga").copy(location = "Odda, Norway")))
        assertNull(placeWhere(testLocation("Trolltunga")))
    }
}
