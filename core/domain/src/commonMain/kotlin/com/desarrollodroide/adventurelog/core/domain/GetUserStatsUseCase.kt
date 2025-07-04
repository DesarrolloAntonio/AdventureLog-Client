package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserStats

class GetUserStatsUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Either<String, UserStats> {
        return try {
            val stats = userRepository.getUserStats(username)
            Either.Right(stats)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Error getting user statistics")
        }
    }
}
