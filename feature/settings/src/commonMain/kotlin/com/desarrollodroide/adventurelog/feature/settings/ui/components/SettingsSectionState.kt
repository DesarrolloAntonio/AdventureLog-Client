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
     * Represents the feed settings section.
     */
    data class Feed(
        val compactView: Boolean,
    ) : SettingsSectionState()

    /**
     * Represents the defaults settings section.
     */
    data class Defaults(
        val makeArchivePublic: Boolean,
        val createEbook: Boolean,
        val createArchive: Boolean,
        val autoAddBookmark: Boolean
    ) : SettingsSectionState()

    /**
     * Represents the data settings section.
     */
    data class Data(
        val cacheSize: String
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

