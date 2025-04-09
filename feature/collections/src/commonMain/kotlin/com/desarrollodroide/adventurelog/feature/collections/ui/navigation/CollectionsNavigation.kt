package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionsScreen

/**
 * Extension function to add collections screen to a navigation graph
 * Meant to be used internally by the Home feature
 */
fun NavGraphBuilder.collectionsScreen(
    onCollectionClick: (String) -> Unit
) {
    composable(route = NavigationRoutes.Collections.route) {
        CollectionsScreen(
            onCollectionClick = onCollectionClick
        )
    }
}