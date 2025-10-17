package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

class GetAllLocationsUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(): Either<String, List<Location>> {
        return when (val result = locationsRepository.getAllLocations()) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Map requires network access.")
                    is ApiResponse.HttpError -> Either.Left("Failed to load locations. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
