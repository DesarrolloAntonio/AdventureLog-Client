package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreenRoute

/**
 * Detail module navigation graph
 */
fun NavGraphBuilder.detailNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = "detail/{adventureId}",
        route = NavigationRoutes.Detail.route
    ) {
        composable(
            route = "detail/{adventureId}",
            arguments = listOf(navArgument("adventureId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adventureId = backStackEntry.arguments?.getString("adventureId") ?: ""
            AdventureDetailScreenRoute(
                adventureId = adventureId,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}