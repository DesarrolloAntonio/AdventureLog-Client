package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository

/** The web's "update visited regions": ask the server to work out where your places actually are. */
class RefreshVisitedRegionsUseCase(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke(): Either<String, Pair<Int, Int>> =
        when (val result = countriesRepository.refreshVisitedRegions()) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> when (result.value) {
                is ApiResponse.IOException -> Either.Left("Can't reach the server. Check your connection.")
                is ApiResponse.HttpError -> Either.Left("Could not update that, try again later")
                is ApiResponse.Forbidden -> Either.Left("You don't have permission to do that.")
                is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
            }
        }
}
