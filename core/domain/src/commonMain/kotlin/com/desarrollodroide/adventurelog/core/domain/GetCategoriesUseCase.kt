package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CategoriesRepository
import com.desarrollodroide.adventurelog.core.model.Category

class GetCategoriesUseCase(
    private val categoriesRepository: CategoriesRepository
) {
    suspend operator fun invoke(): Either<String, List<Category>> {
        return categoriesRepository.getCategories()
    }
}
