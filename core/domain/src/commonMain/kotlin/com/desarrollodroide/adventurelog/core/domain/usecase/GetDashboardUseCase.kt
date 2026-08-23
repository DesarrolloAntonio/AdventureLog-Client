package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.DashboardRepository
import com.desarrollodroide.adventurelog.core.model.Dashboard

class GetDashboardUseCase(
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(): Either<String, Dashboard> {
        return when (val result = dashboardRepository.getDashboard()) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> "No internet connection."
                    is ApiResponse.HttpError -> "Could not load your dashboard. Please try again."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                }
            )
            is Either.Right -> Either.Right(result.value)
        }
    }
}
