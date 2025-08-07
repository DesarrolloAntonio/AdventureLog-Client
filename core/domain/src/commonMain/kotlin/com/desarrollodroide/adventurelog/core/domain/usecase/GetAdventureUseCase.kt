package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure

class GetAdventureUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(adventureId: String): Either<String, Adventure> {
        return when (val result = adventuresRepository.getAdventure(adventureId)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.HttpError -> Either.Left("Network error. Please check your connection.")
                    is ApiResponse.IOException -> Either.Left("Connection error. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Authentication error. Please login again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
