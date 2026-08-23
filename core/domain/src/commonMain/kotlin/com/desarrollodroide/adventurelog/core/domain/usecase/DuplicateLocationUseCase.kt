package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

/**
 * Copies a location. The server does the work - it clones the fields and the images but leaves
 * out collections and visits, names the copy "Copy of ..." and marks it private.
 */
class DuplicateLocationUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(locationId: String): Either<String, Location> =
        when (val result = locationsRepository.duplicateLocation(locationId)) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> "No internet connection."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                    is ApiResponse.HttpError -> "Could not duplicate this location."
                }
            )
            is Either.Right -> Either.Right(result.value)
        }
}
