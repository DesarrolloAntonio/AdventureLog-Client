package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.desarrollodroide.adventurelog.feature.settings.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SettingsViewModel(
    // Add Use cases classes here
): ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()


}