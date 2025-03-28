package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.home.ui.screen.HomeScreenRoute

interface HomeNavigator {
    fun goToDetail(adventureId: String)
}

/**
 * Home module navigation graph
 */
fun NavGraphBuilder.homeNavGraph(
    navigator: HomeNavigator
) {
    navigation(
        route = NavigationRoutes.Home.graph,
        startDestination = NavigationRoutes.Home.screen
    ) {
        composable(route = NavigationRoutes.Home.screen) {
            HomeScreenRoute(
                onAdventureClick = {
                    adventureId -> navigator.goToDetail(adventureId)
                }
            )
        }
    }
}