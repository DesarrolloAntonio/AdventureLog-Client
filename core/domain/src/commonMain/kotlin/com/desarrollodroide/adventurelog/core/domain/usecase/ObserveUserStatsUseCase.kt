package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

class ObserveUserStatsUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(username: String): Flow<UserStats> = flow {
        // First emit fresh data from network
        val result = userRepository.getUserStats(username)
        
        when (result) {
            is Either.Left -> {
                // If initial load fails, emit default stats
                emit(UserStats())
            }
            is Either.Right -> {
                emit(result.value)
            }
        }
        
        // Then observe cached changes
        userRepository.getUserStatsFlow()
            .filterNotNull()
            .collect { emit(it) }
    }
}
