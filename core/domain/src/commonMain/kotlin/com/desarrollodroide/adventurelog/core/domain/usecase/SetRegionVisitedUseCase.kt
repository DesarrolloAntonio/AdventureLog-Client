package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository

/** Ticks a region on or off. The World tab is a scoreboard nobody could write to before this. */
class SetRegionVisitedUseCase(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke(regionId: String, visited: Boolean): Either<String, Unit> {
        val result: Either<ApiResponse, Any> = if (visited) {
            countriesRepository.markRegionVisited(regionId)
        } else {
            countriesRepository.unmarkRegionVisited(regionId)
        }
        return when (result) {
            is Either.Right -> Either.Right(Unit)
            is Either.Left -> when (result.value) {
                is ApiResponse.IOException -> Either.Left(CANT_REACH_SERVER)
                is ApiResponse.HttpError -> Either.Left("Could not save that, try again later")
                is ApiResponse.Forbidden -> Either.Left("You don't have permission to do that.")
                is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
            }
        }
    }
}
