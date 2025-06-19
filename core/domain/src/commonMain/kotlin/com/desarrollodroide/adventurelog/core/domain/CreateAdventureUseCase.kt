package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Visit

class CreateAdventureUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        categoryId: String?,
        rating: Double,
        link: String,
        location: String,
        latitude: String,
        longitude: String,
        isPublic: Boolean,
        tags: List<String>,
        visitDates: Visit? = null
    ): Either<String, Adventure> {
        // Validate required fields
        if (name.isBlank()) {
            return Either.Left("Adventure name is required")
        }
        
        if (categoryId.isNullOrBlank()) {
            return Either.Left("Category is required")
        }
        
        // Parse coordinates
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        
        // Create the adventure
        return adventuresRepository.createAdventure(
            name = name,
            description = description,
            categoryId = categoryId,
            rating = rating,
            link = link,
            location = location,
            latitude = lat,
            longitude = lon,
            isPublic = isPublic,
            visitDates = visitDates
        )
    }
}
