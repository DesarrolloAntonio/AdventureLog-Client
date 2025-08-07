package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.GeocodeRepository
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult

class SearchLocationsUseCase(
    private val geocodeRepository: GeocodeRepository
) {
    suspend operator fun invoke(query: String): Either<String, List<GeocodeSearchResult>> {
        return when (val result = geocodeRepository.searchLocations(query)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Cannot search locations.")
                    is ApiResponse.HttpError -> Either.Left("Failed to search locations. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
