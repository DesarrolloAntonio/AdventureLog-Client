package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * Use case to save user session data
 * Used when user successfully logs in with "Remember me" checked
 */
class SaveSessionUseCase(
    private val userRepository: UserRepository
) {

    /**
     * Saves the user session persistently
     * @param userDetails User details to save
     */
    suspend operator fun invoke(userDetails: UserDetails) {
        userRepository.saveUserSession(userDetails)
    }
}
