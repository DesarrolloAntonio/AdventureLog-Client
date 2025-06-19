package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.AddEditAdventureScreen
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.AdventureListScreen

/**
 * Extension function to add adventures screens to a navigation graph
 */
fun NavGraphBuilder.adventuresScreen(
    onAdventureClick: (Adventure) -> Unit,
    onAddAdventureClick: () -> Unit,
    navController: NavController
) {
    // Adventures List Screen
    composable(route = NavigationRoutes.Adventures.route) {
        AdventureListScreen(
            onAdventureClick = onAdventureClick,
            onAddAdventureClick = onAddAdventureClick
        )
    }
    
    // Add Adventure Screen
    composable(route = "add_adventure") {
        AddEditAdventureScreen(
            onNavigateBack = {
                navController.navigateUp()
            },
            onSave = { formData ->
                // TODO: Handle save and navigate back
                println("Saving adventure: $formData")
                navController.navigateUp()
            }
        )
    }
}
