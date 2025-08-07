package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository

class GenerateDescriptionUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(name: String): Either<String, String> {
        return when (val result = adventuresRepository.generateDescription(name)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Cannot generate description.")
                    is ApiResponse.HttpError -> Either.Left("No Wikipedia description available for \"$name\"")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
