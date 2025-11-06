package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

class GetLocationsUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int): Either<String, List<Location>> =
        when (val result = locationsRepository.getLocations(page, pageSize)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error getting locations, try again later")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }

    fun selectLocation(location: Location) {
        println("🟡 [GetLocationsUseCase] Setting selectedLocation: ${location.id} - ${location.name}")
        locationsRepository.selectedLocation = location
        println("🟡 [GetLocationsUseCase] selectedLocation is now: ${locationsRepository.selectedLocation?.name}")
    }
}
