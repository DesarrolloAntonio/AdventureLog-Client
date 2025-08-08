package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.data.SettingsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val logoutUseCase: LogoutUseCase
//    private val imageLoader: ImageLoader
) : ViewModel() {

    private val _userDetails = MutableStateFlow<UserDetails?>(null)

    val themeMode = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)

    val useDynamicColors = settingsRepository.getUseDynamicColors()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val compactView = settingsRepository.getCompactView()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)


    private val _serverUrl = MutableStateFlow("")

    init {
        loadSettings()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                println("Logout completed successfully")
                // Note: Navigation back to login will be handled by the main navigation
                // observing the user session state
            } catch (e: Exception) {
                println("Error during logout: ${e.message}")
                // Even if logout fails, local data should be cleared by the UseCase
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
//            try {
//                // Cargar detalles del usuario desde el repositorio
//                val userDetails = settingsRepository.getUserDetails()
//                _userDetails.value = userDetails
//
//                // Si hay detalles de usuario, extraer información del servidor
//                userDetails?.let { user ->
//                    _serverVersion.value = user.serverVersion ?: "1.0.0"
//                    _serverUrl.value = user.serverUrl ?: "localhost"
//                }
//
//                // Transición al estado Success
//                _uiState.value = SettingsUiState.Success
//            } catch (e: Exception) {
//                _uiState.value = SettingsUiState.Error(e.message ?: "Failed to load settings")
//            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setUseDynamicColors(useDynamic: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColors(useDynamic)
        }
    }

    fun setCompactView(isCompact: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCompactView(isCompact)
        }
    }

    fun getServerUrl(): String {
        return _serverUrl.value
    }
}
