package com.desarrollodroide.adventurelog.core.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val themeManager: ThemeManager,
) : ViewModel() {

    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    val themeMode = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)

    val useDynamicColors = settingsRepository.getUseDynamicColors()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val compactView = settingsRepository.getCompactView()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val makeArchivePublic = MutableStateFlow(false)
    val createEbook = MutableStateFlow(false)
    val autoAddBookmark = MutableStateFlow(false)
    val createArchive = MutableStateFlow(false)


    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails = _userDetails.asStateFlow()

    private val _serverVersion = MutableStateFlow("1.0.0")
    private val _serverUrl = MutableStateFlow("localhost")

    init {
        loadSettings()
    }

    fun logout() {
        viewModelScope.launch {
            settingsRepository.clearLoginCredentials()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val userDetails = settingsRepository.getUserDetails()
            _userDetails.value = userDetails
            val credentials = settingsRepository.getLoginCredentials()
            credentials?.let {
                _serverUrl.value = it.serverUrl
            }
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

    fun getServerUrl(): String = _serverUrl.value

    fun getServerVersion(): String = _serverVersion.value

}

