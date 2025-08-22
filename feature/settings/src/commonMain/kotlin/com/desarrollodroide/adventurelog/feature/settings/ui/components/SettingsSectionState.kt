package com.desarrollodroide.adventurelog.feature.settings.ui.components

import com.desarrollodroide.adventurelog.core.constants.ThemeMode

sealed class SettingsSectionState {

    /**
     * Represents the visual settings section.
     */
    data class Visual(
        val themeMode: ThemeMode,
        val useDynamicColors: Boolean
    ) : SettingsSectionState()

    /**
     * Represents an error state within the settings.
     */
    data class Error(val message: String) : SettingsSectionState()

    /**
     * Represents a loading state within the settings.
     */
    object Loading : SettingsSectionState()
}

