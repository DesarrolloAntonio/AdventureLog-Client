package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class UltraSlimCollectionTest : BaseModelTest<UltraSlimCollection>() {

    @Test
    fun `should create UltraSlimCollection with all properties`() {
        val collection = UltraSlimCollection(
            id = "col-1",
            name = "Summer 2024",
            description = "Summer vacation collection",
            isPublic = true,
            isArchived = false,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-15T00:00:00Z",
            startDate = "2024-06-01",
            endDate = "2024-08-31",
            adventureCount = 5,
            featuredImage = "https://example.com/image.jpg",
            link = "https://example.com/collection/1"
        )

        assertEquals("col-1", collection.id)
        assertEquals("Summer 2024", collection.name)
        assertEquals("Summer vacation collection", collection.description)
        assertTrue(collection.isPublic)
        assertFalse(collection.isArchived)
        assertEquals("2024-01-01T00:00:00Z", collection.createdAt)
        assertEquals("2024-01-15T00:00:00Z", collection.updatedAt)
        assertEquals("2024-06-01", collection.startDate)
        assertEquals("2024-08-31", collection.endDate)
        assertEquals(5, collection.adventureCount)
        assertEquals("https://example.com/image.jpg", collection.featuredImage)
        assertEquals("https://example.com/collection/1", collection.link)
    }

    @Test
    fun `should create UltraSlimCollection with minimal properties`() {
        val collection = UltraSlimCollection(
            id = "col-minimal",
            name = "Minimal Collection",
            description = "",
            isPublic = false,
            isArchived = false,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            startDate = null,
            endDate = null,
            adventureCount = 0,
            featuredImage = null,
            link = null
        )

        assertEquals("col-minimal", collection.id)
        assertEquals("Minimal Collection", collection.name)
        assertEquals("", collection.description)
        assertFalse(collection.isPublic)
        assertFalse(collection.isArchived)
        assertEquals(0, collection.adventureCount)
        assertNull(collection.featuredImage)
        assertNull(collection.startDate)
        assertNull(collection.endDate)
        assertNull(collection.link)
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val collection1 = createTestCollection()
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
        val activeCollection = createTestCollection(isArchived = false)
        val archivedCollection = activeCollection.copy(isArchived = true)

        assertFalse(activeCollection.isArchived)
        assertTrue(archivedCollection.isArchived)
    }

    @Test
    fun `should handle collections without dates`() {
        val collectionWithDates = createTestCollection(
            startDate = "2024-06-01",
            endDate = "2024-06-30"
        )

        val collectionWithoutDates = createTestCollection(
            startDate = null,
            endDate = null
        )

        assertEquals("2024-06-01", collectionWithDates.startDate)
        assertEquals("2024-06-30", collectionWithDates.endDate)
        assertNull(collectionWithoutDates.startDate)
        assertNull(collectionWithoutDates.endDate)
    }

    @Test
    fun `should handle collections without featured image`() {
        val withImage = createTestCollection(
            featuredImage = "https://example.com/image.jpg"
        )
        val withoutImage = createTestCollection(
            featuredImage = null
        )

        assertEquals("https://example.com/image.jpg", withImage.featuredImage)
        assertNull(withoutImage.featuredImage)
    }

    @Test
    fun `should handle different adventure counts`() {
        val emptyCollection = createTestCollection(adventureCount = 0)
        val smallCollection = createTestCollection(adventureCount = 3)
        val largeCollection = createTestCollection(adventureCount = 100)

        assertEquals(0, emptyCollection.adventureCount)
        assertEquals(3, smallCollection.adventureCount)
        assertEquals(100, largeCollection.adventureCount)
    }

    @Test
    fun `should convert from Collection to UltraSlimCollection without images`() {
        val user = TestDataFactory.createUserDetails()
        
        val location1 = Location(
            id = "loc-1",
            name = "Location 1",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            images = emptyList(),
            user = user
        )
        val location2 = Location(
            id = "loc-2",
            name = "Location 2",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            images = emptyList(),
            user = user
        )

        val fullCollection = TestDataFactory.createCollection(
            id = "col-1",
            name = "Test Collection",
            description = "Test description",
            userId = "user-1",
            isPublic = true,
            isArchived = false,
            locations = listOf(location1, location2)
        ).copy(
            startDate = "2024-06-01",
            endDate = "2024-08-31",
            link = "https://example.com/collection/1"
        )

        val slimCollection = fullCollection.toUltraSlimCollection()

        assertEquals(fullCollection.id, slimCollection.id)
        assertEquals(fullCollection.name, slimCollection.name)
        assertEquals(fullCollection.description, slimCollection.description)
        assertEquals(fullCollection.isPublic, slimCollection.isPublic)
        assertEquals(fullCollection.isArchived, slimCollection.isArchived)
        assertEquals(fullCollection.createdAt, slimCollection.createdAt)
        assertEquals(fullCollection.updatedAt, slimCollection.updatedAt)
        assertEquals(fullCollection.startDate, slimCollection.startDate)
        assertEquals(fullCollection.endDate, slimCollection.endDate)
        assertEquals(fullCollection.locations.size, slimCollection.adventureCount)
        assertNull(slimCollection.featuredImage)
        assertEquals(fullCollection.link, slimCollection.link)
    }

    @Test
    fun `should convert from Collection to UltraSlimCollection with primary image`() {
        val user = TestDataFactory.createUserDetails()
        
        val primaryImage = ContentImage(
            id = "img-1",
            image = "https://example.com/primary.jpg",
            isPrimary = true,
            user = "user-1"
        )
        val secondaryImage = ContentImage(
            id = "img-2",
            image = "https://example.com/secondary.jpg",
            isPrimary = false,
            user = "user-1"
        )

        val location1 = Location(
            id = "loc-1",
            name = "Location 1",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            images = listOf(secondaryImage, primaryImage),
            user = user
        )
        val location2 = Location(
            id = "loc-2",
            name = "Location 2",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            images = listOf(secondaryImage),
            user = user
        )

        val fullCollection = TestDataFactory.createCollection(
            id = "col-1",
            name = "Test Collection",
            locations = listOf(location1, location2)
        )

        val slimCollection = fullCollection.toUltraSlimCollection()

        assertEquals("https://example.com/primary.jpg", slimCollection.featuredImage)
        assertEquals(2, slimCollection.adventureCount)
    }

    @Test
    fun `should convert from Collection to UltraSlimCollection with first image when no primary`() {
        val user = TestDataFactory.createUserDetails()
        
        val firstImage = ContentImage(
            id = "img-1",
            image = "https://example.com/first.jpg",
            isPrimary = false,
            user = "user-1"
        )
        val secondImage = ContentImage(
            id = "img-2",
            image = "https://example.com/second.jpg",
            isPrimary = false,
            user = "user-1"
        )

        val location = Location(
            id = "loc-1",
            name = "Location 1",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            images = listOf(firstImage, secondImage),
            user = user
        )

        val fullCollection = TestDataFactory.createCollection(
            id = "col-1",
            name = "Test Collection",
            locations = listOf(location)
        )

        val slimCollection = fullCollection.toUltraSlimCollection()

        assertEquals("https://example.com/first.jpg", slimCollection.featuredImage)
    }

    @Test
    fun `should handle private and public collections`() {
        val publicCollection = createTestCollection(isPublic = true)
        val privateCollection = createTestCollection(isPublic = false)

        assertTrue(publicCollection.isPublic)
        assertFalse(privateCollection.isPublic)
    }

    @Test
    fun `should handle collections with links`() {
        val withLink = createTestCollection(link = "https://example.com/collection")
        val withoutLink = createTestCollection(link = null)

        assertEquals("https://example.com/collection", withLink.link)
        assertNull(withoutLink.link)
    }

    private fun createTestCollection(
        id: String = "col-test",
        name: String = "Test Collection",
        description: String = "Test description",
        isPublic: Boolean = true,
        isArchived: Boolean = false,
        createdAt: String = "2024-01-01T00:00:00Z",
        updatedAt: String = "2024-01-01T00:00:00Z",
        startDate: String? = null,
        endDate: String? = null,
        adventureCount: Int = 0,
        featuredImage: String? = null,
        link: String? = null
    ): UltraSlimCollection {
        return UltraSlimCollection(
            id = id,
            name = name,
            description = description,
            isPublic = isPublic,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt,
            startDate = startDate,
            endDate = endDate,
            adventureCount = adventureCount,
            featuredImage = featuredImage,
            link = link
        )
    }
}
