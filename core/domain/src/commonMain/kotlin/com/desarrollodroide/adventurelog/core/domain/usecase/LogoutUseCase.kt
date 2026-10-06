package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.domain.repository.AccountDataCache
import com.desarrollodroide.adventurelog.core.domain.repository.LocalAccountCopies
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("LogoutUseCase")

/**
 * Use case to handle user logout
 * Clears user session but preserves remember me credentials
 */
class LogoutUseCase(
    private val userRepository: UserRepository,
    private val networkDataSource: AdventureLogNetwork,
    private val localAccountCopies: LocalAccountCopies = LocalAccountCopies.None,
    private val accountCaches: List<AccountDataCache> = emptyList()
) {

    /**
     * Performs logout operation
     * - Ends the session on the server (in the background)
     * - Deletes the account's files kept on the device
     * - Clears user session from local storage (preserves remember me credentials)
     * - Resets network configuration
     */
    suspend operator fun invoke() {
        var userRepositoryError: Exception? = null
        var networkDataSourceError: Exception? = null

        // First, while the token is still in hand. It returns at once - the request goes out in the
        // background - so an unreachable server never holds up signing out.
        try {
            networkDataSource.endServerSession()
        } catch (e: Exception) {
            logger.e { "Could not ask the server to end the session: ${e.message}" }
        }

        try {
            localAccountCopies.delete()
        } catch (e: Exception) {
            logger.e { "Could not delete the account's files on this device: ${e.message}" }
        }

        accountCaches.forEach { cache ->
            try {
                cache.clearAccountData()
            } catch (e: Exception) {
                logger.e { "Could not empty an account cache: ${e.message}" }
            }
        }

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
            logger.e { "Warning: Error during logout, but local data was cleared: ${it.message}" }
        }
        networkDataSourceError?.let {
            logger.e { "Warning: Error during logout, but local data was cleared: ${it.message}" }
        }
    }
}