package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AdventureImageTest : BaseModelTest<ContentImage>() {
    
    @Test
    fun `should create AdventureImage with all properties`() {
        val image = ContentImage(
            id = "img-123",
            image = "https://example.com/photo.jpg",
            isPrimary = true,
            user = "user-456",
            immichId = "immich-789"
        )
        
        assertEquals("img-123", image.id)
        assertEquals("https://example.com/photo.jpg", image.image)
        assertTrue(image.isPrimary)
        assertEquals("user-456", image.user)
        assertEquals("immich-789", image.immichId)
    }
    
    @Test
    fun `should use PreviewData images correctly`() {
        val lakeMountainImages = PreviewData.lakeMountainImages
        val beachResortImages = PreviewData.beachResortImages
        
        assertEquals(5, lakeMountainImages.size)
        assertEquals(4, beachResortImages.size)
        
        // First lake mountain image should be primary
        assertTrue(lakeMountainImages[0].isPrimary)
        assertFalse(lakeMountainImages[1].isPrimary)
        
        // Check user assignment
        assertEquals("user1", lakeMountainImages[0].user)
        assertEquals("user2", beachResortImages[0].user)
    }
    
    @Test
    fun `should serialize and deserialize correctly`() {
        val image = ContentImage(
            id = "test-img",
            image = "https://test.com/image.png",
            isPrimary = false,
            user = "test-user",
            immichId = null
        )
        
        testSerialization(image, ContentImage.serializer()) { jsonString ->
            assertTrue(jsonString.contains("\"id\":\"test-img\""))
            assertTrue(jsonString.contains("\"image\":\"https://test.com/image.png\""))
            assertTrue(jsonString.contains("\"isPrimary\":false"))
            assertTrue(jsonString.contains("\"user\":\"test-user\""))
        }
    }
    
    @Test
    fun `should correctly implement equals and hashCode`() {
        val image1 = ContentImage(
            id = "1",
            image = "https://example.com/1.jpg",
            isPrimary = true,
            user = "user1",
            immichId = null
        )
        
        val image2 = image1.copy()
        val image3 = image1.copy(id = "2")
        val image4 = image1.copy(isPrimary = false)
        
        testEquality(
            original = image1,
            equal = image2,
            different = listOf(image3, image4)
        )
    }
    
    @Test
    fun `should handle primary and non-primary images`() {
        val primaryImage = ContentImage(
            id = "primary",
            image = "https://example.com/primary.jpg",
            isPrimary = true,
            user = "user1",
            immichId = null
        )
        
        val nonPrimaryImage = ContentImage(
            id = "non-primary",
            image = "https://example.com/other.jpg",
            isPrimary = false,
            user = "user1",
            immichId = null
        )
        
        assertTrue(primaryImage.isPrimary)
        assertFalse(nonPrimaryImage.isPrimary)
    }
    
    @Test
    fun `should verify PreviewData image collections`() {
        val allImageCollections = listOf(
            PreviewData.lakeMountainImages,
            PreviewData.beachResortImages,
            PreviewData.mountainHotelImages,
            PreviewData.balnearioImages,
            PreviewData.navalagamellaImages
        )
        
        allImageCollections.forEach { collection ->
            assertTrue(collection.isNotEmpty())
            // Each collection should have exactly one primary image
            val primaryImages = collection.filter { it.isPrimary }
            assertEquals(1, primaryImages.size)
        }
    }
}
