package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Location

class UpdateAdventureCollectionsUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(
        adventureId: String,
        collectionIds: List<String>
    ): Either<String, Location> {
        // Get the current adventure
        return when (val adventureResult = adventuresRepository.getAdventure(adventureId)) {
            is Either.Left -> {
                val errorMessage = when (adventureResult.value) {
                    is ApiResponse.IOException -> "Network error"
                    is ApiResponse.HttpError -> "Server error"
                    is ApiResponse.InvalidCredentials -> "Invalid credentials"
                }
                Either.Left(errorMessage)
            }
            is Either.Right -> {
                val adventure = adventureResult.value
                
                // Update only the collections, keeping everything else the same
                val updateResult = adventuresRepository.updateAdventure(
                    adventureId = adventureId,
                    name = adventure.name,
                    description = adventure.description ?: "",
                    category = adventure.category,
                    rating = adventure.rating ?: 0.0,
                    link = adventure.link ?: "",
                    location = adventure.location ?: "",
                    latitude = adventure.latitude,
                    longitude = adventure.longitude,
                    isPublic = adventure.isPublic,
                    tags = adventure.tags,
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
                    is Either.Right -> Either.Right(updateResult.value)
                }
            }
        }
    }
}
