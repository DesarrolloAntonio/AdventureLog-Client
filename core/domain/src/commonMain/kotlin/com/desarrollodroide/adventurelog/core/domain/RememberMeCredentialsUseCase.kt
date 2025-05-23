package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
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
     * Saves remember me credentials
     */
    suspend fun save(url: String, username: String, password: String) {
        userRepository.saveRememberMeCredentials(url, username, password)
    }

    /**
     * Clears saved remember me credentials
     */
    suspend fun clear() {
        userRepository.clearRememberMeCredentials()
    }
}
