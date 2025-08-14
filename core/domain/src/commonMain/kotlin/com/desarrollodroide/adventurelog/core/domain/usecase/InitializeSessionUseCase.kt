package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork

/**
 * Use case to initialize user session if it exists
 * This handles both checking for existing session and configuring network layer
 */
class InitializeSessionUseCase(
    private val userRepository: UserRepository,
    private val networkDataSource: AdventureLogNetwork
) {

    /**
     * Checks for existing user session and initializes network configuration if found
     * @return UserDetails if session exists and is valid, null otherwise
     */
    suspend operator fun invoke(): UserDetails? {
        return try {
            val existingSession = userRepository.getUserSessionOnce()

            if (existingSession != null) {
                networkDataSource.initializeFromSession(
                    serverUrl = existingSession.serverUrl,
                    sessionToken = existingSession.sessionToken
                )

                existingSession
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
