package com.desarrollodroide.adventurelog.core.network

import com.desarrollodroide.adventurelog.core.network.model.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.UserDetailsDTO

interface AdventureLogNetworkDataSource {

    /**
     * Get paginated list of adventures
     */
    suspend fun getAdventures(page: Int, pageSize: Int): List<AdventureDTO>

    /**
     * Get adventure details by ID
     */
    suspend fun getAdventureDetail(objectId: String): AdventureDTO

    /**
     * Send login request and return user details
     */
    suspend fun sendLogin(url: String, username: String, password: String): UserDetailsDTO

    /**
     * Get current user details
     */
    suspend fun getUserDetails(): UserDetailsDTO

    /**
     * Initialize network client with server URL and tokens from existing session
     */
    fun initializeFromSession(serverUrl: String, sessionToken: String?)

    /**
     * Clear session data from network client (tokens, base URL)
     * Used during logout to reset network state
     */
    fun clearSession()
}