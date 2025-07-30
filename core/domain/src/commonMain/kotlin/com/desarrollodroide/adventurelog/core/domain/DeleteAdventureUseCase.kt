package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository

class DeleteAdventureUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(adventureId: String): Either<String, Unit> {
        return adventuresRepository.deleteAdventure(adventureId)
    }
}
