package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AdventureTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // Helper function to create a fake user object
    private fun createFakeUser(id: String = "user-1") = UserDetails(
        uuid = id,
        username = "testuser",
        firstName = "Test",
        lastName = "User",
        profilePic = "",
        publicProfile = true,
        measurementSystem = "metric",
        dateJoined = "2024-01-01T00:00:00Z",
        isStaff = false,
        disablePassword = false,
        hasPassword = true
    )

    @Test
    fun `should use PreviewData adventures correctly`() {
        // Given
        val previewAdventure = PreviewData.locations[0]

        // Then
        assertEquals("1", previewAdventure.id)
        assertEquals("Lake District Mountain Resort (Pending)", previewAdventure.name)
        assertEquals(4.5, previewAdventure.rating)
        assertFalse(previewAdventure.isVisited)
        assertFalse(previewAdventure.isPublic)
        assertEquals(PreviewData.lakeMountainImages, previewAdventure.images)
        assertEquals(PreviewData.categories[0], previewAdventure.category)
        assertEquals(5, previewAdventure.images.size)
        assertEquals(1, previewAdventure.visits.size)
    }

    @Test
    fun `should verify relationships between PreviewData entities`() {
        // Given
        val adventure = PreviewData.locations[0]
        val category = adventure.category
        val images = adventure.images

        // Then
        assertNotNull(category)
        assertEquals("hotel", category?.name)
        assertEquals("🏨", category?.icon)

        // Verify primary image
        val primaryImage = images.find { it.isPrimary }
        assertNotNull(primaryImage)
        assertEquals("1-1", primaryImage?.id)
    }

    @Test
    fun `should create Adventure with all properties`() {
        // Given
        val user = createFakeUser()
        val category = Category(
            id = "cat-1",
            name = "Hiking",
            displayName = "Hiking",
            icon = "🏞️",
            numAdventures = "10"
        )

        val image = ContentImage(
            id = "img-1",
            image = "https://example.com/image1.jpg",
            isPrimary = true,
            user = user.uuid
        )

        val visit = Visit(
            id = "visit-1",
            location = "adv-1",
            startDate = "2024-01-15",
            endDate = "2024-01-20",
            notes = "Great experience",
            timezone = "UTC",
            createdAt = "2024-01-15T00:00:00Z",
            updatedAt = "2024-01-15T00:00:00Z"
        )

        val attachment = Attachment(
            id = "att-1",
            file = "https://example.com/file.pdf",
            extension = "pdf",
            name = "guide",
            user = user.uuid
        )

        val adventure = Location(
            id = "adv-1",
            user = user,
            name = "Mountain Trail Adventure",
            description = "A beautiful mountain trail with stunning views",
            rating = 4.5,
            tags = listOf("hiking", "nature"),
            location = "Rocky Mountains",
            isPublic = true,
            collections = listOf("Summer Locations"),
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-02T15:30:00Z",
            images = listOf(image),
            link = "https://example.com/adventure/1",
            longitude = "-105.2705",
            latitude = "40.0150",
            visits = listOf(visit),
            isVisited = true,
            category = category,
            attachments = listOf(attachment),
            trails = emptyList()
        )

        // Then
        assertEquals("adv-1", adventure.id)
        assertEquals("user-1", adventure.user.uuid)
        assertEquals("Mountain Trail Adventure", adventure.name)
        assertEquals(4.5, adventure.rating)
        assertEquals(2, adventure.tags.size)
        assertTrue(adventure.isPublic)
        assertTrue(adventure.isVisited)
        assertNotNull(adventure.category)
        assertEquals("Hiking", adventure.category?.name)
        assertEquals(1, adventure.images.size)
        assertEquals(1, adventure.visits.size)
        assertEquals(1, adventure.attachments.size)
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val location = Location(
            id = "123",
            user = createFakeUser("user-456"),
            name = "Test Adventure",
            description = "Test description",
            rating = 3.5,
            tags = listOf("hiking"),
            location = "Test Location",
            isPublic = false,
            collections = listOf("Test Collection"),
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02",
            images = emptyList(),
            link = "https://test.com",
            longitude = "0.0",
            latitude = "0.0",
            visits = emptyList(),
            isVisited = false,
            category = null,
            attachments = emptyList(),
            trails = emptyList()
        )

        // When
        val jsonString = json.encodeToString(Location.serializer(), location)

        // Then
        assertTrue(jsonString.contains("\"id\":\"123\""))
        assertTrue(jsonString.contains("\"name\":\"Test Adventure\""))
        assertTrue(jsonString.contains("\"rating\":3.5"))
        assertTrue(jsonString.contains("\"isPublic\":false"))
        assertTrue(jsonString.contains("\"tags\":[\"hiking\"]"))
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": "789",
                "user": { "uuid": "user-999", "username": "desert-user", "dateJoined": "2024-01-01" },
                "name": "Desert Trek",
                "description": "An amazing desert adventure",
                "rating": 5.0,
                "tags": ["trekking", "camping"],
                "location": "Sahara Desert",
                "isPublic": true,
                "collections": ["Desert Locations"],
                "createdAt": "2024-03-01T08:00:00Z",
                "updatedAt": "2024-03-02T10:00:00Z",
                "images": [],
                "link": "https://adventures.com/789",
                "longitude": "10.5",
                "latitude": "25.0",
                "visits": [],
                "isVisited": false,
                "category": null,
                "attachments": [],
                "trails": []
            }
        """.trimIndent()

        // When
        val location = json.decodeFromString<Location>(jsonString)

        // Then
        assertEquals("789", location.id)
        assertEquals("Desert Trek", location.name)
        assertEquals(5.0, location.rating)
        assertEquals(2, location.tags.size)
        assertTrue(location.tags.contains("trekking"))
        assertTrue(location.tags.contains("camping"))
        assertTrue(location.isPublic)
        assertFalse(location.isVisited)
        assertNull(location.category)
    }

    @Test
    fun `should deserialize with nested objects correctly`() {
        // Given
        val jsonString = """
            {
                "id": "adv-complex",
                "user": { "uuid": "user-complex", "username": "complex-user", "dateJoined": "2024-01-01" },
                "name": "Complex Adventure",
                "description": "Adventure with all nested objects",
                "rating": 4.8,
                "tags": ["hiking"],
                "location": "Complex Location",
                "isPublic": true,
                "collections": ["Complex Collection"],
                "createdAt": "2024-01-01",
                "updatedAt": "2024-01-02",
                "images": [
                    { "id": "img-1", "image": "https://example.com/img.jpg", "isPrimary": true, "user": "user-complex" }
                ],
                "link": "https://example.com",
                "longitude": "1.0",
                "latitude": "2.0",
                "visits": [
                    { 
                        "id": "visit-1",
                        "location": "adv-complex",
                        "startDate": "2024-01-15", 
                        "endDate": "2024-01-20", 
                        "timezone": "UTC",
                        "createdAt": "2024-01-15T00:00:00Z",
                        "updatedAt": "2024-01-15T00:00:00Z"
                    }
                ],
                "isVisited": true,
                "category": { "id": "cat-1", "name": "Hiking", "displayName": "Hiking", "icon": "🏞️", "numAdventures": "5" },
                "attachments": [
                    { "id": "att-1", "file": "https://example.com/file.pdf", "extension": "pdf", "name": "guide", "user": "user-complex" }
                ],
                "trails": []
            }
        """.trimIndent()

        // When
        val location = json.decodeFromString<Location>(jsonString)

        // Then
        assertEquals("adv-complex", location.id)
        assertEquals(1, location.images.size)
        assertEquals("img-1", location.images[0].id)
        assertEquals(1, location.visits.size)
        assertEquals("visit-1", location.visits[0].id)
        assertNotNull(location.category)
        assertEquals("Hiking", location.category?.name)
        assertEquals("🏞️", location.category?.icon)
        assertEquals(1, location.attachments.size)
        assertEquals("guide", location.attachments[0].name)
        assertEquals("pdf", location.attachments[0].extension)
    }

    @Test
    fun `should correctly compare Adventure instances`() {
        // Given
        val location1 = Location(
            id = "adv-1",
            user = createFakeUser(),
            name = "Adventure 1",
            description = "Description 1",
            rating = 4.0,
            tags = listOf("hiking"),
            location = "Location 1",
            isPublic = true,
            collections = listOf("Collection 1"),
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02",
            images = emptyList(),
            link = "https://link1.com",
            longitude = "1.0",
            latitude = "2.0",
            visits = emptyList(),
            isVisited = false,
            category = null,
            attachments = emptyList(),
            trails = emptyList()
        )

        val adventure2 = location1.copy()
        val adventure3 = location1.copy(id = "adv-2")
        val adventure4 = location1.copy(rating = 5.0)

        // Then
        assertEquals(location1, adventure2)
        assertNotEquals(location1, adventure3)
        assertNotEquals(location1, adventure4)
        assertEquals(location1.hashCode(), adventure2.hashCode())
    }

    @Test
    fun `should handle empty collections correctly`() {
        // Given
        val adventure = Location(
            id = "empty-collections",
            user = createFakeUser("user-empty"),
            name = "Empty Collections Adventure",
            description = "Adventure with empty collections",
            rating = 3.0,
            tags = emptyList(),
            location = "Empty Location",
            isPublic = false,
            collections = listOf("Empty Collection"),
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            images = emptyList(),
            link = "",
            longitude = "0.0",
            latitude = "0.0",
            visits = emptyList(),
            isVisited = false,
            category = null,
            attachments = emptyList(),
            trails = emptyList()
        )

        // Then
        assertTrue(adventure.tags.isEmpty())
        assertTrue(adventure.images.isEmpty())
        assertTrue(adventure.visits.isEmpty())
        assertTrue(adventure.attachments.isEmpty())
        assertNull(adventure.category)
        assertFalse(adventure.isVisited)
    }
}