package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.GeocodeRepository
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult

class ReverseGeocodeUseCase(
    private val geocodeRepository: GeocodeRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Either<String, ReverseGeocodeResult> {
        return when (val result = geocodeRepository.reverseGeocode(latitude, longitude)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Cannot get location details.")
                    is ApiResponse.HttpError -> Either.Left("Failed to get location details. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
