package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource

/**
 * Use case to handle user logout
 * This clears all user session data and resets network configuration
 */
class LogoutUseCase(
    private val userRepository: UserRepository,
    private val networkDataSource: AdventureLogNetworkDataSource
) {

    /**
     * Performs logout operation
     * - Clears user session from local storage
     * - Resets network configuration
     * - Optionally calls server to invalidate token (if needed)
     */
    suspend operator fun invoke() {
        try {
            // Clear all user data (session + remember me credentials)
            userRepository.clearAllUserData()

            // Reset network configuration to clear any cached tokens/headers
            networkDataSource.clearSession()

            // TODO: If server supports logout endpoint, call it here to invalidate token
            // networkDataSource.logout()

        } catch (e: Exception) {
            // Even if there's an error, we still want to clear local data
            // Log the error but don't throw it
            println("Warning: Error during logout, but local data was cleared: ${e.message}")
        }
    }
}
