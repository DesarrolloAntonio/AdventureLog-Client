package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home {
    const val route = "home"
}

@Serializable
internal data object HomeScreen {
    const val route = "home_screen"
}

@Serializable
internal data object AdventuresScreen {
    const val route = "adventures_screen"
}

@Serializable
internal data object CollectionsScreen {
    const val route = "collections_screen"
}

@Serializable
internal data object TravelScreen {
    const val route = "travel_screen"
}

@Serializable
internal data object MapScreen {
    const val route = "map_screen"
}

@Serializable
internal data object CalendarScreen {
    const val route = "calendar_screen"
}

@Serializable
internal data object SettingsScreen {
    const val route = "settings_screen"
}