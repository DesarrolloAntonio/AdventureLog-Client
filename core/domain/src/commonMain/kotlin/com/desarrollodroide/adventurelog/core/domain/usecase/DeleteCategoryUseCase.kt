package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CategoriesRepository

class DeleteCategoryUseCase(
    private val categoriesRepository: CategoriesRepository
) {
    suspend operator fun invoke(categoryId: String): Either<String, Unit> {
        // Validate required fields
        if (categoryId.isBlank()) {
            return Either.Left("Category ID is required")
        }
        
        // Delete the category
        return when (val result = categoriesRepository.deleteCategory(categoryId)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Please check your network.")
                    is ApiResponse.HttpError -> Either.Left("Failed to delete category. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(Unit)
        }
    }
}
