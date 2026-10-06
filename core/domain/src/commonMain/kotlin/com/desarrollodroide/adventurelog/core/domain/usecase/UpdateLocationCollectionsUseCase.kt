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
        // Only the collections go to the server. Rebuilding the whole place from the copy kept
        // for the list sent every other field back as that copy had it, undoing anything changed
        // elsewhere since the list loaded.
        return when (val updateResult = locationsRepository.updateLocationCollections(locationId, collectionIds)) {
            is Either.Left -> {
                val errorMessage = when (updateResult.value) {
                    is ApiResponse.IOException -> CANT_REACH_SERVER
                    is ApiResponse.HttpError -> "Server error"
                    is ApiResponse.Forbidden -> "That place belongs to someone else."
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
