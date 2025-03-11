package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Tag
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
//    private val sendLogoutUseCase: SendLogoutUseCase,
//    private val bookmarksRepository: BookmarksRepository,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val themeManager: ThemeManager,
    private val getTagsUseCase: GetTagsUseCase,
    private val imageLoader: ImageLoader,
    ) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(UiState<String>(isLoading = false))
    val settingsUiState = _settingsUiState.asStateFlow()

    private val _tagsState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsState = _tagsState.asStateFlow()

    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    val useDynamicColors = MutableStateFlow(false)
    val themeMode = MutableStateFlow(ThemeMode.AUTO)
    private var _token = ""
    private var _serverVersion = ""
    private var _serverUrl: String = ""

    val compactView: StateFlow<Boolean> = settingsPreferenceDataSource.compactViewFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setCompactView(isCompact: Boolean) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setCompactView(isCompact)
        }
    }

    init {
        loadSettings()
        observeDefaultsSettings()
        updateCacheSize()
    }
    fun logout() {
        viewModelScope.launch {
            sendLogoutUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                xSession = settingsPreferenceDataSource.getSession()
            ).collect { result ->
                when (result) {
                    is Result.Error -> {
                        _settingsUiState.error(errorMessage = result.error?.throwable?.message?: "")
                    }
                    is Result.Loading -> {
                        _settingsUiState.isLoading(true)
                    }
                    is Result.Success -> {
                        _settingsUiState.success(result.data)
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            useDynamicColors.value = settingsPreferenceDataSource.getUseDynamicColors()
            themeMode.value = settingsPreferenceDataSource.getThemeMode()
            _token = settingsPreferenceDataSource.getToken()
            _serverVersion = settingsPreferenceDataSource.getServerVersion()
            _serverUrl = settingsPreferenceDataSource.getUrl()
        }
    }

    fun getTags() {
      viewModelScope.launch {
            getTagsUseCase.invoke(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                token = _token,
            )
                .distinctUntilChanged()
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("FeedViewModel", "Error getting tags: ${result.error?.message}")
                        }
                        is Result.Loading -> {
                            Log.v("FeedViewModel", "Loading, updating tags from cache...")
                            _tagsState.isLoading(true)
                        }
                        is Result.Success -> {
                            Log.v("FeedViewModel", "Tags loaded successfully.")
                            _tagsState.success(result.data)
                        }
                    }
                }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun updateCacheSize() {
        viewModelScope.launch {
            val size = imageLoader.diskCache?.size ?: 0L
            _cacheSize.value = size.bytesToDisplaySize()
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

    private fun observeDefaultsSettings() {
        viewModelScope.launch {
            useDynamicColors.collect { newValue ->
                settingsPreferenceDataSource.setUseDynamicColors(newValue)
                themeManager.useDynamicColors.value = newValue
            }
        }
        viewModelScope.launch {
            themeMode.collect { newValue ->
                settingsPreferenceDataSource.setTheme(newValue)
                themeManager.themeMode.value = newValue
            }
        }
    }

    fun getServerUrl(): String = _serverUrl

    fun getServerVersion(): String = _serverVersion

}

