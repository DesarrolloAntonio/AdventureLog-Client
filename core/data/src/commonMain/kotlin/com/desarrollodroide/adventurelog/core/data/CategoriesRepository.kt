package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Category

interface CategoriesRepository {
    /**
     * Get all categories
     */
    suspend fun getCategories(): Either<ApiResponse, List<Category>>
    
    /**
     * Get a single category by ID
     */
    suspend fun getCategoryById(categoryId: String): Either<ApiResponse, Category>
    
    /**
     * Create a new category
     */
    suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category>
    
    /**
     * Update an existing category
     */
    suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category>
    
    /**
     * Delete a category
     */
    suspend fun deleteCategory(categoryId: String): Either<ApiResponse, Unit>
}
