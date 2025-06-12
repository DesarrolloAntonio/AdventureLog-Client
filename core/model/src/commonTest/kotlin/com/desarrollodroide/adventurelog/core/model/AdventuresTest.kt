package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AdventuresTest {
    
    @Test
    fun `should create Adventures with all properties`() {
        // Given
        val adventure1 = Adventure(
            id = "1",
            userId = "user1",
            name = "Adventure 1",
            description = "Description 1",
            rating = 4.0,
            activityTypes = listOf("hiking"),
            location = "Location 1",
            isPublic = true,
            collection = "Collection 1",
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
        
        val adventure2 = Adventure(
            id = "2",
            userId = "user2",
            name = "Adventure 2",
            description = "Description 2",
            rating = 5.0,
            activityTypes = listOf("camping"),
            location = "Location 2",
            isPublic = false,
            collection = "Collection 2",
            createdAt = "2024-02-01",
            updatedAt = "2024-02-02",
            images = emptyList(),
            link = "https://link2.com",
            longitude = "3.0",
            latitude = "4.0",
            visits = emptyList(),
            isVisited = true,
            category = null,
            attachments = emptyList()
        )
        
        val adventures = Adventures(
            count = 2,
            next = "https://api.example.com/adventures?page=2",
            previous = "https://api.example.com/adventures?page=0",
            results = listOf(adventure1, adventure2)
        )
        
        // Then
        assertEquals(2, adventures.count)
        assertEquals("https://api.example.com/adventures?page=2", adventures.next)
        assertEquals("https://api.example.com/adventures?page=0", adventures.previous)
        assertEquals(2, adventures.results.size)
        assertEquals("Adventure 1", adventures.results[0].name)
        assertEquals("Adventure 2", adventures.results[1].name)
    }
    
    @Test
    fun `should create empty Adventures`() {
        // Given
        val emptyAdventures = Adventures(
            count = 0,
            next = "",
            previous = "",
            results = emptyList()
        )
        
        // Then
        assertEquals(0, emptyAdventures.count)
        assertEquals("", emptyAdventures.next)
        assertEquals("", emptyAdventures.previous)
        assertTrue(emptyAdventures.results.isEmpty())
    }
    
    @Test
    fun `should use PreviewData adventures`() {
        // Given
        val adventures = Adventures(
            count = PreviewData.adventures.size,
            next = "https://api.example.com/adventures?page=2",
            previous = "",
            results = PreviewData.adventures
        )
        
        // Then
        assertEquals(3, adventures.count)
        assertEquals(3, adventures.results.size)
        assertEquals("Lake District Mountain Resort (Pending)", adventures.results[0].name)
        assertEquals("Coastal Beach Resort & Spa", adventures.results[1].name)
        assertEquals("Mountain View Hotel", adventures.results[2].name)
    }
    
    @Test
    fun `should correctly compare Adventures instances`() {
        // Given
        val adventures1 = Adventures(
            count = 1,
            next = "next1",
            previous = "prev1",
            results = listOf(PreviewData.adventures[0])
        )
        
        val adventures2 = adventures1.copy()
        val adventures3 = adventures1.copy(count = 2)
        val adventures4 = adventures1.copy(results = emptyList())
        
        // Then
        assertEquals(adventures1, adventures2)
        assertNotEquals(adventures1, adventures3)
        assertNotEquals(adventures1, adventures4)
        assertEquals(adventures1.hashCode(), adventures2.hashCode())
    }
    
    @Test
    fun `should handle pagination correctly`() {
        // Given
        val firstPage = Adventures(
            count = 100,
            next = "https://api.example.com/adventures?page=2&limit=10",
            previous = "",
            results = PreviewData.adventures.take(10)
        )
        
        val middlePage = Adventures(
            count = 100,
            next = "https://api.example.com/adventures?page=6&limit=10",
            previous = "https://api.example.com/adventures?page=4&limit=10",
            results = PreviewData.adventures.take(10)
        )
        
        val lastPage = Adventures(
            count = 100,
            next = "",
            previous = "https://api.example.com/adventures?page=9&limit=10",
            results = PreviewData.adventures.take(10)
        )
        
        // Then
        // First page has no previous
        assertEquals("", firstPage.previous)
        assertFalse(firstPage.next.isEmpty())
        
        // Middle page has both
        assertFalse(middlePage.previous.isEmpty())
        assertFalse(middlePage.next.isEmpty())
        
        // Last page has no next
        assertEquals("", lastPage.next)
        assertFalse(lastPage.previous.isEmpty())
    }
    
    @Test
    fun `should handle single page results`() {
        // Given - All results fit in one page
        val singlePage = Adventures(
            count = 3,
            next = "",
            previous = "",
            results = PreviewData.adventures
        )
        
        // Then
        assertEquals("", singlePage.next)
        assertEquals("", singlePage.previous)
        assertEquals(3, singlePage.count)
        assertEquals(3, singlePage.results.size)
    }
    
    @Test
    fun `should handle large count with small results`() {
        // Given - Typical paginated response
        val paginatedAdventures = Adventures(
            count = 1000,  // Total count in database
            next = "https://api.example.com/adventures?offset=20&limit=20",
            previous = "",
            results = PreviewData.adventures  // Only returning first page
        )
        
        // Then
        assertEquals(1000, paginatedAdventures.count)
        assertEquals(3, paginatedAdventures.results.size)  // Only 3 items in current page
        assertTrue(paginatedAdventures.count > paginatedAdventures.results.size)
    }
    
    @Test
    fun `should verify adventures properties`() {
        // Given
        val adventures = Adventures(
            count = 2,
            next = "next_url",
            previous = "prev_url",
            results = listOf(
                PreviewData.adventures[0],
                PreviewData.adventures[1]
            )
        )
        
        // Then
        // Verify first adventure
        val firstAdventure = adventures.results[0]
        assertEquals("1", firstAdventure.id)
        assertFalse(firstAdventure.isVisited)
        assertEquals(4.5, firstAdventure.rating)
        
        // Verify second adventure
        val secondAdventure = adventures.results[1]
        assertEquals("2", secondAdventure.id)
        assertTrue(secondAdventure.isVisited)
        assertEquals(4.8, secondAdventure.rating)
    }
    
    @Test
    fun `should handle null pagination URLs`() {
        // Given
        val adventures = Adventures(
            count = 5,
            next = "",
            previous = "",
            results = PreviewData.adventures
        )
        
        // Then
        assertTrue(adventures.next.isEmpty())
        assertTrue(adventures.previous.isEmpty())
        assertEquals(5, adventures.count)
    }
}
