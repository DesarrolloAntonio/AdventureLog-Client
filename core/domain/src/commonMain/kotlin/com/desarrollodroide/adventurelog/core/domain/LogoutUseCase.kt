package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource

/**
 * Use case to handle user logout
 * Clears user session but preserves remember me credentials
 */
class LogoutUseCase(
    private val userRepository: UserRepository,
    private val networkDataSource: AdventureLogNetworkDataSource
) {

    /**
     * Performs logout operation
     * - Clears user session from local storage (preserves remember me credentials)
     * - Resets network configuration
     */
    suspend operator fun invoke() {
        try {
            userRepository.clearUserSession()
            networkDataSource.clearSession()
        } catch (e: Exception) {
            println("Warning: Error during logout, but local data was cleared: ${e.message}")
        }
    }
}
