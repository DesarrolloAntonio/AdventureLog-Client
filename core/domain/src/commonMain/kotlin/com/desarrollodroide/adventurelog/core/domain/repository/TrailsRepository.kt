package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Trail
import com.desarrollodroide.adventurelog.core.model.TrailFormData

interface TrailsRepository {

    suspend fun createTrail(locationId: String, trail: TrailFormData): Either<ApiResponse, Trail>

    suspend fun updateTrail(
        trailId: String,
        locationId: String,
        trail: TrailFormData
    ): Either<ApiResponse, Trail>

    suspend fun deleteTrail(trailId: String): Either<ApiResponse, Unit>
}
