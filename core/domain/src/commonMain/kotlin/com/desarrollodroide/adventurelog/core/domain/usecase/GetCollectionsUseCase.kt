package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection

class GetCollectionsUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int): Either<String, List<Collection>> =
        when (val result = collectionsRepository.getCollections(page, pageSize)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error getting collections, try again later")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
}
