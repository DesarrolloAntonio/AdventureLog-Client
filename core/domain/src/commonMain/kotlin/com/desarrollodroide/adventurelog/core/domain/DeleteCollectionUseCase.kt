package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository

class DeleteCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(collectionId: String): Either<String, Unit> {
        return collectionsRepository.deleteCollection(collectionId)
    }
}
