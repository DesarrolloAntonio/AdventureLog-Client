package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class VisitTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    @Test
    fun `should create Visit with all properties`() {
        // Given
        val visit = Visit(
            id = "visit-123",
            startDate = "2024-06-01",
            endDate = "2024-06-07",
            notes = "Amazing week-long vacation with family"
        )
        
        // Then
        assertEquals("visit-123", visit.id)
        assertEquals("2024-06-01", visit.startDate)
        assertEquals("2024-06-07", visit.endDate)
        assertEquals("Amazing week-long vacation with family", visit.notes)
    }
    
    @Test
    fun `should use PreviewData visits correctly`() {
        // Given
        val visit1 = PreviewData.visits[0]
        val visit2 = PreviewData.visits[1]
        
        // Then
        assertEquals("1", visit1.id)
        assertEquals("2024-01-15", visit1.startDate)
        assertEquals("2024-01-20", visit1.endDate)
        assertEquals("Amazing experience", visit1.notes)
        
        assertEquals("2", visit2.id)
        assertEquals("2024-02-01", visit2.startDate)
        assertEquals("2024-02-05", visit2.endDate)
        assertEquals("Great weekend getaway", visit2.notes)
    }
    
    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val visit = Visit(
            id = "v-456",
            startDate = "2024-12-24",
            endDate = "2024-12-26",
            notes = "Christmas holidays"
        )
        
        // When
        val jsonString = json.encodeToString(Visit.serializer(), visit)
        
        // Then
        assertTrue(jsonString.contains("\"id\":\"v-456\""))
        assertTrue(jsonString.contains("\"startDate\":\"2024-12-24\""))
        assertTrue(jsonString.contains("\"endDate\":\"2024-12-26\""))
        assertTrue(jsonString.contains("\"notes\":\"Christmas holidays\""))
    }
    
    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": "visit-789",
                "startDate": "2024-07-01",
                "endDate": "2024-07-15",
                "notes": "Summer vacation in Europe"
            }
        """.trimIndent()
        
        // When
        val visit = json.decodeFromString<Visit>(jsonString)
        
        // Then
        assertEquals("visit-789", visit.id)
        assertEquals("2024-07-01", visit.startDate)
        assertEquals("2024-07-15", visit.endDate)
        assertEquals("Summer vacation in Europe", visit.notes)
    }
    
    @Test
    fun `should correctly compare Visit instances`() {
        // Given
        val visit1 = Visit(
            id = "1",
            startDate = "2024-03-01",
            endDate = "2024-03-03",
            notes = "Weekend trip"
        )
        
        val visit2 = visit1.copy()
        val visit3 = visit1.copy(id = "2")
        val visit4 = visit1.copy(notes = "Different notes")
        
        // Then
        assertEquals(visit1, visit2)
        assertNotEquals(visit1, visit3)
        assertNotEquals(visit1, visit4)
        assertEquals(visit1.hashCode(), visit2.hashCode())
    }
    
    @Test
    fun `should handle same day visits`() {
        // Given
        val sameDayVisit = Visit(
            id = "same-day",
            startDate = "2024-05-15",
            endDate = "2024-05-15",
            notes = "Day trip to the mountains"
        )
        
        // Then
        assertEquals(sameDayVisit.startDate, sameDayVisit.endDate)
    }
    
    @Test
    fun `should handle empty notes`() {
        // Given
        val visitWithoutNotes = Visit(
            id = "no-notes",
            startDate = "2024-08-01",
            endDate = "2024-08-05",
            notes = ""
        )
        
        // Then
        assertEquals("", visitWithoutNotes.notes)
        assertTrue(visitWithoutNotes.notes.isEmpty())
    }
    
    @Test
    fun `should handle various date formats`() {
        // Given - Different date format patterns that might be used
        val visits = listOf(
            Visit("1", "2024-01-01", "2024-01-02", "ISO format"),
            Visit("2", "2024-12-31", "2025-01-01", "Year transition"),
            Visit("3", "2024-02-29", "2024-03-01", "Leap year")
        )
        
        // Then
        assertEquals("2024-01-01", visits[0].startDate)
        assertEquals("2024-12-31", visits[1].startDate)
        assertEquals("2025-01-01", visits[1].endDate)
        assertEquals("2024-02-29", visits[2].startDate) // Leap year date
    }
    
    @Test
    fun `should handle long notes`() {
        // Given
        val longNotes = "This was an incredible adventure! " +
                "We visited multiple locations, tried local cuisine, " +
                "met wonderful people, and created memories that will last a lifetime. " +
                "The weather was perfect throughout our stay."
        
        val visitWithLongNotes = Visit(
            id = "long-notes",
            startDate = "2024-09-01",
            endDate = "2024-09-14",
            notes = longNotes
        )
        
        // Then
        assertEquals(longNotes, visitWithLongNotes.notes)
        assertTrue(visitWithLongNotes.notes.length > 100)
    }
}
