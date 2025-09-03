package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisitTest : BaseModelTest<Visit>() {
    
    @Test
    fun `should create Visit with all properties`() {
        val visit = TestDataFactory.createVisit(
            id = "visit-123",
            location = "loc-1",
            startDate = "2024-06-01",
            endDate = "2024-06-07",
            notes = "Amazing week-long vacation with family",
            timezone = "UTC"
        )
        
        assertEquals("visit-123", visit.id)
        assertEquals("loc-1", visit.location)
        assertEquals("2024-06-01", visit.startDate)
        assertEquals("2024-06-07", visit.endDate)
        assertEquals("Amazing week-long vacation with family", visit.notes)
        assertEquals("UTC", visit.timezone)
    }
    
    @Test
    fun `should use PreviewData visits correctly`() {
        val visits = PreviewData.visits
        
        assertEquals(2, visits.size)
        assertEquals("1", visits[0].id)
        assertEquals("location-1", visits[0].location)
        assertEquals("2024-01-15", visits[0].startDate)
        assertEquals("Amazing experience", visits[0].notes)
        
        assertEquals("2", visits[1].id)
        assertEquals("location-2", visits[1].location)
        assertEquals("2024-02-01", visits[1].startDate)
        assertEquals("Great weekend getaway", visits[1].notes)
    }
    
    @Test
    fun `should serialize and deserialize correctly`() {
        val visit = TestDataFactory.createVisit(
            id = "v-456",
            location = "loc-2",
            startDate = "2024-12-24",
            endDate = "2024-12-26",
            notes = "Christmas holidays"
        )
        
        testSerialization(visit, Visit.serializer()) { jsonString ->
            assertTrue(jsonString.contains("\"id\":\"v-456\""))
            assertTrue(jsonString.contains("\"location\":\"loc-2\""))
            assertTrue(jsonString.contains("\"startDate\":\"2024-12-24\""))
            assertTrue(jsonString.contains("\"notes\":\"Christmas holidays\""))
        }
    }
    
    @Test
    fun `should correctly implement equals and hashCode`() {
        val visit1 = TestDataFactory.createVisit()
        val visit2 = visit1.copy()
        val visit3 = visit1.copy(id = "2")
        val visit4 = visit1.copy(notes = "Different notes")
        
        testEquality(
            original = visit1,
            equal = visit2,
            different = listOf(visit3, visit4)
        )
    }
    
    @Test
    fun `should handle edge cases`() {
        // Same day visit
        val sameDayVisit = TestDataFactory.createVisit(
            startDate = "2024-05-15",
            endDate = "2024-05-15",
            notes = "Day trip to the mountains"
        )
        assertEquals(sameDayVisit.startDate, sameDayVisit.endDate)
        
        // Empty notes
        val emptyNotesVisit = TestDataFactory.createVisit(notes = "")
        assertTrue(emptyNotesVisit.notes!!.isEmpty())
        
        // Long notes
        val longNotes = "This was an incredible adventure! " +
                "We visited multiple locations, tried local cuisine, " +
                "met wonderful people, and created memories that will last a lifetime. " +
                "The weather was perfect throughout our stay."
        val longNotesVisit = TestDataFactory.createVisit(notes = longNotes)
        assertEquals(longNotes, longNotesVisit.notes)
        assertTrue(longNotesVisit.notes!!.length > 100)
        
        // Various date formats
        val visits = listOf(
            TestDataFactory.createVisit(startDate = "2024-01-01", endDate = "2024-01-02"),
            TestDataFactory.createVisit(startDate = "2024-12-31", endDate = "2025-01-01"),
            TestDataFactory.createVisit(startDate = "2024-02-29", endDate = "2024-03-01")
        )
        assertEquals("2024-01-01", visits[0].startDate)
        assertEquals("2024-12-31", visits[1].startDate)
        assertEquals("2025-01-01", visits[1].endDate)
        assertEquals("2024-02-29", visits[2].startDate)
    }
}
