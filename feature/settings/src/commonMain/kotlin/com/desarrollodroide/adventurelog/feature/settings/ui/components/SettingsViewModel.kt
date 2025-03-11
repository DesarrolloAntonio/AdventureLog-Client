package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.data.SettingsRepository
import com.desarrollodroide.adventurelog.core.data.Tag
import com.desarrollodroide.adventurelog.core.data.ThemeManager
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.settings.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val themeManager: ThemeManager,
    private val imageLoader: ImageLoader,
) : ViewModel() {

    private val _settingsUiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val settingsUiState = _settingsUiState.asStateFlow()

    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    // StateFlows para la configuración de la aplicación desde el repositorio
    val themeMode = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)
    
    val useDynamicColors = settingsRepository.getUseDynamicColors()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
        
    val compactView = settingsRepository.getCompactView()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
        
    // Otras configuraciones
    val makeArchivePublic = MutableStateFlow(false)
    val createEbook = MutableStateFlow(false)
    val autoAddBookmark = MutableStateFlow(false)
    val createArchive = MutableStateFlow(false)
    
    // Información del usuario
    private val _tagToHide = MutableStateFlow<Tag?>(null)
    val tagToHide = _tagToHide.asStateFlow()
    
    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails = _userDetails.asStateFlow()

    private val _serverVersion = MutableStateFlow("")
    private val _serverUrl = MutableStateFlow("")

    init {
        loadSettings()
        updateCacheSize()
    }
    
    fun logout() {
        viewModelScope.launch {
            // Limpiar credenciales y cerrar sesión
            settingsRepository.clearLoginCredentials()
            //_settingsUiState.value = SettingsUiState.Success("Logged out")
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Cargar detalles del usuario desde el repositorio
            val userDetails = settingsRepository.getUserDetails()
            _userDetails.value = userDetails
            
            // Si hay detalles de usuario, extraer información del servidor
            userDetails?.let { user ->
                // Estos son ejemplos, ajusta según la estructura real de tus datos
                _serverVersion.value = user.serverVersion ?: "1.0.0"
                _serverUrl.value = user.serverUrl ?: "localhost"
                
                // También podrías cargar otras configuraciones específicas del usuario
                makeArchivePublic.value = user.preferences?.makeArchivePublic ?: false
                createEbook.value = user.preferences?.createEbook ?: false
                autoAddBookmark.value = user.preferences?.autoAddBookmark ?: false
                createArchive.value = user.preferences?.createArchive ?: false
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

    fun setMakeArchivePublic(isPublic: Boolean) {
        viewModelScope.launch {
            makeArchivePublic.value = isPublic
            // También podrías guardar esto en las preferencias del usuario si es necesario
        }
    }

    fun setCreateEbook(createEbookValue: Boolean) {
        viewModelScope.launch {
            createEbook.value = createEbookValue
            // También podrías guardar esto en las preferencias del usuario si es necesario
        }
    }

    fun setAutoAddBookmark(autoAdd: Boolean) {
        viewModelScope.launch {
            autoAddBookmark.value = autoAdd
            // También podrías guardar esto en las preferencias del usuario si es necesario
        }
    }

    fun setCreateArchive(create: Boolean) {
        viewModelScope.launch {
            createArchive.value = create
            // También podrías guardar esto en las preferencias del usuario si es necesario
        }
    }

    fun getTags() {
        // Implementa la lógica para obtener etiquetas si es necesario
    }

    fun setHideTag(tag: Tag?) {
        _tagToHide.value = tag
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun updateCacheSize() {
        viewModelScope.launch {
            val size = imageLoader.diskCache?.size ?: 0L
            _cacheSize.value = formatByteSize(size)
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    fun clearImageCache() {
        viewModelScope.launch {
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
            updateCacheSize()
        }
    }

    fun getServerUrl(): String = _serverUrl.value

    fun getServerVersion(): String = _serverVersion.value
    
    // Función auxiliar para formatear el tamaño en bytes a una representación legible
    private fun formatByteSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}

// Clase estub para que compile mientras no tengas la definición real
data class Tag(val id: Int, val name: String)
