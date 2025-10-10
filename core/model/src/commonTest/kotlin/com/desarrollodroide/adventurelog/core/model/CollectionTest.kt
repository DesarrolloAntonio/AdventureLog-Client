package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CollectionTest : BaseModelTest<Collection>() {

    @Test
    fun `should create Collection with all properties`() {
        val location = TestDataFactory.createLocation(
            id = "adv-1",
            name = "Test Adventure",
            description = "Test description",
            rating = 4.0,
            tags = listOf("hiking"),
            isPublic = true
        )

        val collection = TestDataFactory.createCollection(
            id = "col-1",
            description = "Summer vacation collection",
            userId = "user-1",
            name = "Summer 2024",
            isPublic = true,
            locations = listOf(location)
        ).copy(
            startDate = "2024-06-01",
            endDate = "2024-08-31",
            transportations = emptyList(),
            notes = listOf("Remember sunscreen", "Book hotels early"),
            checklists = listOf("Packing list", "Documents"),
            sharedWith = listOf("user-2", "user-3"),
            link = "https://example.com/collection/1",
            lodging = listOf("Hotel California", "Camping Site A")
        )

        assertEquals("col-1", collection.id)
        assertEquals("Summer 2024", collection.name)
        assertEquals("user-1", collection.userId)
        assertTrue(collection.isPublic)
        assertFalse(collection.isArchived)
        assertEquals(1, collection.locations.size)
        assertEquals("Test Adventure", collection.locations[0].name)
        assertEquals(0, collection.transportations.size)
        assertEquals(2, collection.notes.size)
        assertEquals(2, collection.checklists.size)
        assertEquals(2, collection.sharedWith.size)
        assertEquals(2, collection.lodging.size)
    }

    @Test
    fun `should create Collection with minimal properties`() {
        val collection = TestDataFactory.createCollection(
            id = "col-minimal",
            name = "Minimal Collection",
            userId = "user-minimal",
            description = "",
            isPublic = false
        )

        assertEquals("col-minimal", collection.id)
        assertEquals("Minimal Collection", collection.name)
        assertFalse(collection.isPublic)
        assertFalse(collection.isArchived)
        assertTrue(collection.locations.isEmpty())
        assertTrue(collection.transportations.isEmpty())
        assertNull(collection.startDate)
        assertNull(collection.endDate)
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val collection1 = TestDataFactory.createCollection()
        val collection2 = collection1.copy()
        val collection3 = collection1.copy(id = "col-2")
        val collection4 = collection1.copy(name = "Different Name")

        testEquality(
            original = collection1,
            equal = collection2,
            different = listOf(collection3, collection4)
        )
    }

    @Test
    fun `should handle archived collections`() {
        val activeCollection = TestDataFactory.createCollection(isArchived = false)
        val archivedCollection = activeCollection.copy(
            id = "col-archived",
            name = "Archived",
            isArchived = true
        )

        assertFalse(activeCollection.isArchived)
        assertTrue(archivedCollection.isArchived)
    }

    @Test
    fun `should handle collections with multiple locations`() {
        val locations = List(5) { index ->
            TestDataFactory.createLocation(
                id = "loc-$index",
                name = "Location $index",
                rating = 4.0 + (index * 0.1),
                isVisited = index % 2 == 0
            )
        }

        val collection = TestDataFactory.createCollection(
            id = "col-multi",
            name = "Multi Locations",
            locations = locations
        ).copy(
            transportations = emptyList(),
            notes = listOf("Note 1", "Note 2", "Note 3"),
            lodging = listOf("Hotel A", "Hotel B", "Camping")
        )

        assertEquals(5, collection.locations.size)
        assertEquals("Location 0", collection.locations[0].name)
        assertEquals("Location 4", collection.locations[4].name)
        assertEquals(0, collection.transportations.size)
        assertEquals(3, collection.notes.size)
        assertEquals(3, collection.lodging.size)
        assertTrue(collection.locations[0].isVisited)
        assertFalse(collection.locations[1].isVisited)
    }

    @Test
    fun `should handle date ranges correctly`() {
        val collectionWithDates = TestDataFactory.createCollection().copy(
            startDate = "2024-06-01",
            endDate = "2024-06-30"
        )

        val collectionWithoutDates = TestDataFactory.createCollection().copy(
            startDate = null,
            endDate = null
        )

        assertEquals("2024-06-01", collectionWithDates.startDate)
        assertEquals("2024-06-30", collectionWithDates.endDate)
        assertNull(collectionWithoutDates.startDate)
        assertNull(collectionWithoutDates.endDate)
    }
}
