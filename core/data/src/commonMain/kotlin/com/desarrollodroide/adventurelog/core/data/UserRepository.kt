package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user authentication and credentials
 */
interface UserRepository {
    
    /**
     * Saves credentials when user checks "Remember Me" in login
     * @param url Server URL
     * @param username User's username
     * @param password User's password
     */
    suspend fun saveRememberMeCredentials(
        url: String,
        username: String,
        password: String
    )
    
    /**
     * Retrieves saved "Remember Me" credentials
     * @return Flow of Account with credentials or null if none exists
     */
    fun getRememberMeCredentials(): Flow<Account?>
    
    /**
     * Clears saved "Remember Me" credentials
     */
    suspend fun clearRememberMeCredentials()
    
    /**
     * Saves active user session data for auto-login
     * @param userDetails Complete user details to save
     */
    suspend fun saveUserSession(userDetails: UserDetails)
    
    /**
     * Gets current user session as a Flow
     * @return Flow of UserDetails or null if not logged in
     */
    fun getUserSession(): Flow<UserDetails?>
    
    /**
     * Gets current user session once (non-Flow)
     * @return UserDetails or null if not logged in
     */
    suspend fun getUserSessionOnce(): UserDetails?
    
    /**
     * Clears the current user session (logout)
     */
    suspend fun clearUserSession()
    
    /**
     * Checks if a user is currently logged in
     * @return Flow of login status (true if logged in)
     */
    fun isLoggedIn(): Flow<Boolean>
    
    /**
     * Clears all user data (both session and remember me)
     */
    suspend fun clearAllUserData()
}