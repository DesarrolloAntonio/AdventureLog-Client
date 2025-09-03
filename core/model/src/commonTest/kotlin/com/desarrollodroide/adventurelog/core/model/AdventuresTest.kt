package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

class AdventuresTest : BaseModelTest<Locations>() {

    @Test
    fun `should create Adventures with all properties`() {
        val locations = listOf(
            TestDataFactory.createLocation(id = "1"),
            TestDataFactory.createLocation(id = "2"),
            TestDataFactory.createLocation(id = "3")
        )

        val adventures = Locations(
            count = 3,
            next = "https://api.example.com/adventures?page=2",
            previous = "https://api.example.com/adventures?page=0",
            results = locations
        )

        assertEquals(3, adventures.count)
        assertEquals("https://api.example.com/adventures?page=2", adventures.next)
        assertEquals("https://api.example.com/adventures?page=0", adventures.previous)
        assertEquals(3, adventures.results.size)
        assertEquals("1", adventures.results[0].id)
    }

    @Test
    fun `should use PreviewData adventures`() {
        val adventures = Locations(
            count = PreviewData.locations.size,
            next = "",
            previous = "",
            results = PreviewData.locations
        )

        assertEquals(3, adventures.count)
        assertEquals(PreviewData.locations, adventures.results)
        assertTrue(adventures.results.isNotEmpty())
    }

    @Test
    fun `should handle pagination correctly`() {
        val firstPage = Locations(
            count = 100,
            next = "https://api.example.com/adventures?page=2",
            previous = "",
            results = List(20) { index ->
                TestDataFactory.createLocation(id = "adv-$index")
            }
        )

        val middlePage = Locations(
            count = 100,
            next = "https://api.example.com/adventures?page=3",
            previous = "https://api.example.com/adventures?page=1",
            results = List(20) { index ->
                TestDataFactory.createLocation(id = "adv-${index + 20}")
            }
        )

        val lastPage = Locations(
            count = 100,
            next = "",
            previous = "https://api.example.com/adventures?page=4",
            results = List(20) { index ->
                TestDataFactory.createLocation(id = "adv-${index + 80}")
            }
        )

        assertEquals("", firstPage.previous)
        assertEquals("https://api.example.com/adventures?page=2", firstPage.next)
        assertTrue(middlePage.next.isNotEmpty())
        assertTrue(middlePage.previous.isNotEmpty())
        assertEquals("", lastPage.next)
        assertTrue(lastPage.previous.isNotEmpty())
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val locations = listOf(TestDataFactory.createLocation())
        
        val adventures1 = Locations(10, "next", "prev", locations)
        val adventures2 = adventures1.copy()
        val adventures3 = adventures1.copy(count = 20)
        val adventures4 = adventures1.copy(results = emptyList())

        testEquality(
            original = adventures1,
            equal = adventures2,
            different = listOf(adventures3, adventures4)
        )
    }

    @Test
    fun `should handle empty results`() {
        val emptyAdventures = Locations(
            count = 0,
            next = "",
            previous = "",
            results = emptyList()
        )

        assertEquals(0, emptyAdventures.count)
        assertEquals("", emptyAdventures.next)
        assertEquals("", emptyAdventures.previous)
        assertTrue(emptyAdventures.results.isEmpty())
    }

    @Test
    fun `should handle null pagination URLs`() {
        val adventures = Locations(
            count = 5,
            next = "",
            previous = "",
            results = List(5) { TestDataFactory.createLocation(id = "$it") }
        )

        assertEquals("", adventures.next)
        assertEquals("", adventures.previous)
        assertEquals(5, adventures.results.size)
    }

    @Test
    fun `should handle single page results`() {
        val singlePageAdventures = Locations(
            count = 3,
            next = "",
            previous = "",
            results = List(3) { TestDataFactory.createLocation(id = "single-$it") }
        )

        assertEquals(3, singlePageAdventures.count)
        assertEquals("", singlePageAdventures.next)
        assertEquals("", singlePageAdventures.previous)
        assertEquals(singlePageAdventures.count, singlePageAdventures.results.size)
    }

    @Test
    fun `should handle large count with small results`() {
        val adventures = Locations(
            count = 1000,
            next = "https://api.example.com/adventures?page=2",
            previous = "",
            results = List(10) { TestDataFactory.createLocation(id = "large-$it") }
        )

        assertEquals(1000, adventures.count)
        assertEquals(10, adventures.results.size)
        assertTrue(adventures.count > adventures.results.size)
    }

    @Test
    fun `should verify adventures properties`() {
        val adventures = Locations(
            count = 2,
            next = "next-url",
            previous = "prev-url",
            results = listOf(
                TestDataFactory.createLocation(id = "1", name = "Adventure 1", isVisited = true),
                TestDataFactory.createLocation(id = "2", name = "Adventure 2", isVisited = false)
            )
        )

        assertEquals(2, adventures.results.size)
        assertTrue(adventures.results[0].isVisited)
        assertTrue(!adventures.results[1].isVisited)
        assertEquals("Adventure 1", adventures.results[0].name)
        assertEquals("Adventure 2", adventures.results[1].name)
    }
}
