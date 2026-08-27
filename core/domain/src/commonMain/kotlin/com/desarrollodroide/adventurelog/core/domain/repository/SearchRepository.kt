package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.SearchHit

interface SearchRepository {
    suspend fun search(query: String, limit: Int): Either<ApiResponse, List<SearchHit>>
}
