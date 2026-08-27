package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("InitializeSessionUseCase")

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
                    serverUrl = existingSession.serverUrl ?: "",
                    sessionToken = existingSession.sessionToken
                )
                
                try {
                    // This call doubles as the token check and as the only source of the user's
                    // real name: allauth's headless login response carries just id/username/email,
                    // so without merging this in the greeting falls back to the username forever.
                    val fresh = networkDataSource.getUserDetails()
                        .toDomainModel(serverUrl = existingSession.serverUrl ?: "")
                        .copy(sessionToken = existingSession.sessionToken)

                    // Publish without persisting: everything observing the session (the greeting,
                    // the drawer) needs the refreshed profile, but writing it back would store a
                    // session the user may have chosen not to keep.
                    userRepository.setActiveSession(fresh)
                    fresh
                } catch (e: Exception) {
                    logger.e { "❌ Token validation failed: ${e.message}" }
                    logger.d { "🧹 Clearing corrupted session" }
                    userRepository.clearUserSession()
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            logger.e { "⚠️ Session initialization failed: ${e.message}" }
            null
        }
    }
}
