package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of UserRepository using MultiPlatform-Settings
 */
class UserRepositoryImpl(
    private val settings: Settings
) : UserRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // StateFlows to observe changes
    private val rememberMeFlow = MutableStateFlow<Account?>(null)
    private val userSessionFlow = MutableStateFlow<UserDetails?>(null)
    
    // Keys for storing user data
    private object Keys {
        // Remember Me keys
        const val REMEMBER_USER_ID = "remember_user_id"
        const val REMEMBER_USERNAME = "remember_username"
        const val REMEMBER_PASSWORD = "remember_password"
        const val REMEMBER_URL = "remember_url"
        
        // User Session key (stored as JSON)
        const val USER_SESSION = "user_session"
    }
    
    init {
        // Initialize state flows with stored data
        loadInitialData()
    }
    
    private fun loadInitialData() {
        // Load remember me credentials if they exist
        if (hasKey(Keys.REMEMBER_USERNAME)) {
            rememberMeFlow.value = Account(
                id = settings.getInt(Keys.REMEMBER_USER_ID, -1),
                userName = settings.getString(Keys.REMEMBER_USERNAME, ""),
                password = settings.getString(Keys.REMEMBER_PASSWORD, ""),
                serverUrl = settings.getString(Keys.REMEMBER_URL, "")
            )
        }
        
        // Load user session if it exists
        try {
            val userSessionJson = settings.getStringOrNull(Keys.USER_SESSION)
            if (userSessionJson != null) {
                userSessionFlow.value = json.decodeFromString(userSessionJson)
            }
        } catch (e: Exception) {
            println("Error deserializing user session: ${e.message}")
        }
    }
    
    /**
     * Helper method to check if a key exists in settings
     */
    private fun hasKey(key: String): Boolean {
        return try {
            settings.getString(key, "")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extension function to get a String value or null if not present
     */
    private fun Settings.getStringOrNull(key: String): String? {
        return try {
            getString(key, "")
        } catch (e: NoSuchElementException) {
            null
        }
    }
    
    override suspend fun saveRememberMeCredentials(url: String, username: String, password: String) {
        // Generate an ID if needed (or use existing)
        val id = settings.getInt(Keys.REMEMBER_USER_ID, 1)
        
        // Save to settings
        settings.putInt(Keys.REMEMBER_USER_ID, id)
        settings.putString(Keys.REMEMBER_USERNAME, username)
        settings.putString(Keys.REMEMBER_PASSWORD, password) 
        settings.putString(Keys.REMEMBER_URL, url)
        
        // Update flow
        rememberMeFlow.value = Account(
            id = id,
            userName = username,
            password = password,
            serverUrl = url
        )
    }
    
    override fun getRememberMeCredentials(): Flow<Account?> {
        return rememberMeFlow.asStateFlow()
    }
    
    override suspend fun clearRememberMeCredentials() {
        settings.remove(Keys.REMEMBER_USER_ID)
        settings.remove(Keys.REMEMBER_USERNAME)
        settings.remove(Keys.REMEMBER_PASSWORD)
        settings.remove(Keys.REMEMBER_URL)
        
        rememberMeFlow.value = null
    }
    
    override suspend fun saveUserSession(userDetails: UserDetails) {
        try {
            // Serialize the entire UserDetails object to JSON
            val userSessionJson = json.encodeToString(userDetails)
            settings.putString(Keys.USER_SESSION, userSessionJson)
            
            // Update flow
            userSessionFlow.value = userDetails
        } catch (e: Exception) {
            println("Error saving user session: ${e.message}")
        }
    }
    
    override fun getUserSession(): Flow<UserDetails?> {
        return userSessionFlow.asStateFlow()
    }
    
    override suspend fun getUserSessionOnce(): UserDetails? {
        return userSessionFlow.value
    }
    
    override suspend fun clearUserSession() {
        settings.remove(Keys.USER_SESSION)
        userSessionFlow.value = null
    }
    
    override fun isLoggedIn(): Flow<Boolean> {
        return userSessionFlow.map { it != null }
    }
    
    override suspend fun clearAllUserData() {
        clearRememberMeCredentials()
        clearUserSession()
    }
}