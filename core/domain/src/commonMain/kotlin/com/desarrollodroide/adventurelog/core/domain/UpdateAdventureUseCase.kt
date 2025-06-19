package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure

class UpdateAdventureUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(
        adventureId: String,
        name: String,
        description: String,
        categoryId: String?,
        rating: Double,
        link: String,
        location: String,
        latitude: String,
        longitude: String,
        isPublic: Boolean,
        tags: List<String>
    ): Either<String, Adventure> {
        // TODO: Implement adventure update logic
        return Either.Left("Not implemented yet")
    }
}
