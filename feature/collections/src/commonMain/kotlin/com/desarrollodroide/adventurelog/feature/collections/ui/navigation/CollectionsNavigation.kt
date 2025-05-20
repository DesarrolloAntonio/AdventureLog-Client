package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionDetailScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionsScreen
import com.desarrollodroide.adventurelog.core.model.Adventure

/**
 * Extension function to add collections screen to a navigation graph
 * Meant to be used internally by the Home feature
 */
fun NavGraphBuilder.collectionsScreen(
    onCollectionClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onAdventureClick: (Adventure) -> Unit,
    navController: NavController
) {
    // Collections List Screen
    composable(route = NavigationRoutes.Collections.route) {
        CollectionsScreen(
            onCollectionClick = onCollectionClick
        )
    }
    
    // Collection Detail Screen
    composable(
        route = "collection/{collectionId}",
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
        CollectionDetailScreen(
            collectionId = collectionId,
            onBackClick = { 
                // Navigate back to collections list
                navController.navigateUp()
            },
            onHomeClick = onHomeClick,
            onAdventureClick = onAdventureClick
        )
    }
}