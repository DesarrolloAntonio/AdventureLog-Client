package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection

class GetCollectionDetailUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(collectionId: String): Either<String, Collection> =
        when (val result = collectionsRepository.getCollection(collectionId)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error getting collection details, try again later")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
}
