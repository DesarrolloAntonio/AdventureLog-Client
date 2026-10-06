package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory

/**
 * What is near a point that the account does not already have.
 *
 * The search takes either a pair of coordinates or a place name for the server to geocode. Asking
 * for neither is a caller bug rather than a state worth a message, so it is refused here instead
 * of turning into a 400 from the server.
 */
class GetRecommendationsUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): Either<String, List<Recommendation>> {
        val hasPoint = latitude != null && longitude != null
        if (!hasPoint && place.isNullOrBlank()) {
            return Either.Left("Choose a place to look around, or type where to look")
        }

        return when (
            val result = locationsRepository.getRecommendations(
                latitude = latitude,
                longitude = longitude,
                place = place,
                category = category,
                radiusMetres = radiusMetres
            )
        ) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> CANT_REACH_SERVER
                    is ApiResponse.HttpError -> "Could not search there. Try somewhere else."
                    is ApiResponse.Forbidden -> "You don't have permission to do that."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                }
            )
        }
    }
}
