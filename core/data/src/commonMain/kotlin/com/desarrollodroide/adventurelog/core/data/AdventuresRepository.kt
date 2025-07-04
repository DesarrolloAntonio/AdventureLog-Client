package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit

interface AdventuresRepository {

    suspend fun getAdventures(
        page: Int, pageSize: Int
    ): Either<ApiResponse, List<Adventure>>

    suspend fun getAdventure(
        objectId: String
    ): Either<ApiResponse, Adventure>

    suspend fun createAdventure(
        name: String,
        description: String,
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visitDates: Visit?
    ): Either<String, Adventure>

}
