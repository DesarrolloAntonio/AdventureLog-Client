package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CategoryTest : BaseModelTest<Category>() {
    
    @Test
    fun `should create Category with all properties`() {
        val category = TestDataFactory.createCategory(
            id = "cat-hiking",
            name = "hiking",
            displayName = "Hiking",
            icon = "🥾",
            numAdventures = "25"
        )
        
        assertEquals("cat-hiking", category.id)
        assertEquals("hiking", category.name)
        assertEquals("Hiking", category.displayName)
        assertEquals("🥾", category.icon)
        assertEquals("25", category.numAdventures)
    }
    
    @Test
    fun `should use PreviewData categories correctly`() {
        val categories = PreviewData.categories
        
        assertEquals(4, categories.size)
        assertEquals("hotel", categories[0].name)
        assertEquals("🏨", categories[0].icon)
        assertEquals("restaurant", categories[1].name)
        assertEquals("🍽️", categories[1].icon)
    }
    
    @Test
    fun `should serialize and deserialize correctly`() {
        val category = TestDataFactory.createCategory(
            id = "123",
            name = "restaurant",
            displayName = "Restaurant",
            icon = "🍽️",
            numAdventures = "10"
        )
        
        testSerialization(category, Category.serializer()) { jsonString ->
            assertTrue(jsonString.contains("\"id\":\"123\""))
            assertTrue(jsonString.contains("\"name\":\"restaurant\""))
            assertTrue(jsonString.contains("\"icon\":\"🍽️\""))
            assertTrue(jsonString.contains("\"numAdventures\":\"10\""))
        }
    }
    
    @Test
    fun `should correctly implement equals and hashCode`() {
        val category1 = TestDataFactory.createCategory()
        val category2 = category1.copy()
        val category3 = category1.copy(id = "2")
        val category4 = category1.copy(numAdventures = "10")
        
        testEquality(
            original = category1,
            equal = category2,
            different = listOf(category3, category4)
        )
    }
    
    @Test
    fun `should handle edge cases`() {
        val emptyCategory = TestDataFactory.createCategory(
            name = "new_category",
            displayName = "New Category",
            icon = "✨",
            numAdventures = "0"
        )
        assertEquals("0", emptyCategory.numAdventures)
        
        val emojiCategories = listOf("🏨", "🍽️", "🏖️", "⛰️", "🛍️", "🏕️")
        emojiCategories.forEach { emoji ->
            val cat = TestDataFactory.createCategory(icon = emoji)
            assertEquals(emoji, cat.icon)
        }
    }
}
