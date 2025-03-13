package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.LoginCredentials
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing application settings and preferences.
 * This repository handles app theme, UI preferences, and other settings.
 */
interface SettingsRepository {

    /**
     * Saves user details to persistent storage
     * @param userDetails The user details to save
     */
    suspend fun saveUserDetails(userDetails: UserDetails)
    
    /**
     * Retrieves user details from persistent storage
     * @return The stored user details, or null if none exist
     */
    suspend fun getUserDetails(): UserDetails?

    /**
     * Saves login credentials to persistent storage
     * @param credentials The credentials to save
     */
    suspend fun saveLoginCredentials(credentials: LoginCredentials)
    
    /**
     * Retrieves login credentials from persistent storage
     * @return The stored credentials, or null if none exist
     */
    suspend fun getLoginCredentials(): LoginCredentials?
    
    /**
     * Clears saved login credentials
     */
    suspend fun clearLoginCredentials()
    
    /**
     * Clears all settings and preferences
     */
    suspend fun clearAll()
    
    /**
     * Gets the current theme mode as an observable StateFlow
     * @return StateFlow of current ThemeMode
     */
    fun getThemeMode(): StateFlow<ThemeMode>
    
    /**
     * Sets the application theme mode
     * @param themeMode The theme mode to apply
     */
    suspend fun setThemeMode(themeMode: ThemeMode)
    
    /**
     * Gets whether dynamic colors are enabled as an observable StateFlow
     * @return StateFlow of the dynamic colors setting
     */
    fun getUseDynamicColors(): StateFlow<Boolean>
    
    /**
     * Sets whether to use dynamic colors
     * @param useDynamicColors True to enable dynamic colors, false otherwise
     */
    suspend fun setUseDynamicColors(useDynamicColors: Boolean)
    
    /**
     * Gets whether compact view is enabled as an observable StateFlow
     * @return StateFlow of the compact view setting
     */
    fun getCompactView(): StateFlow<Boolean>
    
    /**
     * Sets whether to use compact view
     * @param compactView True to enable compact view, false otherwise
     */
    suspend fun setCompactView(compactView: Boolean)
}