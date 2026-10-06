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
                    // A timeout is an IOException too, and "No internet connection." over a slow
                    // server sent people to check a network that was fine (measured).
                    is ApiResponse.IOException -> CANT_REACH_SERVER
                    is ApiResponse.HttpError -> "Could not load your dashboard. Please try again."
                    is ApiResponse.Forbidden -> "You don't have permission to do that."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                }
            )
            is Either.Right -> Either.Right(result.value)
        }
    }
}
