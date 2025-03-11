package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.LoginCredentials
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    suspend fun saveUserDetails(userDetails: UserDetails)
    suspend fun getUserDetails(): UserDetails?

    suspend fun saveLoginCredentials(credentials: LoginCredentials)
    suspend fun getLoginCredentials(): LoginCredentials?
    suspend fun clearLoginCredentials()
    suspend fun clearAll()
    
    fun getThemeMode(): StateFlow<ThemeMode>
    suspend fun setThemeMode(themeMode: ThemeMode)
    
    fun getUseDynamicColors(): StateFlow<Boolean>
    suspend fun setUseDynamicColors(useDynamicColors: Boolean)
    
    fun getCompactView(): StateFlow<Boolean>
    suspend fun setCompactView(compactView: Boolean)
}