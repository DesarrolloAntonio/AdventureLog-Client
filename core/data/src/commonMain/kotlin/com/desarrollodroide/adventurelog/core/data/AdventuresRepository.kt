package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Adventure

interface AdventuresRepository {

    suspend fun getAdventures(page: Int): Either<ApiResponse, List<Adventure>>
    suspend fun getAdventure(objectId: String): Either<ApiResponse, Adventure>

}
