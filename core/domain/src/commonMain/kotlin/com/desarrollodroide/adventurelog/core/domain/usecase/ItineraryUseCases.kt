package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind

/**
 * Lay out a whole trip from the dates already on its records.
 *
 * The server refuses on a collection that already has an itinerary, so the offer belongs on an
 * empty one only - which is also the only place it is any use.
 */
class AutoGenerateItineraryUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(collectionId: String): Either<String, List<ItineraryEntry>> =
        when (val result = collectionsRepository.autoGenerateItinerary(collectionId)) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> Either.Left(result.value.itineraryMessage())
        }
}

class AddItineraryEntryUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    /**
     * [order] puts the new entry after everything already on that day. The caller knows how many
     * that is; the server does not renumber.
     */
    suspend operator fun invoke(
        collectionId: String,
        kind: ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): Either<String, ItineraryEntry> =
        when (
            val result =
                collectionsRepository.addItineraryEntry(collectionId, kind, itemId, date, order)
        ) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> Either.Left(result.value.itineraryMessage())
        }
}

class DeleteItineraryEntryUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(entryId: String): Either<String, Unit> =
        when (val result = collectionsRepository.deleteItineraryEntry(entryId)) {
            is Either.Right -> Either.Right(Unit)
            is Either.Left -> Either.Left(result.value.itineraryMessage())
        }
}

private fun ApiResponse.itineraryMessage(): String = when (this) {
    is ApiResponse.IOException -> "Network unavailable"
    is ApiResponse.HttpError -> "Could not change the itinerary, try again later"
    is ApiResponse.InvalidCredentials -> "Session expired, please log in again"
}
