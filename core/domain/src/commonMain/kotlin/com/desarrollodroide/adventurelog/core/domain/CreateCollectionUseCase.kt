package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection

class CreateCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String? = null,
        endDate: String? = null
    ): Either<String, Collection> {
        // Validate required fields
        if (name.isBlank()) {
            return Either.Left("Collection name is required")
        }
        
        // Create the collection
        return collectionsRepository.createCollection(
            name = name,
            description = description,
            isPublic = isPublic,
            startDate = startDate,
            endDate = endDate
        )
    }
}
