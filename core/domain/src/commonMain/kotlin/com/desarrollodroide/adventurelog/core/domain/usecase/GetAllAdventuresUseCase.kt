package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure

class GetAllAdventuresUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(): Either<String, List<Adventure>> {
        return when (val result = adventuresRepository.getAllAdventures()) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Map requires network access.")
                    is ApiResponse.HttpError -> Either.Left("Failed to load adventures. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
