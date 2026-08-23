package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData

interface VisitsRepository {

    suspend fun createVisit(
        locationId: String,
        visit: VisitFormData
    ): Either<ApiResponse, Visit>

    suspend fun updateVisit(
        visitId: String,
        locationId: String,
        visit: VisitFormData
    ): Either<ApiResponse, Visit>

    suspend fun deleteVisit(visitId: String): Either<ApiResponse, Unit>
}
