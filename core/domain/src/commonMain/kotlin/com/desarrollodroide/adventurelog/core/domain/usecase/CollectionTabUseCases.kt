package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection

class GetArchivedCollectionsUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(): Either<String, List<UltraSlimCollection>> =
        collectionsRepository.getArchivedCollections().describe("load the archive")
}

class GetSharedCollectionsUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(): Either<String, List<UltraSlimCollection>> =
        collectionsRepository.getSharedCollections().describe("load shared collections")
}

class GetCollectionInvitesUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(): Either<String, List<CollectionInvite>> =
        collectionsRepository.getInvites().describe("load your invitations")
}

class RespondToCollectionInviteUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        collectionId: String,
        accept: Boolean
    ): Either<String, Unit> = collectionsRepository
        .respondToInvite(collectionId, accept)
        .describe(if (accept) "accept this invitation" else "decline this invitation")
}

private fun <T> Either<ApiResponse, T>.describe(action: String): Either<String, T> = when (this) {
    is Either.Left -> Either.Left(
        when (value) {
            is ApiResponse.IOException -> "No internet connection."
            is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
            is ApiResponse.HttpError -> "Could not $action."
        }
    )
    is Either.Right -> Either.Right(value)
}
