package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CountriesRepository
import com.desarrollodroide.adventurelog.core.model.Country

class GetCountriesUseCase(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke(): Either<String, List<Country>> =
        when (val result = countriesRepository.getCountries()) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error loading countries, try again later")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
}
