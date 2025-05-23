package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository

/**
 * Use case to clear user session data
 * Used when user unchecks "Remember me" or logs out
 */
class ClearSessionUseCase(
    private val userRepository: UserRepository
) {

    /**
     * Clears the saved user session
     */
    suspend operator fun invoke() {
        userRepository.clearUserSession()
    }
}
