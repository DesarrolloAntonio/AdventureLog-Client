package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository

class GenerateDescriptionUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(name: String): Either<String, String> {
        return adventuresRepository.generateDescription(name)
    }
}
