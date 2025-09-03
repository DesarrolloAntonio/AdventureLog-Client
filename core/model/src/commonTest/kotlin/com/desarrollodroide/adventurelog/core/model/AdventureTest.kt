package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AdventureTest : BaseModelTest<Location>() {

    @Test
    fun `should use PreviewData adventures correctly`() {
        val location = PreviewData.locations[0]

        assertEquals("1", location.id)
        assertEquals("Lake District Mountain Resort (Pending)", location.name)
        assertEquals(4.5, location.rating)
        assertFalse(location.isVisited)
        assertFalse(location.isPublic)
        assertEquals(PreviewData.lakeMountainImages, location.images)
        assertEquals(PreviewData.categories[0], location.category)
        assertEquals(5, location.images.size)
        assertEquals(1, location.visits.size)
    }

    @Test
    fun `should verify relationships between PreviewData entities`() {
        val location = PreviewData.locations[0]
        val category = location.category
        val images = location.images

        assertNotNull(category)
        assertEquals("hotel", category?.name)
        assertEquals("🏨", category?.icon)

        val primaryImage = images.find { it.isPrimary }
        assertNotNull(primaryImage)
        assertEquals("1-1", primaryImage?.id)
    }

    @Test
    fun `should create Adventure with all properties`() {
        val category = TestDataFactory.createCategory(
            id = "cat-1",
            name = "Hiking",
            displayName = "Hiking",
            icon = "🏞️",
            numAdventures = "10"
        )

        val location = TestDataFactory.createLocation(
            id = "adv-1",
            name = "Mountain Trail Adventure",
            description = "A beautiful mountain trail with stunning views",
            rating = 4.5,
            tags = listOf("hiking", "nature"),
            isPublic = true,
            isVisited = true,
            category = category
        )

        assertEquals("adv-1", location.id)
        assertEquals("Mountain Trail Adventure", location.name)
        assertEquals(4.5, location.rating)
        assertEquals(2, location.tags.size)
        assertTrue(location.isPublic)
        assertTrue(location.isVisited)
        assertNotNull(location.category)
        assertEquals("Hiking", location.category?.name)
    }

    @Test
    fun `should serialize and deserialize correctly`() {
        val location = TestDataFactory.createLocation(
            id = "123",
            name = "Test Adventure",
            description = "Test description",
            rating = 3.5,
            tags = listOf("hiking"),
            isPublic = false,
            isVisited = false
        )

        testSerialization(location, Location.serializer()) { jsonString ->
            assertTrue(jsonString.contains("\"id\":\"123\""))
            assertTrue(jsonString.contains("\"name\":\"Test Adventure\""))
            assertTrue(jsonString.contains("\"rating\":3.5"))
            assertTrue(jsonString.contains("\"isPublic\":false"))
            assertTrue(jsonString.contains("\"tags\":[\"hiking\"]"))
        }
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val location1 = TestDataFactory.createLocation(id = "adv-1")
        val location2 = location1.copy()
        val location3 = location1.copy(id = "adv-2")
        val location4 = location1.copy(rating = 5.0)

        testEquality(
            original = location1,
            equal = location2,
            different = listOf(location3, location4)
        )
    }

    @Test
    fun `should handle empty collections correctly`() {
        val location = TestDataFactory.createLocation(
            id = "empty-collections",
            name = "Empty Collections Adventure",
            tags = emptyList(),
            isVisited = false,
            category = null
        )

        assertTrue(location.tags.isEmpty())
        assertTrue(location.images.isEmpty())
        assertTrue(location.visits.isEmpty())
        assertTrue(location.attachments.isEmpty())
        assertNull(location.category)
        assertFalse(location.isVisited)
    }

    @Test
    fun `should handle nested objects correctly`() {
        val category = TestDataFactory.createCategory()
        val visit = TestDataFactory.createVisit()
        val attachment = TestDataFactory.createAttachment()
        
        val location = TestDataFactory.createLocation(
            category = category
        ).copy(
            visits = listOf(visit),
            attachments = listOf(attachment),
            images = listOf(
                ContentImage(
                    id = "img-1",
                    image = "https://example.com/img.jpg",
                    isPrimary = true,
                    user = "user-1",
                    immichId = null
                )
            )
        )

        assertEquals(1, location.images.size)
        assertEquals("img-1", location.images[0].id)
        assertEquals(1, location.visits.size)
        assertEquals(visit.id, location.visits[0].id)
        assertNotNull(location.category)
        assertEquals(category.name, location.category?.name)
        assertEquals(1, location.attachments.size)
        assertEquals(attachment.name, location.attachments[0].name)
    }
}
