package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO

interface CategoryApi {
    /**
     * Get all categories
     */
    suspend fun getCategories(): List<CategoryDTO>
    
    /**
     * Get a single category by ID
     */
    suspend fun getCategoryById(categoryId: String): CategoryDTO
    
    /**
     * Create a new category
     */
    suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO
    
    /**
     * Update an existing category
     */
    suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO
    
    /**
     * Delete a category
     */
    suspend fun deleteCategory(categoryId: String)
}
