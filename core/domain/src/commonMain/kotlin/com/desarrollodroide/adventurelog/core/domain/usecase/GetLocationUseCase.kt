package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

class GetLocationUseCase(
    private val locationsRepository: LocationsRepository
) {
    /**
     * [fromServer] skips the copy kept from the list the place was opened from. An edit form
     * seeded from that copy saved it back over whatever had changed on the server since (measured).
     */
    suspend operator fun invoke(locationId: String, fromServer: Boolean = false): Either<String, Location> {
        val result = if (fromServer) {
            locationsRepository.fetchLocation(locationId)
        } else {
            locationsRepository.getLocation(locationId)
        }
        return when (result) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.HttpError -> Either.Left("The server could not load this place. Please try again.")
                    is ApiResponse.IOException -> Either.Left("Can't reach the server. Check your connection.")
                    is ApiResponse.Forbidden -> Either.Left("You don't have permission to do that.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Authentication error. Please login again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }

    fun clearSelectedLocation() {
        locationsRepository.selectedLocation = null
    }
}
