package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CategoriesRepository
import com.desarrollodroide.adventurelog.core.model.Category

class CreateCategoryUseCase(
    private val categoriesRepository: CategoriesRepository
) {
    suspend operator fun invoke(
        name: String,
        displayName: String,
        icon: String?
    ): Either<String, Category> {
        // Validate required fields
        if (name.isBlank()) {
            return Either.Left("Category name is required")
        }
        
        if (displayName.isBlank()) {
            return Either.Left("Display name is required")
        }
        
        // Create the category
        return when (val result = categoriesRepository.createCategory(
            name = name,
            displayName = displayName,
            icon = icon
        )) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Please check your network.")
                    is ApiResponse.HttpError -> Either.Left("Failed to create category. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
