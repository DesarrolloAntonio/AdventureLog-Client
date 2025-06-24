package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO

interface CategoryApi {
    suspend fun getCategories(): List<CategoryDTO>
}
