package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
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
     * Publishes the session for the current run without writing it to disk.
     *
     * Screens observe [getUserSession] to know who is logged in, so this must happen on every
     * successful login. Only persistence across app restarts is conditional on "Remember Me".
     *
     * @param userDetails Complete user details for the active session
     */
    fun setActiveSession(userDetails: UserDetails)
    
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
     * The active session read synchronously, for callers that cannot suspend.
     *
     * Ktor's `defaultRequest` builder is one such caller: it has to attach credentials while the
     * request is being built. Reading the session there - instead of having the UI push a token in -
     * removes the race where the first image requests go out unauthenticated.
     */
    val activeSession: UserDetails?
    
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
    
    /**
     * Get user statistics
     * @param username Username to get stats for
     * @return Either with ApiResponse error or UserStats with adventure counts, visited places, etc.
     */
    suspend fun getUserStats(username: String): Either<ApiResponse, UserStats>
    
    /**
     * Gets user stats as a Flow (cached)
     * @return Flow of UserStats or null if not loaded
     */
    fun getUserStatsFlow(): Flow<UserStats?>
}