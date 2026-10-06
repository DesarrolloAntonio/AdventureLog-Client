package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Category
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a CategoriesRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class CategoriesRepositoryStub : CategoriesRepository {
    override suspend fun getCategories(): Either<ApiResponse, List<Category>> = unused()
    override suspend fun getCategoryById(categoryId: String): Either<ApiResponse, Category> = unused()
    override suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category> = unused()
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category> = unused()
    override suspend fun deleteCategory(categoryId: String): Either<ApiResponse, Unit> = unused()
}
