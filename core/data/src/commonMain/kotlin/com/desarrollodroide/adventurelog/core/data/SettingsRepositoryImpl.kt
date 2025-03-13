package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.LoginCredentials
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Implementation of SettingsRepository using multiplatform-settings
 */
class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    /**
     * JSON serializer with common configuration
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    // Lazy initialization of StateFlows
    private val _themeMode by lazy { MutableStateFlow(getThemeModeFromSettings()) }
    private val _useDynamicColors by lazy { MutableStateFlow(getUseDynamicColorsFromSettings()) }
    private val _compactView by lazy { MutableStateFlow(getCompactViewFromSettings()) }
    
    override suspend fun saveUserDetails(userDetails: UserDetails) {
        try {
            settings.putString(KEY_USER_DETAILS, json.encodeToString(userDetails))
        } catch (e: SerializationException) {
            // Log or handle serialization errors
            println("Error serializing user details: ${e.message}")
        }
    }

    override suspend fun getUserDetails(): UserDetails? {
        return try {
            settings.getStringOrNull(KEY_USER_DETAILS)?.let {
                json.decodeFromString(it)
            }
        } catch (e: SerializationException) {
            // Log or handle deserialization errors
            println("Error deserializing user details: ${e.message}")
            null
        }
    }

    override suspend fun saveLoginCredentials(credentials: LoginCredentials) {
        try {
            if (credentials.rememberCredentials) {
                settings.putString(KEY_CREDENTIALS, json.encodeToString(credentials))
            } else {
                clearLoginCredentials()
            }
        } catch (e: SerializationException) {
            // Log or handle serialization errors
            println("Error serializing login credentials: ${e.message}")
        }
    }

    override suspend fun getLoginCredentials(): LoginCredentials? {
        return try {
            settings.getStringOrNull(KEY_CREDENTIALS)?.let {
                json.decodeFromString(it)
            }
        } catch (e: SerializationException) {
            // Log or handle deserialization errors
            println("Error deserializing login credentials: ${e.message}")
            null
        }
    }

    override suspend fun clearLoginCredentials() {
        settings.remove(KEY_CREDENTIALS)
    }

    override suspend fun clearAll() {
        settings.clear()
        resetStateFlows()
    }
    
    private fun resetStateFlows() {
        _themeMode.value = ThemeMode.AUTO
        _useDynamicColors.value = true
        _compactView.value = false
    }

    override fun getThemeMode(): StateFlow<ThemeMode> = _themeMode
    
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settings.putInt(KEY_THEME_MODE, themeMode.ordinal)
        _themeMode.value = themeMode
    }
    
    private fun getThemeModeFromSettings(): ThemeMode {
        val ordinal = settings.getIntOrNull(KEY_THEME_MODE) ?: ThemeMode.AUTO.ordinal
        return ThemeMode.values().getOrElse(ordinal) { ThemeMode.AUTO }
    }

    override fun getUseDynamicColors(): StateFlow<Boolean> = _useDynamicColors
    
    override suspend fun setUseDynamicColors(useDynamicColors: Boolean) {
        settings.putBoolean(KEY_USE_DYNAMIC_COLORS, useDynamicColors)
        _useDynamicColors.value = useDynamicColors
    }
    
    private fun getUseDynamicColorsFromSettings(): Boolean {
        return settings.getBoolean(KEY_USE_DYNAMIC_COLORS, true)
    }
    
    override fun getCompactView(): StateFlow<Boolean> = _compactView
    
    override suspend fun setCompactView(compactView: Boolean) {
        settings.putBoolean(KEY_COMPACT_VIEW, compactView)
        _compactView.value = compactView
    }
    
    private fun getCompactViewFromSettings(): Boolean {
        return settings.getBoolean(KEY_COMPACT_VIEW, false)
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
    
    /**
     * Extension function to get an Int value or null if not present
     */
    private fun Settings.getIntOrNull(key: String): Int? {
        return try {
            getInt(key, 0)
        } catch (e: NoSuchElementException) {
            null
        }
    }

    companion object {
        private const val KEY_USER_DETAILS = "user_details"
        private const val KEY_CREDENTIALS = "login_credentials"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"
        private const val KEY_COMPACT_VIEW = "compact_view"
    }
}