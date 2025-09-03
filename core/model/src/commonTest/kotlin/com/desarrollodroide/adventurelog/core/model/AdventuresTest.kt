package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AdventuresTest {

    // Helper function to create a fake user object
    private fun createFakeUser(id: String) = UserDetails(
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
    fun `should create Adventures with all properties`() {
        // Given
        val location1 = Location(
            id = "1",
            user = createFakeUser("user1"),
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

        val location2 = Location(
            id = "2",
            user = createFakeUser("user2"),
            name = "Adventure 2",
            description = "Description 2",
            rating = 5.0,
            tags = listOf("camping"),
            location = "Location 2",
            isPublic = false,
            collections = listOf("Collection 2"),
            createdAt = "2024-02-01",
            updatedAt = "2024-02-02",
            images = emptyList(),
            link = "https://link2.com",
            longitude = "3.0",
            latitude = "4.0",
            visits = emptyList(),
            isVisited = true,
            category = null,
            attachments = emptyList(),
            trails = emptyList()
        )

        val locations = Locations(
            count = 2,
            next = "https://api.example.com/adventures?page=2",
            previous = "https://api.example.com/adventures?page=0",
            results = listOf(location1, location2)
        )

        // Then
        assertEquals(2, locations.count)
        assertEquals("https://api.example.com/adventures?page=2", locations.next)
        assertEquals("https://api.example.com/adventures?page=0", locations.previous)
        assertEquals(2, locations.results.size)
        assertEquals("Adventure 1", locations.results[0].name)
        assertEquals("Adventure 2", locations.results[1].name)
    }

    @Test
    fun `should create empty Adventures`() {
        // Given
        val emptyLocations = Locations(
            count = 0,
            next = "",
            previous = "",
            results = emptyList()
        )

        // Then
        assertEquals(0, emptyLocations.count)
        assertEquals("", emptyLocations.next)
        assertEquals("", emptyLocations.previous)
        assertTrue(emptyLocations.results.isEmpty())
    }

    @Test
    fun `should use PreviewData adventures`() {
        // Given
        val locations = Locations(
            count = PreviewData.locations.size,
            next = "https://api.example.com/adventures?page=2",
            previous = "",
            results = PreviewData.locations
        )

        // Then
        assertEquals(3, locations.count)
        assertEquals(3, locations.results.size)
        assertEquals("Lake District Mountain Resort (Pending)", locations.results[0].name)
        assertEquals("Coastal Beach Resort & Spa", locations.results[1].name)
        assertEquals("Mountain View Hotel", locations.results[2].name)
    }

    @Test
    fun `should correctly compare Adventures instances`() {
        // Given
        val locations1 = Locations(
            count = 1,
            next = "next1",
            previous = "prev1",
            results = listOf(PreviewData.locations[0])
        )

        val adventures2 = locations1.copy()
        val adventures3 = locations1.copy(count = 2)
        val adventures4 = locations1.copy(results = emptyList())

        // Then
        assertEquals(locations1, adventures2)
        assertNotEquals(locations1, adventures3)
        assertNotEquals(locations1, adventures4)
        assertEquals(locations1.hashCode(), adventures2.hashCode())
    }

    @Test
    fun `should handle pagination correctly`() {
        // Given
        val firstPage = Locations(
            count = 100,
            next = "https://api.example.com/adventures?page=2&limit=10",
            previous = "",
            results = PreviewData.locations.take(10)
        )

        val middlePage = Locations(
            count = 100,
            next = "https://api.example.com/adventures?page=6&limit=10",
            previous = "https://api.example.com/adventures?page=4&limit=10",
            results = PreviewData.locations.take(10)
        )

        val lastPage = Locations(
            count = 100,
            next = "",
            previous = "https://api.example.com/adventures?page=9&limit=10",
            results = PreviewData.locations.take(10)
        )

        // Then
        assertEquals("", firstPage.previous)
        assertFalse(firstPage.next.isEmpty())

        assertFalse(middlePage.previous.isEmpty())
        assertFalse(middlePage.next.isEmpty())

        assertEquals("", lastPage.next)
        assertFalse(lastPage.previous.isEmpty())
    }

    @Test
    fun `should handle single page results`() {
        // Given
        val singlePage = Locations(
            count = 3,
            next = "",
            previous = "",
            results = PreviewData.locations
        )

        // Then
        assertEquals("", singlePage.next)
        assertEquals("", singlePage.previous)
        assertEquals(3, singlePage.count)
        assertEquals(3, singlePage.results.size)
    }

    @Test
    fun `should handle large count with small results`() {
        // Given
        val paginatedLocations = Locations(
            count = 1000,
            next = "https://api.example.com/adventures?offset=20&limit=20",
            previous = "",
            results = PreviewData.locations
        )

        // Then
        assertEquals(1000, paginatedLocations.count)
        assertEquals(3, paginatedLocations.results.size)
        assertTrue(paginatedLocations.count > paginatedLocations.results.size)
    }

    @Test
    fun `should verify adventures properties`() {
        // Given
        val locations = Locations(
            count = 2,
            next = "next_url",
            previous = "prev_url",
            results = listOf(
                PreviewData.locations[0],
                PreviewData.locations[1]
            )
        )

        // Then
        val firstAdventure = locations.results[0]
        assertEquals("1", firstAdventure.id)
        assertFalse(firstAdventure.isVisited)
        assertEquals(4.5, firstAdventure.rating)

        val secondAdventure = locations.results[1]
        assertEquals("2", secondAdventure.id)
        assertTrue(secondAdventure.isVisited)
        assertEquals(4.8, secondAdventure.rating)
    }

    @Test
    fun `should handle null pagination URLs`() {
        // Given
        val locations = Locations(
            count = 5,
            next = "",
            previous = "",
            results = PreviewData.locations
        )

        // Then
        assertTrue(locations.next.isEmpty())
        assertTrue(locations.previous.isEmpty())
        assertEquals(5, locations.count)
    }
}