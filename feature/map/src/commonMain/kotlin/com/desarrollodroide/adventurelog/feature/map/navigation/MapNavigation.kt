package com.desarrollodroide.adventurelog.feature.map.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.map.ui.screen.MapScreen

/**
 * Extension function to add map screen to a navigation graph
 */
fun NavGraphBuilder.mapScreen(
    navController: NavController,
    onAdventureClick: (adventureId: String) -> Unit
) {
    composable(route = NavigationRoutes.Map.route) {
        MapScreen(
            onAdventureClick = onAdventureClick,
            onAddAdventureClick = {
                navController.navigate(NavigationRoutes.Adventures.add)
            }
        )
    }
}
