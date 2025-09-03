package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AdventureImageTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun `should create AdventureImage with all properties`() {
        // Given
        val image = ContentImage(
            id = "img-123",
            image = "https://example.com/adventure-photo.jpg",
            isPrimary = true,
            user = "user-789"
        )

        // Then
        assertEquals("img-123", image.id)
        assertEquals("https://example.com/adventure-photo.jpg", image.image)
        assertTrue(image.isPrimary)
        assertEquals("user-789", image.user)
    }

    @Test
    fun `should use PreviewData images correctly`() {
        // Given
        val lakeMountainImage = PreviewData.lakeMountainImages[0]
        val beachResortImage = PreviewData.beachResortImages[0]

        // Then
        assertEquals("1-1", lakeMountainImage.id)
        assertTrue(lakeMountainImage.isPrimary)
        assertEquals("user1", lakeMountainImage.user)

        assertEquals("2-1", beachResortImage.id)
        assertTrue(beachResortImage.isPrimary)
        assertEquals("user2", beachResortImage.user)
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val image = ContentImage(
            id = "test-img",
            image = "https://test.com/image.jpg",
            isPrimary = false,
            user = "test-user"
        )

        // When
        val jsonString = json.encodeToString(ContentImage.serializer(), image)

        // Then
        assertTrue(jsonString.contains("\"id\":\"test-img\""))
        assertTrue(jsonString.contains("\"image\":\"https://test.com/image.jpg\""))
        assertTrue(jsonString.contains("\"isPrimary\":false"))
        assertTrue(jsonString.contains("\"user\":\"test-user\""))
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": "json-img",
                "image": "https://json.com/photo.png",
                "isPrimary": true,
                "user": "json-user"
            }
        """.trimIndent()

        // When
        val image = json.decodeFromString<ContentImage>(jsonString)

        // Then
        assertEquals("json-img", image.id)
        assertEquals("https://json.com/photo.png", image.image)
        assertTrue(image.isPrimary)
        assertEquals("json-user", image.user)
    }

    @Test
    fun `should correctly compare AdventureImage instances`() {
        // Given
        val image1 = ContentImage(
            id = "1",
            image = "https://example.com/1.jpg",
            isPrimary = true,
            user = "user-1"
        )

        val image2 = image1.copy()
        val image3 = image1.copy(id = "2")
        val image4 = image1.copy(isPrimary = false)

        // Then
        assertEquals(image1, image2)
        assertNotEquals(image1, image3)
        assertNotEquals(image1, image4)
        assertEquals(image1.hashCode(), image2.hashCode())
    }

    @Test
    fun `should handle primary and non-primary images`() {
        // Given
        val primaryImage = ContentImage(
            id = "primary",
            image = "https://example.com/primary.jpg",
            isPrimary = true,
            user = "user"
        )

        val secondaryImage = ContentImage(
            id = "secondary",
            image = "https://example.com/secondary.jpg",
            isPrimary = false,
            user = "user"
        )

        // Then
        assertTrue(primaryImage.isPrimary)
        assertFalse(secondaryImage.isPrimary)
    }

    @Test
    fun `should verify PreviewData image collections`() {
        // Given
        val lakeMountainImages = PreviewData.lakeMountainImages
        val beachResortImages = PreviewData.beachResortImages
        val mountainHotelImages = PreviewData.mountainHotelImages

        // Then
        assertEquals(5, lakeMountainImages.size)
        assertEquals(4, beachResortImages.size)
        assertEquals(6, mountainHotelImages.size)

        // Verify only one primary image per collection
        assertEquals(1, lakeMountainImages.count { it.isPrimary })
        assertEquals(1, beachResortImages.count { it.isPrimary })
        assertEquals(1, mountainHotelImages.count { it.isPrimary })
    }
}