package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

class GetLocationUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(locationId: String): Either<String, Location> {
        return when (val result = locationsRepository.getLocation(locationId)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.HttpError -> Either.Left("Network error. Please check your connection.")
                    is ApiResponse.IOException -> Either.Left("Connection error. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Authentication error. Please login again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
