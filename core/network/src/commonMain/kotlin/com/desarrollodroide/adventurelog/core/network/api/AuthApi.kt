package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO

interface AuthApi {
    /**
     * Send login request and return user details
     */
    suspend fun sendLogin(url: String, username: String, password: String): UserDetailsDTO

    /**
     * Initialize network client with server URL and tokens from existing session
     */
    fun initializeFromSession(serverUrl: String, sessionToken: String?)

    /**
     * Clear session data from network client (tokens, base URL)
     * Used during logout to reset network state
     */
    fun clearSession()
    
    /**
     * Get the current session token
     */
    fun getSessionToken(): String?
    
    /**
     * Get the current base URL
     */
    fun getBaseUrl(): String?
}
