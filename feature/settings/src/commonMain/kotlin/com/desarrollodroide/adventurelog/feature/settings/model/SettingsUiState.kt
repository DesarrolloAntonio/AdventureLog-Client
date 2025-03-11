package com.desarrollodroide.adventurelog.feature.settings.model

import co.touchlab.kermit.Tag

sealed interface SettingsUiState {
    data class Error(val message: String) : SettingsUiState
    data object Loading : SettingsUiState
    data object Success : SettingsUiState
}