package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository

class DeleteLocationUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(locationId: String): Either<String, Unit> {
        return when (val result = locationsRepository.deleteLocation(locationId)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Please check your network.")
                    is ApiResponse.HttpError -> Either.Left("Failed to delete location. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
