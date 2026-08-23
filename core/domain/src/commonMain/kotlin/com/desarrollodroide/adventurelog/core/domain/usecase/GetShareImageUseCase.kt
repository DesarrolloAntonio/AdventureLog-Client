package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository

/**
 * The share card the server renders for a location. Fetched rather than drawn locally so a card
 * shared from the phone looks the same as one shared from the web.
 */
class GetShareImageUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(
        locationId: String,
        aspect: String = ASPECT_SQUARE
    ): Either<String, ByteArray> =
        when (val result = locationsRepository.getShareImage(locationId, aspect)) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> "No internet connection."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                    is ApiResponse.HttpError -> "Could not build a share image for this location."
                }
            )
            is Either.Right -> Either.Right(result.value)
        }

    companion object {
        const val ASPECT_SQUARE = "square"
        const val ASPECT_STORY = "story"
        const val ASPECT_LANDSCAPE = "landscape"
    }
}
