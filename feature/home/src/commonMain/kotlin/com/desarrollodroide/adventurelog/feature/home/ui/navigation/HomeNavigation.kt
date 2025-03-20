package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.home.ui.screen.HomeScreenRoute

/**
 * Home module navigation graph
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController
) {
    navigation(
        route = Home.route,
        startDestination = HomeScreen.route
    ) {
        composable(route = HomeScreen.route) {
            HomeScreenRoute(
                onAdventureClick = { adventureId ->
                    navController.navigate(NavigationRoutes.Detail.createDetailRoute(adventureId))
                }
            )
        }
    }
}