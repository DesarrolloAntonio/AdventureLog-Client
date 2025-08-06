package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure

class GetAllAdventures(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(): Either<ApiResponse, List<Adventure>> {
        return adventuresRepository.getAllAdventures()
    }
}
