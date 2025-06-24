package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Category

interface CategoriesRepository {
    suspend fun getCategories(): Either<String, List<Category>>
}
