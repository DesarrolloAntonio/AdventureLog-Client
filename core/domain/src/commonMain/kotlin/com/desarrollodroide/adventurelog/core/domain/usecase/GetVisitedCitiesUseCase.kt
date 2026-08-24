package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.model.VisitedCity

/**
 * The cities the user has been to.
 *
 * This used to call the repository and throw the answer away, returning Unit - so nothing could
 * ever show a visited city. It mirrors [GetVisitedRegionsUseCase] now.
 */
class GetVisitedCitiesUseCase(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke(): Either<String, List<VisitedCity>> =
        when (val result = countriesRepository.getVisitedCities()) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> "Network unavailable"
                    is ApiResponse.HttpError -> "Error getting visited cities, try again later"
                    is ApiResponse.InvalidCredentials -> "Session expired, please log in again"
                }
            )
            is Either.Right -> Either.Right(result.value)
        }
}
