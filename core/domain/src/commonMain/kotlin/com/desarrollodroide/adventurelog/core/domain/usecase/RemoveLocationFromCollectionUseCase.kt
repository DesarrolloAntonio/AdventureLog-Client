package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location

/**
 * Takes a place out of one collection and leaves it everywhere else (QA 04, CO-10).
 *
 * The web does it from the card: the place's collections without this one. The server replaces
 * the whole list, so the list is read from the server first - a copy kept by the screen would
 * take the place out of any collection it joined since that copy was made.
 */
class RemoveLocationFromCollectionUseCase(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(locationId: String, collectionId: String): Either<String, Location> {
        val current = when (val read = locationsRepository.fetchLocation(locationId)) {
            is Either.Left -> return Either.Left(read.value.message())
            is Either.Right -> read.value
        }
        return when (val saved = locationsRepository.updateLocationCollections(locationId, current.collections - collectionId)) {
            is Either.Left -> Either.Left(saved.value.message())
            is Either.Right -> Either.Right(saved.value)
        }
    }

    private fun ApiResponse.message(): String = when (this) {
        is ApiResponse.IOException -> CANT_REACH_SERVER
        is ApiResponse.HttpError -> "Could not take the place out of this collection."
        is ApiResponse.Forbidden -> "That place belongs to someone else."
        is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
    }
}
