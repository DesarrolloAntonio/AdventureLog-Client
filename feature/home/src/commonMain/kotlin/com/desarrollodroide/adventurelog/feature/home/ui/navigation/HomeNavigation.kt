package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.HomeNavigator
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.home.ui.screen.HomeScreenRoute

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
                onAdventureClick = { adventureId -> navigator.goToDetail(adventureId) }
            )
        }
        composable(
            route = "detail_screen/{adventureId}",
            arguments = listOf(navArgument("adventureId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adventureId = backStackEntry.arguments?.getString("adventureId") ?: ""
            //AdventureDetailScreen(adventureId = adventureId)
        }
    }
}