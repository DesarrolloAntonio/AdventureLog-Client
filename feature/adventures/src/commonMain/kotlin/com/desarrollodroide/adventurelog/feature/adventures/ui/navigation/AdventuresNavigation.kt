package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.adventures.ui.adventures.AdventureListScreen
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.AdventureDetailScreen

fun NavGraphBuilder.adventuresGraph(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = AdventuresScreen.route,
        route = AdventuresRoute.route
    ) {
        composable(route = AdventuresScreen.route) {
            AdventureListScreen(
                adventureItems = PreviewData.adventures,
                onOpenDetails = { adventureId: String ->
                    navController.navigate(AdventureDetailScreen.createRoute(adventureId))
                }
            )
        }
    }
}