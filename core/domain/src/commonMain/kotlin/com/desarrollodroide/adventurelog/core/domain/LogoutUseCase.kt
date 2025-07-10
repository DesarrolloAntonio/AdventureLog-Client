package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource

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
        var userRepositoryError: Exception? = null
        var networkDataSourceError: Exception? = null

        // Try to clear user session
        try {
            userRepository.clearUserSession()
        } catch (e: Exception) {
            userRepositoryError = e
        }

        // Try to clear network session regardless of user repository result
        try {
            networkDataSource.clearSession()
        } catch (e: Exception) {
            networkDataSourceError = e
        }

        // Log warnings if any errors occurred
        userRepositoryError?.let {
            println("Warning: Error during logout, but local data was cleared: ${it.message}")
        }
        networkDataSourceError?.let {
            println("Warning: Error during logout, but local data was cleared: ${it.message}")
        }
    }
}