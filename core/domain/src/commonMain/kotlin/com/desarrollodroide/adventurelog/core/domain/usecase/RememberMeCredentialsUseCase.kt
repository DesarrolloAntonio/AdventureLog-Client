package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * Use case to handle remember me credentials operations
 */
class RememberMeCredentialsUseCase(
    private val userRepository: UserRepository
) {

    /**
     * Gets saved remember me credentials as a flow
     */
    fun get(): Flow<Account?> = userRepository.getRememberMeCredentials()

    /**
     * Saves remember me credentials: the server and the username, never the password
     */
    suspend fun save(url: String, username: String) {
        userRepository.saveRememberMeCredentials(url, username)
    }

    /**
     * Clears saved remember me credentials
     */
    suspend fun clear() {
        userRepository.clearRememberMeCredentials()
    }
}
