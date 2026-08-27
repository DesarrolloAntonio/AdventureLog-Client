package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

class UpdateLocationCollectionsUseCase(
    private val locationsRepository: LocationsRepository,
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        locationId: String,
        collectionIds: List<String>
    ): Either<String, Location> {
        return when (val locationResult = locationsRepository.getLocation(locationId)) {
            is Either.Left -> {
                val errorMessage = when (locationResult.value) {
                    is ApiResponse.IOException -> "Network error"
                    is ApiResponse.HttpError -> "Server error"
                    is ApiResponse.InvalidCredentials -> "Invalid credentials"
                }
                Either.Left(errorMessage)
            }
            is Either.Right -> {
                val location = locationResult.value
                
                val updateResult = locationsRepository.updateLocation(
                    adventureId = locationId,
                    name = location.name,
                    description = location.description ?: "",
                    category = location.category,
                    rating = location.rating ?: 0.0,
                    link = location.link ?: "",
                    location = location.location ?: "",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    isPublic = location.isPublic,
                    tags = location.tags,
                    collections = collectionIds
                )
                
                when (updateResult) {
                    is Either.Left -> {
                        val errorMessage = when (updateResult.value) {
                            is ApiResponse.IOException -> "Network error"
                            is ApiResponse.HttpError -> "Server error" 
                            is ApiResponse.InvalidCredentials -> "Invalid credentials"
                        }
                        Either.Left(errorMessage)
                    }
                    is Either.Right -> {
                        // The collections this place joined or left now hold a different number
                        // of places, and the collections screen reads that from a cache which
                        // knows nothing about what just happened here.
                        collectionsRepository.refreshCollections()
                        Either.Right(updateResult.value)
                    }
                }
            }
        }
    }
}
