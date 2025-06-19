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
        isPublic: Boolean
    ): Either<String, Collection> {
        // TODO: Implement collection creation logic
        return Either.Left("Not implemented yet")
    }
}
