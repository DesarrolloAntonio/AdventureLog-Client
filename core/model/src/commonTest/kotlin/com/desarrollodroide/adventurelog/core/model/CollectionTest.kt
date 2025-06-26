package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CollectionTest {
    
    @Test
    fun `should create Collection with all properties`() {
        // Given
        val adventure = Adventure(
            id = "adv-1",
            userId = "user-1",
            name = "Test Adventure",
            description = "Test description",
            rating = 4.0,
            activityTypes = listOf("hiking"),
            location = "Test Location",
            isPublic = true,
            collections = listOf("col-1"),
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02",
            images = emptyList(),
            link = "https://test.com",
            longitude = "0.0",
            latitude = "0.0",
            visits = emptyList(),
            isVisited = false,
            category = null,
            attachments = emptyList()
        )
        
        val collection = Collection(
            id = "col-1",
            description = "Summer vacation collection",
            userId = "user-1",
            name = "Summer 2024",
            isPublic = true,
            adventures = listOf(adventure),
            createdAt = "2024-01-01T10:00:00Z",
            startDate = "2024-06-01",
            endDate = "2024-08-31",
            transportations = listOf("car", "plane"),
            notes = listOf("Remember sunscreen", "Book hotels early"),
            updatedAt = "2024-01-02T15:00:00Z",
            checklists = listOf("Packing list", "Documents"),
            isArchived = false,
            sharedWith = listOf("user-2", "user-3"),
            link = "https://example.com/collection/1",
            lodging = listOf("Hotel California", "Camping Site A")
        )
        
        // Then
        assertEquals("col-1", collection.id)
        assertEquals("Summer 2024", collection.name)
        assertEquals("user-1", collection.userId)
        assertTrue(collection.isPublic)
        assertFalse(collection.isArchived)
        assertEquals(1, collection.adventures.size)
        assertEquals("Test Adventure", collection.adventures[0].name)
        assertEquals(2, collection.transportations.size)
        assertEquals(2, collection.notes.size)
        assertEquals(2, collection.checklists.size)
        assertEquals(2, collection.sharedWith.size)
        assertEquals(2, collection.lodging.size)
        assertEquals("2024-06-01", collection.startDate)
        assertEquals("2024-08-31", collection.endDate)
    }
    
    @Test
    fun `should create Collection with minimal properties`() {
        // Given
        val collection = Collection(
            id = "col-minimal",
            description = "",
            userId = "user-minimal",
            name = "Minimal Collection",
            isPublic = false,
            adventures = emptyList(),
            createdAt = "2024-01-01",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2024-01-01",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        )
        
        // Then
        assertEquals("col-minimal", collection.id)
        assertEquals("Minimal Collection", collection.name)
        assertFalse(collection.isPublic)
        assertFalse(collection.isArchived)
        assertTrue(collection.adventures.isEmpty())
        assertTrue(collection.transportations.isEmpty())
        assertTrue(collection.notes.isEmpty())
        assertTrue(collection.checklists.isEmpty())
        assertTrue(collection.sharedWith.isEmpty())
        assertTrue(collection.lodging.isEmpty())
        assertNull(collection.startDate)
        assertNull(collection.endDate)
    }
    
    @Test
    fun `should correctly compare Collection instances`() {
        // Given
        val collection1 = Collection(
            id = "col-1",
            description = "Description 1",
            userId = "user-1",
            name = "Collection 1",
            isPublic = true,
            adventures = emptyList(),
            createdAt = "2024-01-01",
            startDate = "2024-06-01",
            endDate = "2024-08-31",
            transportations = listOf("car"),
            notes = listOf("Note 1"),
            updatedAt = "2024-01-02",
            checklists = listOf("Checklist 1"),
            isArchived = false,
            sharedWith = listOf("user-2"),
            link = "https://link1.com",
            lodging = listOf("Hotel 1")
        )
        
        val collection2 = collection1.copy()
        val collection3 = collection1.copy(id = "col-2")
        val collection4 = collection1.copy(name = "Different Name")
        
        // Then
        assertEquals(collection1, collection2)
        assertNotEquals(collection1, collection3)
        assertNotEquals(collection1, collection4)
        assertEquals(collection1.hashCode(), collection2.hashCode())
    }
    
    @Test
    fun `should handle archived collections`() {
        // Given
        val activeCollection = Collection(
            id = "col-active",
            description = "Active collection",
            userId = "user-1",
            name = "Active",
            isPublic = true,
            adventures = emptyList(),
            createdAt = "2024-01-01",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2024-01-01",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        )
        
        val archivedCollection = activeCollection.copy(
            id = "col-archived",
            name = "Archived",
            isArchived = true
        )
        
        // Then
        assertFalse(activeCollection.isArchived)
        assertTrue(archivedCollection.isArchived)
        assertNotEquals(activeCollection, archivedCollection)
    }
    
    @Test
    fun `should handle collections with multiple adventures`() {
        // Given
        val adventures = List(5) { index ->
            Adventure(
                id = "adv-$index",
                userId = "user-1",
                name = "Adventure $index",
                description = "Description $index",
                rating = 4.0 + (index * 0.1),
                activityTypes = listOf("type-$index"),
                location = "Location $index",
                isPublic = true,
                collections = listOf("col-multi"),
                createdAt = "2024-01-0${index + 1}",
                updatedAt = "2024-01-0${index + 1}",
                images = emptyList(),
                link = "https://link$index.com",
                longitude = "$index.0",
                latitude = "$index.0",
                visits = emptyList(),
                isVisited = index % 2 == 0,
                category = null,
                attachments = emptyList()
            )
        }
        
        val collection = Collection(
            id = "col-multi",
            description = "Multi-adventure collection",
            userId = "user-1",
            name = "Multi Adventures",
            isPublic = true,
            adventures = adventures,
            createdAt = "2024-01-01",
            startDate = "2024-01-01",
            endDate = "2024-01-05",
            transportations = listOf("car", "bike", "walk"),
            notes = listOf("Note 1", "Note 2", "Note 3"),
            updatedAt = "2024-01-06",
            checklists = listOf("Check 1", "Check 2"),
            isArchived = false,
            sharedWith = listOf("user-2", "user-3", "user-4"),
            link = "https://multi.com",
            lodging = listOf("Hotel A", "Hotel B", "Camping")
        )
        
        // Then
        assertEquals(5, collection.adventures.size)
        assertEquals("Adventure 0", collection.adventures[0].name)
        assertEquals("Adventure 4", collection.adventures[4].name)
        assertEquals(3, collection.transportations.size)
        assertEquals(3, collection.notes.size)
        assertEquals(3, collection.lodging.size)
        assertTrue(collection.adventures[0].isVisited)
        assertFalse(collection.adventures[1].isVisited)
    }
    
    @Test
    fun `should handle date ranges correctly`() {
        // Given
        val collectionWithDates = Collection(
            id = "col-dates",
            description = "Collection with date range",
            userId = "user-1",
            name = "Date Range Collection",
            isPublic = false,
            adventures = emptyList(),
            createdAt = "2024-01-01T00:00:00Z",
            startDate = "2024-06-01",
            endDate = "2024-06-30",
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2024-01-01T12:00:00Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        )
        
        val collectionWithoutDates = collectionWithDates.copy(
            id = "col-no-dates",
            name = "No Dates Collection",
            startDate = null,
            endDate = null
        )
        
        // Then
        assertEquals("2024-06-01", collectionWithDates.startDate)
        assertEquals("2024-06-30", collectionWithDates.endDate)
        assertNull(collectionWithoutDates.startDate)
        assertNull(collectionWithoutDates.endDate)
    }
}
