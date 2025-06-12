package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CategoryTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    @Test
    fun `should create Category with all properties`() {
        // Given
        val category = Category(
            id = "cat-hiking",
            name = "hiking",
            displayName = "Hiking",
            icon = "🥾",
            numAdventures = 25
        )
        
        // Then
        assertEquals("cat-hiking", category.id)
        assertEquals("hiking", category.name)
        assertEquals("Hiking", category.displayName)
        assertEquals("🥾", category.icon)
        assertEquals(25, category.numAdventures)
    }
    
    @Test
    fun `should use PreviewData categories correctly`() {
        // Given
        val hotelCategory = PreviewData.categories[0]
        val restaurantCategory = PreviewData.categories[1]
        val beachCategory = PreviewData.categories[2]
        val rutaCategory = PreviewData.categories[3]
        
        // Then
        assertEquals("hotel", hotelCategory.name)
        assertEquals("🏨", hotelCategory.icon)
        assertEquals(5, hotelCategory.numAdventures)
        
        assertEquals("restaurant", restaurantCategory.name)
        assertEquals("🍽️", restaurantCategory.icon)
        assertEquals(3, restaurantCategory.numAdventures)
        
        assertEquals("beach", beachCategory.name)
        assertEquals("🏖️", beachCategory.icon)
        assertEquals(4, beachCategory.numAdventures)
        
        assertEquals("ruta", rutaCategory.name)
        assertEquals("🏞️", rutaCategory.icon)
        assertEquals(2, rutaCategory.numAdventures)
    }
    
    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val category = Category(
            id = "123",
            name = "restaurant",
            displayName = "Restaurant",
            icon = "🍽️",
            numAdventures = 10
        )
        
        // When
        val jsonString = json.encodeToString(Category.serializer(), category)
        
        // Then
        assertTrue(jsonString.contains("\"id\":\"123\""))
        assertTrue(jsonString.contains("\"name\":\"restaurant\""))
        assertTrue(jsonString.contains("\"displayName\":\"Restaurant\""))
        assertTrue(jsonString.contains("\"icon\":\"🍽️\""))
        assertTrue(jsonString.contains("\"numAdventures\":10"))
    }
    
    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": "cat-456",
                "name": "museum",
                "displayName": "Museum",
                "icon": "🏛️",
                "numAdventures": 15
            }
        """.trimIndent()
        
        // When
        val category = json.decodeFromString<Category>(jsonString)
        
        // Then
        assertEquals("cat-456", category.id)
        assertEquals("museum", category.name)
        assertEquals("Museum", category.displayName)
        assertEquals("🏛️", category.icon)
        assertEquals(15, category.numAdventures)
    }
    
    @Test
    fun `should correctly compare Category instances`() {
        // Given
        val category1 = Category(
            id = "1",
            name = "park",
            displayName = "Park",
            icon = "🌳",
            numAdventures = 8
        )
        
        val category2 = category1.copy()
        val category3 = category1.copy(id = "2")
        val category4 = category1.copy(numAdventures = 10)
        
        // Then
        assertEquals(category1, category2)
        assertNotEquals(category1, category3)
        assertNotEquals(category1, category4)
        assertEquals(category1.hashCode(), category2.hashCode())
    }
    
    @Test
    fun `should handle various emoji icons`() {
        // Given
        val categories = listOf(
            Category("1", "hotel", "Hotel", "🏨", 5),
            Category("2", "restaurant", "Restaurant", "🍽️", 10),
            Category("3", "beach", "Beach", "🏖️", 7),
            Category("4", "mountain", "Mountain", "⛰️", 3),
            Category("5", "shopping", "Shopping", "🛍️", 12),
            Category("6", "camping", "Camping", "🏕️", 4)
        )
        
        // Then
        assertEquals("🏨", categories[0].icon)
        assertEquals("🍽️", categories[1].icon)
        assertEquals("🏖️", categories[2].icon)
        assertEquals("⛰️", categories[3].icon)
        assertEquals("🛍️", categories[4].icon)
        assertEquals("🏕️", categories[5].icon)
    }
    
    @Test
    fun `should handle zero adventures count`() {
        // Given
        val emptyCategory = Category(
            id = "empty",
            name = "new_category",
            displayName = "New Category",
            icon = "✨",
            numAdventures = 0
        )
        
        // Then
        assertEquals(0, emptyCategory.numAdventures)
    }
}
