package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.CollectionExport

class DuplicateCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(collectionId: String): Either<String, Collection> =
        collectionsRepository.duplicateCollection(collectionId).mapError("duplicate this collection")
}

class ArchiveCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        collectionId: String,
        archived: Boolean
    ): Either<String, Collection> = collectionsRepository
        .setArchived(collectionId, archived)
        .mapError(if (archived) "archive this collection" else "restore this collection")
}

/**
 * Builds one of the files the server renders for a collection. Fetched rather than drawn locally
 * so a card or an itinerary produced on the phone matches the one produced on the web.
 */
class ExportCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        collectionId: String,
        what: CollectionExport
    ): Either<String, ByteArray> = collectionsRepository
        .exportCollection(collectionId, what)
        .mapError(
            when (what) {
                CollectionExport.SHARE_CARD -> "build a share image"
                CollectionExport.PDF -> "build the itinerary PDF"
                CollectionExport.ZIP -> "build the export"
            }
        )
}

private fun <T> Either<ApiResponse, T>.mapError(action: String): Either<String, T> = when (this) {
    is Either.Left -> Either.Left(
        when (value) {
            is ApiResponse.IOException -> "No internet connection."
            is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
            is ApiResponse.HttpError -> "Could not $action."
        }
    )
    is Either.Right -> Either.Right(value)
}
