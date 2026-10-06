package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.model.Region

/**
 * The regions of one country - what the world screen drills into.
 *
 * The repository call existed and nothing reached it: the country detail screen was a TODO that
 * printed the country code.
 */
class GetRegionsUseCase(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke(countryCode: String): Either<String, List<Region>> =
        when (val result = countriesRepository.getRegions(countryCode)) {
            is Either.Left -> when (result.value) {
                is ApiResponse.IOException -> Either.Left(CANT_REACH_SERVER)
                is ApiResponse.HttpError -> Either.Left("Could not load the regions, try again later")
                is ApiResponse.Forbidden -> Either.Left("You don't have permission to do that.")
                is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
            }
            is Either.Right -> Either.Right(result.value)
        }
}
