package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.model.LoginCredentials
import com.desarrollodroide.adventurelog.core.model.UserDetails

interface SettingsRepository {
    suspend fun saveUserDetails(userDetails: UserDetails)
    suspend fun getUserDetails(): UserDetails?
    suspend fun saveLoginCredentials(credentials: LoginCredentials)
    suspend fun getLoginCredentials(): LoginCredentials?
    suspend fun clearLoginCredentials()
    suspend fun clearAll()
}