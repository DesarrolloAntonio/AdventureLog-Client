package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserStats

class GetUserStatsUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Either<String, UserStats> {
        return when (val result = userRepository.getUserStats(username)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Cannot load statistics.")
                    is ApiResponse.HttpError -> Either.Left("Failed to load user statistics. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
    }
}
