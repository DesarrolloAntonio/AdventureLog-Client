package com.desarrollodroide.adventurelog.feature.settings.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Settings {
    const val route = "settings"
}

@Serializable
internal data object SettingsScreen {
    const val route = "settings_screen"
}