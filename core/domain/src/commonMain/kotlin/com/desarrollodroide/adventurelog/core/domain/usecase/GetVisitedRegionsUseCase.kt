package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CountriesRepository
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import kotlinx.coroutines.flow.StateFlow

class GetVisitedRegionsUseCase(
    private val countriesRepository: CountriesRepository
) {
    val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = countriesRepository.visitedRegionsFlow
    
    suspend operator fun invoke(): Either<String, List<VisitedRegion>> =
        when (val result = countriesRepository.getVisitedRegions()) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error getting visited regions, try again later")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
}
