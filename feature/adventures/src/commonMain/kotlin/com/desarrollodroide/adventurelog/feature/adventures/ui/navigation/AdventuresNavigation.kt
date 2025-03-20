package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.adventures.ui.adventures.AdventureListScreen

/**
 * Adventures module navigation graph
 */
fun NavGraphBuilder.adventuresNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = AdventuresScreen.route,
        route = AdventuresRoute.route
    ) {
        composable(route = AdventuresScreen.route) {
            AdventureListScreen(
                adventureItems = PreviewData.adventures,
                onOpenDetails = { adventureId: String ->
                    navController.navigate(NavigationRoutes.Detail.createDetailRoute(adventureId))
                }
            )
        }
    }
}