package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData

class CreateLocationUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        tags: List<String>,
        visits: List<VisitFormData> = emptyList(),
        price: Double? = null,
        priceCurrency: String? = null,
        /**
         * Collections the new place joins on creation. The server takes them in the same POST, so
         * a place added from inside a collection needs one call rather than a create followed by
         * a membership update that can fail on its own.
         */
        collectionIds: List<String> = emptyList()
    ): Either<String, Location> {
        if (name.isBlank()) {
            return Either.Left("Location name is required")
        }
        
        return when (val result = locationsRepository.createLocation(
            name = name,
            description = description,
            category = category,
            rating = rating,
            link = link,
            location = location,
            latitude = latitude,
            longitude = longitude,
            isPublic = isPublic,
            visits = visits,
            price = price,
            priceCurrency = priceCurrency,
            activityTypes = tags,
            collectionIds = collectionIds
        )) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left(CANT_REACH_SERVER)
                    is ApiResponse.HttpError -> Either.Left("Failed to create location. Please try again.")
                    is ApiResponse.Forbidden -> Either.Left("You don't have permission to do that.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
