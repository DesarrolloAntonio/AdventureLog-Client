package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection

class UpdateCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String? = null,
        endDate: String? = null,
        link: String? = null
    ): Either<String, Collection> {
        return collectionsRepository.updateCollection(
            collectionId = collectionId,
            name = name,
            description = description,
            isPublic = isPublic,
            startDate = startDate,
            endDate = endDate,
            link = link
        )
    }
}
