package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.model.LoginCredentials
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun saveUserDetails(userDetails: UserDetails) {
        settings.putString(KEY_USER_DETAILS, json.encodeToString(userDetails))
    }

    override suspend fun getUserDetails(): UserDetails? {
        return settings.getStringOrNull(KEY_USER_DETAILS)?.let {
            json.decodeFromString(it)
        }
    }

    override suspend fun saveLoginCredentials(credentials: LoginCredentials) {
        if (credentials.rememberCredentials) {
            settings.putString(KEY_CREDENTIALS, json.encodeToString(credentials))
        } else {
            clearLoginCredentials()
        }
    }

    override suspend fun getLoginCredentials(): LoginCredentials? {
        return settings.getStringOrNull(KEY_CREDENTIALS)?.let {
            json.decodeFromString(it)
        }
    }

    override suspend fun clearLoginCredentials() {
        settings.remove(KEY_CREDENTIALS)
    }

    override suspend fun clearAll() {
        settings.clear()
    }

    companion object {
        private const val KEY_USER_DETAILS = "user_details"
        private const val KEY_CREDENTIALS = "login_credentials"
    }
}