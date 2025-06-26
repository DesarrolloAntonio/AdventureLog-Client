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
    
    @Test
    fun `should use PreviewData adventures correctly`() {
        // Given
        val previewAdventure = PreviewData.adventures[0]
        
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
        val adventure = PreviewData.adventures[0]
        val category = adventure.category
        val images = adventure.images
        
        // Then
        assertNotNull(category)
        assertEquals("hotel", category?.name)
        assertEquals("🏨", category?.icon)
        
        // Verify all images belong to this adventure
        images.forEach { image ->
            assertEquals(adventure.id, image.adventure)
        }
        
        // Verify primary image
        val primaryImage = images.find { it.isPrimary }
        assertNotNull(primaryImage)
        assertEquals("1-1", primaryImage?.id)
    }
    
    @Test
    fun `should create Adventure with all properties`() {
        // Given
        val category = Category(
            id = "cat-1",
            name = "Hiking",
            displayName = "Hiking",
            icon = "🥾",
            numAdventures = "10"
        )
        
        val image = AdventureImage(
            id = "img-1",
            image = "https://example.com/image1.jpg",
            adventure = "adv-1",
            isPrimary = true,
            userId = "user-1"
        )
        
        val visit = Visit(
            id = "visit-1",
            startDate = "2024-01-15",
            endDate = "2024-01-20",
            notes = "Great experience",
            timezone = "UTC"
        )
        
        val attachment = Attachment(
            id = "att-1",
            file = "https://example.com/file.pdf",
            adventure = "adv-1",
            extension = "pdf",
            name = "guide",
            userId = 1
        )
        
        val adventure = Adventure(
            id = "adv-1",
            userId = "user-1",
            name = "Mountain Trail Adventure",
            description = "A beautiful mountain trail with stunning views",
            rating = 4.5,
            activityTypes = listOf("hiking", "nature"),
            location = "Rocky Mountains",
            isPublic = true,
            collections = listOf("Summer Adventures"),
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-02T15:30:00Z",
            images = listOf(image),
            link = "https://example.com/adventure/1",
            longitude = "-105.2705",
            latitude = "40.0150",
            visits = listOf(visit),
            isVisited = true,
            category = category,
            attachments = listOf(attachment)
        )
        
        // Then
        assertEquals("adv-1", adventure.id)
        assertEquals("user-1", adventure.userId)
        assertEquals("Mountain Trail Adventure", adventure.name)
        assertEquals(4.5, adventure.rating)
        assertEquals(2, adventure.activityTypes.size)
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
        val adventure = Adventure(
            id = "123",
            userId = "user-456",
            name = "Test Adventure",
            description = "Test description",
            rating = 3.5,
            activityTypes = listOf("hiking"),
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
            attachments = emptyList()
        )
        
        // When
        val jsonString = json.encodeToString(Adventure.serializer(), adventure)
        
        // Then
        assertTrue(jsonString.contains("\"id\":\"123\""))
        assertTrue(jsonString.contains("\"name\":\"Test Adventure\""))
        assertTrue(jsonString.contains("\"rating\":3.5"))
        assertTrue(jsonString.contains("\"isPublic\":false"))
        assertTrue(jsonString.contains("\"activityTypes\":[\"hiking\"]"))
    }
    
    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": "789",
                "userId": "user-999",
                "name": "Desert Trek",
                "description": "An amazing desert adventure",
                "rating": 5.0,
                "activityTypes": ["trekking", "camping"],
                "location": "Sahara Desert",
                "isPublic": true,
                "collections": ["Desert Adventures"],
                "createdAt": "2024-03-01T08:00:00Z",
                "updatedAt": "2024-03-02T10:00:00Z",
                "images": [],
                "link": "https://adventures.com/789",
                "longitude": "10.5",
                "latitude": "25.0",
                "visits": [],
                "isVisited": false,
                "category": null,
                "attachments": []
            }
        """.trimIndent()
        
        // When
        val adventure = json.decodeFromString<Adventure>(jsonString)
        
        // Then
        assertEquals("789", adventure.id)
        assertEquals("Desert Trek", adventure.name)
        assertEquals(5.0, adventure.rating)
        assertEquals(2, adventure.activityTypes.size)
        assertTrue(adventure.activityTypes.contains("trekking"))
        assertTrue(adventure.activityTypes.contains("camping"))
        assertTrue(adventure.isPublic)
        assertFalse(adventure.isVisited)
        assertNull(adventure.category)
    }
    
    @Test
    fun `should deserialize with nested objects correctly`() {
        // Given
        val jsonString = """
            {
                "id": "adv-complex",
                "userId": "user-complex",
                "name": "Complex Adventure",
                "description": "Adventure with all nested objects",
                "rating": 4.8,
                "activityTypes": ["hiking"],
                "location": "Complex Location",
                "isPublic": true,
                "collections": ["Complex Collection"],
                "createdAt": "2024-01-01",
                "updatedAt": "2024-01-02",
                "images": [
                    {
                        "id": "img-1",
                        "image": "https://example.com/img.jpg",
                        "adventure": "adv-complex",
                        "isPrimary": true,
                        "userId": "user-complex"
                    }
                ],
                "link": "https://example.com",
                "longitude": "1.0",
                "latitude": "2.0",
                "visits": [
                    {
                        "id": "visit-1",
                        "startDate": "2024-01-15",
                        "endDate": "2024-01-20",
                        "notes": "Great visit",
                        "timezone": "UTC"
                    }
                ],
                "isVisited": true,
                "category": {
                    "id": "cat-1",
                    "name": "Hiking",
                    "displayName": "Hiking",
                    "icon": "🥾",
                    "numAdventures": "5"
                },
                "attachments": [
                    {
                        "id": "att-1",
                        "file": "https://example.com/file.pdf",
                        "adventure": "adv-complex",
                        "extension": "pdf",
                        "name": "guide",
                        "userId": 1
                    }
                ]
            }
        """.trimIndent()
        
        // When
        val adventure = json.decodeFromString<Adventure>(jsonString)
        
        // Then
        assertEquals("adv-complex", adventure.id)
        assertEquals(1, adventure.images.size)
        assertEquals("img-1", adventure.images[0].id)
        assertEquals(1, adventure.visits.size)
        assertEquals("visit-1", adventure.visits[0].id)
        assertNotNull(adventure.category)
        assertEquals("Hiking", adventure.category?.name)
        assertEquals("🥾", adventure.category?.icon)
        assertEquals(1, adventure.attachments.size)
        assertEquals("guide", adventure.attachments[0].name)
        assertEquals("pdf", adventure.attachments[0].extension)
    }
    
    @Test
    fun `should correctly compare Adventure instances`() {
        // Given
        val adventure1 = Adventure(
            id = "adv-1",
            userId = "user-1",
            name = "Adventure 1",
            description = "Description 1",
            rating = 4.0,
            activityTypes = listOf("hiking"),
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
            attachments = emptyList()
        )
        
        val adventure2 = adventure1.copy()
        val adventure3 = adventure1.copy(id = "adv-2")
        val adventure4 = adventure1.copy(rating = 5.0)
        
        // Then
        assertEquals(adventure1, adventure2)
        assertNotEquals(adventure1, adventure3)
        assertNotEquals(adventure1, adventure4)
        assertEquals(adventure1.hashCode(), adventure2.hashCode())
    }
    
    @Test
    fun `should handle empty collections correctly`() {
        // Given
        val adventure = Adventure(
            id = "empty-collections",
            userId = "user-empty",
            name = "Empty Collections Adventure",
            description = "Adventure with empty collections",
            rating = 3.0,
            activityTypes = emptyList(),
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
            attachments = emptyList()
        )
        
        // Then
        assertTrue(adventure.activityTypes.isEmpty())
        assertTrue(adventure.images.isEmpty())
        assertTrue(adventure.visits.isEmpty())
        assertTrue(adventure.attachments.isEmpty())
        assertNull(adventure.category)
        assertFalse(adventure.isVisited)
    }
}
