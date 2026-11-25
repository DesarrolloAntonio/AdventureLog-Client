package com.desarrollodroide.adventurelog.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsScreen
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.UsersScreen

/**
 * Extension function to add settings screen to a navigation graph
 */
fun NavGraphBuilder.settingsScreen(
    onLogout: () -> Unit = {},
) {
    composable(
        route = NavigationRoutes.Settings.route,
    ) {
        SettingsScreen(onLogout = onLogout)
    }

    composable(
        route = NavigationRoutes.Users.route,
    ) {
        UsersScreen()
    }
}
