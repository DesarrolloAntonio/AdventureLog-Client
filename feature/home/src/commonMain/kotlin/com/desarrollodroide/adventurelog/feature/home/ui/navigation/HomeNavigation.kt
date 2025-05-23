package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.home.ui.screen.HomeScreenRoute
import com.desarrollodroide.adventurelog.core.model.Adventure

interface HomeNavigator {
    fun goToDetail(adventure: Adventure)
    fun goToLogin()
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
                onAdventureClick = { adventure -> 
                    navigator.goToDetail(adventure)
                },
                onNavigateToLogin = {
                    navigator.goToLogin()
                }
            )
        }
    }
}